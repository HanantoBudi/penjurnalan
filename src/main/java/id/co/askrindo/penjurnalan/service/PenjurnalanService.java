package id.co.askrindo.penjurnalan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import id.co.askrindo.penjurnalan.config.SSLContextHelper;
import id.co.askrindo.penjurnalan.entity.*;
import id.co.askrindo.penjurnalan.model.*;
import id.co.askrindo.penjurnalan.repository.*;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PenjurnalanService {
    public final Logger logger = LoggerFactory.getLogger("PenjurnalanService endpoints");

    @Autowired
    private FinanceDataPostingRepository financeDataPostingRepo;

    @Autowired
    private KlaimKurRepository klaimKurRepo;

    @Autowired
    private TIjpProjectedRepository tIjpProjectedRepo;

    @Autowired
    private PenjaminanKurRepository penjaminanKurRepo;

    @Autowired
    private PenjaminanKurSprRepository penjaminanKurSprRepo;

    @Autowired
    private ProductAcsRepository productAcsRepo;

    @Autowired
    private LogBrisurfRepository logBrisurfRepo;

    @Autowired
    private TCoveringValidationRepository tCoveringValidationRepo;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Environment env;

    DateFormat dateJournalFormat = new SimpleDateFormat("dd/MM/yyyy");
    DateFormat dateFullFormat = new SimpleDateFormat("dd MMMM yyyy");
    DateFormat monthJournalFormat = new SimpleDateFormat("MM");
    DateFormat yearJournalFormat = new SimpleDateFormat("yyyy");

    @Transactional
    public ResponseEntity<?> penjurnalanProcess(String topic, String uuid) {
        try {
            Optional<FinanceDataPosting> existingFinanceDataPosting = financeDataPostingRepo.findByTrxIdAndJournalName(uuid, topic);
            FinanceDataPosting financeDataPosting = null;

            if (existingFinanceDataPosting.isPresent())
                financeDataPosting = existingFinanceDataPosting.get();
            else {
                FinanceDataPosting newData = new FinanceDataPosting();
                newData.setDataType(setDataType(topic));
                newData.setDataJson("");
                newData.setJournalName(topic);
                newData.setStatus(0);
                newData.setRetryCount(0);
                newData.setErrorMessage("");
                newData.setTrxId(uuid);
                newData.setValueFromBackend("");
                newData.setCreatedBy("h2h-kur-bri");
                newData.setCreatedDate(new Date());
                financeDataPosting = financeDataPostingRepo.save(newData);
            }

            Thread.sleep(2000);

            if (financeDataPosting.getDataId() != null) {
                JournalProduksiIJP journalProduksiIJP = null;
                JournalPelunasanIJP journalPelunasanIJP = null;
                JournalProduksiKlaim journalProduksiKlaim = null;
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String message = "";
                if (topic.equalsIgnoreCase("PRODUKSI IJP")) {
                    journalProduksiIJP = mappingProduksiIjp(topic, uuid, financeDataPosting.getDataId());
                    message = ow.writeValueAsString(journalProduksiIJP);
                } else if (topic.equalsIgnoreCase("PELUNASAN IJP")) {
                    journalPelunasanIJP = mappingPelunasanIjp(topic, uuid, financeDataPosting.getDataId());
                    message = ow.writeValueAsString(journalPelunasanIJP);
                } else if (topic.equalsIgnoreCase("PRODUKSI KLAIM")) {
                    journalProduksiKlaim = mappingProduksiKlaim(topic, uuid, financeDataPosting.getDataId());
                    message = ow.writeValueAsString(journalProduksiKlaim);
                }

                FinanceDataPosting data = financeDataPosting;
                data.setDataId(financeDataPosting.getDataId());
                data.setDataJson(message);
                data.setModifiedBy("h2h-kur-bri");
                data.setModifiedDate(new Date());
                FinanceDataPosting updateData = financeDataPostingRepo.save(data);
                Thread.sleep(2000);

                //hit endpoint
                ResponseEntity<String> responseFms = new ResponseEntity<>("", HttpStatus.OK);;
                if (topic.equalsIgnoreCase("PRODUKSI IJP"))
                    responseFms = hitEndpointFMS(journalProduksiIJP, null, null, updateData);
                else if (topic.equalsIgnoreCase("PELUNASAN IJP"))
                    responseFms = hitEndpointFMS(null, journalPelunasanIJP, null, updateData);
                else if (topic.equalsIgnoreCase("PRODUKSI KLAIM"))
                    responseFms = hitEndpointFMS(null, null, journalProduksiKlaim, updateData);

                String noJournal = "";

                //save log to table
                if(responseFms != null) {
                    try {
                        JSONObject fmsResponse = new JSONObject(responseFms.getBody());
                        if (topic.equalsIgnoreCase("PRODUKSI IJP"))
                            noJournal = fmsResponse.getString(journalProduksiIJP.getSummaryDetailId());
                        else if (topic.equalsIgnoreCase("PELUNASAN IJP"))
                            noJournal = fmsResponse.getString(journalPelunasanIJP.getSummaryDetailId());
                        else if (topic.equalsIgnoreCase("PRODUKSI KLAIM"))
                            noJournal = fmsResponse.getString(journalProduksiKlaim.getSummaryDetailId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return new ResponseEntity<>("FAILED HIT FMS API", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    if (responseFms.getStatusCodeValue() == 201) {
                        FinanceDataPosting dataSuccess = financeDataPosting;
                        data.setRetryCount(data.getRetryCount()+1);
                        dataSuccess.setValueFromBackend(responseFms.toString());
                        dataSuccess.setStatus(1);
                        FinanceDataPosting updateSuccessData = financeDataPostingRepo.save(dataSuccess);

                        logger.info("PENJURNALAN CREATE, SUCCESS save data posting after hit api. " + updateSuccessData.toString());

                        if(updateSuccessData.getDataId() != null)
                            updateNoJurnalAndTanggalPosting(updateSuccessData, noJournal);

                        LogBrisurf log = new LogBrisurf();
                        if (topic.equalsIgnoreCase("PRODUKSI IJP")) {
                            Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(uuid);
                            log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
                            log.setJsonRequest(journalProduksiIJP.toString());
                        } else if (topic.equalsIgnoreCase("PELUNASAN IJP")) {
                            Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(uuid);
                            log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
                            log.setJsonRequest(journalPelunasanIJP.toString());
                        } else if (topic.equalsIgnoreCase("PRODUKSI KLAIM")) {
                            Optional<KlaimKur> klaimKur = klaimKurRepo.findById(uuid);
                            log.setNoRekening(klaimKur.get().getNoRekening());
                            log.setJsonRequest(journalProduksiKlaim.toString());
                        }
                        log.setService("PenjurnalanService");
                        log.setJsonResponse(String.valueOf(responseFms.getStatusCodeValue()));
                        log.setResponseCode(String.valueOf(responseFms.getStatusCodeValue()));
                        log.setResponseDesc("Success");
                        log.setIsIncomingRequest(false);
                        log.setEndpoint("http://10.10.1.233:8085/erp-api/journals/posts?postedBy=h2h-kur-bri&series=MEM");
                        log.setHttpStatusCode(responseFms.getStatusCodeValue());
                        log.setHttpMethod("POST");
                        LogBrisurf newLog = logBrisurfRepo.save(log);

                    }

                    logger.info("PENJURNALAN CREATE, SUCCESS save finance data posting. " + financeDataPosting.toString());
                    return new ResponseEntity<>("PENJURNALAN CREATED", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("FAILED HIT FMS API", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                logger.info("PENJURNALAN CREATE, FAILED save finance data posting. ");
                return new ResponseEntity<>("PENJURNALAN FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            logger.error("PENJURNALAN CREATE, FAILED : "+e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private JournalProduksiIJP mappingProduksiIjp (String topic, String uuid, String dataId) {
        try {
            Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(uuid);
            Optional<PenjaminanKur> penjaminanKur = penjaminanKurRepo.findByNoSertifikat(tIjpProjected.get().getNoRekeningPinjaman());
            Optional<PenjaminanKurSpr> penjaminanKurSpr = penjaminanKurSprRepo.findByNoSertifikatSpr(tIjpProjected.get().getNoRekeningPinjaman());
            Optional<ProductAcs> productAcs = null;

            String idPenjaminan = "";
            String noPolis = "";
            String namaUker = "";
            String jwkAwal = "";
            String jwkAkhir = "";
            String kodeProduk = "";
            String namaProduk = "";
            String tanggalTerbitPolis = (tIjpProjected.get().getTanggalProduksi() != null) ? dateJournalFormat.format(tIjpProjected.get().getTanggalProduksi()) : null;
            String debitur = "";
            String noRekeningPinjaman = tIjpProjected.get().getNoRekeningPinjaman();
            Long plafond = null;
            String branchIdString = (penjaminanKur.isPresent()) ? penjaminanKur.get().getKodeCabangAsk() : "";

            if (penjaminanKurSpr.isPresent()) {
                idPenjaminan = penjaminanKurSpr.get().getIdPenjaminan();
                noPolis = penjaminanKurSpr.get().getNoSertifikatSpr();
                namaUker = "";
                jwkAwal = dateJournalFormat.format(penjaminanKurSpr.get().getTanggalAwalSpr());
                jwkAkhir = dateJournalFormat.format(penjaminanKurSpr.get().getTanggalAkhirSpr());
                debitur = "";
                plafond =  penjaminanKurSpr.get().getPlafonSpr().longValue();
            } else {
                if (penjaminanKur.isPresent()) {
                    productAcs = productAcsRepo.findByJenisKreditAndJenisKur(penjaminanKur.get().getJenisKredit(), penjaminanKur.get().getJenisKur());
                    kodeProduk = productAcs.get().getProductId();
                    namaProduk = productAcs.get().getProductName();
                    idPenjaminan = penjaminanKur.get().getId();
                    noPolis = penjaminanKur.get().getNoSertifikat();
                    namaUker = penjaminanKur.get().getNamaUker();
                    jwkAwal = dateJournalFormat.format(penjaminanKur.get().getTanggalAwal());
                    jwkAkhir = dateJournalFormat.format(penjaminanKur.get().getTanggalAkhir());
                    debitur = penjaminanKur.get().getNamaDebitur();
                    plafond = penjaminanKur.get().getPlafonKredit().longValue();
                }
            }

            String produk = kodeProduk+" "+namaProduk;
            String namaTertanggung = "BANK RAKYAT INDONESIA, UKER : "+namaUker+", DEBITUR : "+debitur;
            String journalNotes = "Polis : "+noPolis+", Nasabah : "+namaUker+", Periode : "+jwkAwal+"-"+jwkAkhir+", Produk : "+produk+", Debitur : "+debitur+", No Rekening Pinjaman : "+noRekeningPinjaman;
            String dueDate = (penjaminanKur.isPresent()) ? dateJournalFormat.format(penjaminanKur.get().getTglApproveSpv()) : "";
            Double grossPremium = (tIjpProjected.get().getNominalIjp() != null) ? tIjpProjected.get().getNominalIjp().doubleValue() : null;
            Double transactionAccountingAmount = (tIjpProjected.get().getNominalIjp() != null) ? tIjpProjected.get().getNominalIjp().doubleValue() : null;

            //========================================================
            JournalProduksiIJPDetailDTO detail1 = new JournalProduksiIJPDetailDTO();
            detail1.setId(null);
            detail1.setJournalId(null);
            detail1.setBranchId(null);
            detail1.setBranchIdString(branchIdString);
            detail1.setAccountTypeIdString("Vendor");
            detail1.setAccountId(null);
            detail1.setAccountIdString("201110020161");
            detail1.setJournalDetailsTransactionSourceId(1020L);
            detail1.setAccountCashBankId(null);
            detail1.setMainAccountId("1.02.01.01.001.002");
            detail1.setDepartmentId(2L);
            detail1.setJournalDetailsTransactionCurrencyCode("IDR");
            detail1.setTransactionAmountDebit(transactionAccountingAmount);
            detail1.setTransactionAmountCredit((double) 0);
            detail1.setExchangeRate(null);
            detail1.setAccountingCurrencyCode("IDR");
            detail1.setAccountingAmountDebit(transactionAccountingAmount);
            detail1.setAccountingAmountCredit((double) 0);
            detail1.setJournalDetailNotes(journalNotes);
            detail1.setSettledTransactionId(null);
            detail1.setTaxJournalDetailId(null);
            detail1.setTaxReclassStatus(0L);

            JournalProduksiIJPDetailDTO detail2 = new JournalProduksiIJPDetailDTO();
            detail2.setId(null);
            detail2.setJournalId(null);
            detail2.setBranchId(null);
            detail2.setBranchIdString(branchIdString);
            detail2.setAccountTypeIdString("Ledger");
            detail2.setAccountId(null);
            detail2.setAccountIdString(null);
            detail2.setJournalDetailsTransactionSourceId(null);
            detail2.setAccountCashBankId(null);
            detail2.setMainAccountId("4.01.01.01.001.002");
            detail2.setDepartmentId(2L);
            detail2.setJournalDetailsTransactionCurrencyCode("IDR");
            detail2.setTransactionAmountDebit((double) 0);
            detail2.setTransactionAmountCredit(transactionAccountingAmount);
            detail2.setExchangeRate(null);
            detail2.setAccountingCurrencyCode("IDR");
            detail2.setAccountingAmountDebit((double) 0);
            detail2.setAccountingAmountCredit(transactionAccountingAmount);
            detail2.setJournalDetailNotes(journalNotes);
            detail2.setSettledTransactionId(null);
            detail2.setTaxJournalDetailId(null);
            detail2.setTaxReclassStatus(0L);

            List<JournalProduksiIJPDetailDTO> details = new ArrayList<>();
            details.add(detail1);
            details.add(detail2);
            //========================================================

            //========================================================
            JournalProduksiIJPExtended journalProduksiIJPExtended = new JournalProduksiIJPExtended();
            journalProduksiIJPExtended.setId(null);
            journalProduksiIJPExtended.setJournalId(null);
            journalProduksiIJPExtended.setKodeCob(kodeProduk);
            journalProduksiIJPExtended.setNamaCob(namaProduk);
            journalProduksiIJPExtended.setNomorPolis(noPolis);
            journalProduksiIJPExtended.setTanggalTerbitPolis(tanggalTerbitPolis);
            journalProduksiIJPExtended.setKodeBusinessPartner(null);
            journalProduksiIJPExtended.setNamaBusinessPartner(null);
            journalProduksiIJPExtended.setKodeTertanggung("201110020161");
            journalProduksiIJPExtended.setNamaTertanggung(namaTertanggung);
            journalProduksiIJPExtended.setReinsuranceFacultativeSlip(null);
            journalProduksiIJPExtended.setPeriodeAwal(jwkAwal);
            journalProduksiIJPExtended.setPeriodeAkhir(jwkAkhir);
            journalProduksiIJPExtended.setCurrencyCode("IDR");
            journalProduksiIJPExtended.setSumInsured(plafond);
            journalProduksiIJPExtended.setDueDate(dueDate);
            journalProduksiIJPExtended.setGrossPremium(grossPremium);
            journalProduksiIJPExtended.setDiscount(0L);
            journalProduksiIJPExtended.setPolicyFee(null);
            journalProduksiIJPExtended.setStampDuty(null);
            journalProduksiIJPExtended.setCommission(0L);
            journalProduksiIJPExtended.setPph(0L);
            journalProduksiIJPExtended.setPpn(0L);
            journalProduksiIJPExtended.setCreatorIPAddress(null);
            journalProduksiIJPExtended.setModifiedByIPAddress(null);
            journalProduksiIJPExtended.setDataId(idPenjaminan);
            journalProduksiIJPExtended.setSppaNo(noPolis);
            journalProduksiIJPExtended.setDataSource(2L);
            //========================================================

            //========================================================
            JournalProduksiIJP journalProduksiIJP = new JournalProduksiIJP();
            journalProduksiIJP.setId(null);
            journalProduksiIJP.setBranchId(null);
            journalProduksiIJP.setBranchIdString(branchIdString);
            journalProduksiIJP.setDocumentTypeId(7L);
            journalProduksiIJP.setAccountingDate(dateJournalFormat.format(new Date()));
            journalProduksiIJP.setEntryDate(dateJournalFormat.format(new Date()));
            journalProduksiIJP.setDueDate(dateJournalFormat.format(new Date()));
            journalProduksiIJP.setJournalNumber(null);
            journalProduksiIJP.setAccountId(null);
            journalProduksiIJP.setAccountCode(null);
            journalProduksiIJP.setAccountFromTo(null);
            journalProduksiIJP.setTransactionSourceId(12L);
            journalProduksiIJP.setAccountCashBankId(null);
            journalProduksiIJP.setMainAccountId(null);
            journalProduksiIJP.setDepartmentId(2L);
            journalProduksiIJP.setDebitCredit("D");
            journalProduksiIJP.setTransactionCurrencyCode("IDR");
            journalProduksiIJP.setTransactionAmount(null);
            journalProduksiIJP.setExchangeRate(null);
            journalProduksiIJP.setAccountingCurrencyCode("IDR");
            journalProduksiIJP.setAccountingAmount(0L);
            journalProduksiIJP.setFinancialMonth(Long.parseLong(monthJournalFormat.format(new Date())));
            journalProduksiIJP.setFinancialYear(Long.parseLong(yearJournalFormat.format(new Date())));
            journalProduksiIJP.setPostingStatus("Posted");
            journalProduksiIJP.setPostedDate(dateJournalFormat.format(new Date()));
            journalProduksiIJP.setPostedByName("h2h-kur-bri");
            journalProduksiIJP.setJournalNotes(journalNotes);
            journalProduksiIJP.setPaymentTypeId(null);
            journalProduksiIJP.setPaymentReferenceNumber(null);
            journalProduksiIJP.setReferenceNumber(noPolis);
            journalProduksiIJP.setJournalStatusId(8L);
            journalProduksiIJP.setJournalNotation(null);
            journalProduksiIJP.setBudgetItemId(null);
            journalProduksiIJP.setJournalDetailDTOs(details);
            journalProduksiIJP.setSummaryDetailId("AOS_BRISURF#"+tIjpProjected.get().getId());
            journalProduksiIJP.setAccountIdString(null);
            journalProduksiIJP.setAccountTypeIdString(null);
            journalProduksiIJP.setJournalExtended(journalProduksiIJPExtended);
            journalProduksiIJP.setVaNumberInternal(null);
            journalProduksiIJP.setDataId(dataId);
            journalProduksiIJP.setVaNumbers(null);
            //========================================================
            
            return journalProduksiIJP;
        } catch (Exception e) {
            logger.error("FAILED to Construct Produksi IJP Journal : "+e.getMessage());
            return null;
        }

    }

    private JournalPelunasanIJP mappingPelunasanIjp (String topic, String uuid, String dataId) {
        try {
            Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(uuid);
            Optional<PenjaminanKur> penjaminanKur = penjaminanKurRepo.findByNoSertifikat(tIjpProjected.get().getNoRekeningPinjaman());
            Optional<PenjaminanKurSpr> penjaminanKurSpr = penjaminanKurSprRepo.findByNoSertifikatSpr(tIjpProjected.get().getNoRekeningPinjaman());
            Double transactionAccountingAmount = tIjpProjected.get().getNominalIjp().doubleValue();

            String idPenjaminan = "";
            String noPolis = "";

            if (penjaminanKurSpr.isPresent()) {
                idPenjaminan = penjaminanKurSpr.get().getIdPenjaminan();
                noPolis = penjaminanKurSpr.get().getNoSertifikatSpr();
            } else {
                if (penjaminanKur.isPresent()) {
                    idPenjaminan = penjaminanKur.get().getId();
                    noPolis = penjaminanKur.get().getNoSertifikat();
                }
            }

            Optional<TCoveringValidation> tCoveringValidation = tCoveringValidationRepo.findById(tIjpProjected.get().getIdCoveringValidation());
            String tanggalCoveringFlag = "";
            String remark = "";
            if (tCoveringValidation.isPresent()) {
                tanggalCoveringFlag = dateFullFormat.format(tCoveringValidation.get().getCreatedDate());
                remark = tCoveringValidation.get().getRemark();
            }
            String journalNotes = "Pembayaran IJP KUR GEN 2 Bank BRI "+tanggalCoveringFlag+" R/C Cabang Cikini";

            //========================================================
            JournalPelunasanIJPDetailDTO detail1 = new JournalPelunasanIJPDetailDTO();
            detail1.setId(null);
            detail1.setJournalId(null);
            detail1.setBranchId(null);
            detail1.setBranchIdString("KP");
            detail1.setAccountTypeIdString("Ledger");
            detail1.setAccountId(null);
            detail1.setAccountIdString(null);
            detail1.setJournalDetailsTransactionSourceId(null);
            detail1.setAccountCashBankId(null);
            detail1.setMainAccountId("");
            detail1.setDepartmentId(2L);
            detail1.setJournalDetailsTransactionCurrencyCode("IDR");
            detail1.setTransactionAmountDebit((double) 0);
            detail1.setTransactionAmountCredit(transactionAccountingAmount);
            detail1.setExchangeRate((double) 1);
            detail1.setAccountingCurrencyCode("IDR");
            detail1.setAccountingAmountDebit((double) 0);
            detail1.setAccountingAmountCredit(transactionAccountingAmount);
            detail1.setJournalDetailNotes(journalNotes);
            detail1.setSettledTransactionId(null);
            detail1.setTaxJournalDetailId(null);
            detail1.setTaxReclassStatus(0L);
            detail1.setCashFlowActivityId(1L);

            List<JournalPelunasanIJPDetailDTO> details = new ArrayList<>();
            details.add(detail1);
            //========================================================

            //========================================================
            JournalPelunasanIJP journalPelunasanIJP = new JournalPelunasanIJP();
            journalPelunasanIJP.setId(null);
            journalPelunasanIJP.setBranchId(null);
            journalPelunasanIJP.setBranchIdString("KP");
            journalPelunasanIJP.setDocumentTypeId(6L);
            journalPelunasanIJP.setAccountingDate(dateJournalFormat.format(new Date()));
            journalPelunasanIJP.setEntryDate(dateJournalFormat.format(new Date()));
            journalPelunasanIJP.setDueDate(dateJournalFormat.format(new Date()));
            journalPelunasanIJP.setJournalNumber(null);
            journalPelunasanIJP.setAccountId(null);
            journalPelunasanIJP.setAccountCode(null);
            journalPelunasanIJP.setAccountFromTo("Bank BRI");
            journalPelunasanIJP.setTransactionSourceId(12L);
            journalPelunasanIJP.setAccountCashBankId(null);
            journalPelunasanIJP.setMainAccountId("1.01.02.01.002.002");
            journalPelunasanIJP.setDepartmentId(2L);
            journalPelunasanIJP.setDebitCredit("D");
            journalPelunasanIJP.setTransactionCurrencyCode("IDR");
            journalPelunasanIJP.setTransactionAmount(transactionAccountingAmount.longValue());
            journalPelunasanIJP.setExchangeRate(1L);
            journalPelunasanIJP.setAccountingCurrencyCode("IDR");
            journalPelunasanIJP.setAccountingAmount(transactionAccountingAmount.longValue());
            journalPelunasanIJP.setFinancialMonth(Long.parseLong(monthJournalFormat.format(new Date())));
            journalPelunasanIJP.setFinancialYear(Long.parseLong(yearJournalFormat.format(new Date())));
            journalPelunasanIJP.setPostingStatus("Posted");
            journalPelunasanIJP.setPostedDate(dateJournalFormat.format(new Date()));
            journalPelunasanIJP.setPostedByName("h2h-kur-bri");
            journalPelunasanIJP.setJournalNotes(journalNotes);
            journalPelunasanIJP.setPaymentTypeId(4L);
            journalPelunasanIJP.setPaymentReferenceNumber(remark);
            journalPelunasanIJP.setReferenceNumber(noPolis);
            journalPelunasanIJP.setJournalStatusId(8L);
            journalPelunasanIJP.setJournalNotation(null);
            journalPelunasanIJP.setBudgetItemId(null);
            journalPelunasanIJP.setJournalDetailDTOS(details);
            journalPelunasanIJP.setSummaryDetailId("AOS_BRISURF#"+tIjpProjected.get().getId());
            journalPelunasanIJP.setAccountIdString(null);
            journalPelunasanIJP.setAccountTypeIdString("Bank");
            journalPelunasanIJP.setDataId(idPenjaminan);
            //========================================================

            return journalPelunasanIJP;
        } catch (Exception e) {
            logger.error("FAILED to Construct Pelunasan IJP Journal : "+e.getMessage());
            return null;
        }
    }

    private JournalProduksiKlaim mappingProduksiKlaim (String topic, String uuid, String dataId) {
        try {
            Optional<KlaimKur> klaimKur = klaimKurRepo.findById(uuid);
            Optional<PenjaminanKur> penjaminanKur = penjaminanKurRepo.findByNoSertifikat(klaimKur.get().getNoRekening());
            Optional<PenjaminanKurSpr> penjaminanKurSpr = penjaminanKurSprRepo.findByNoSertifikatSpr(klaimKur.get().getNoRekening());
            Optional<ProductAcs> productAcs = null;

            String idPenjaminan = "";
            String noPolis = "";
            String namaUker = "";
            String jwkAwal = "";
            String jwkAkhir = "";
            String kodeProduk = "";
            String namaProduk = "";
            String tanggalTerbitPolis = (klaimKur.get().getCreatedDate() != null) ? dateJournalFormat.format(klaimKur.get().getCreatedDate()) : null;
            String debitur = "";
            String noRekeningPinjaman = klaimKur.get().getNoRekening();
            Long plafond = null;
            String branchIdString = (penjaminanKur.isPresent()) ? penjaminanKur.get().getKodeCabangAsk() : "";
            String dueDate = (penjaminanKur.isPresent()) ? dateJournalFormat.format(penjaminanKur.get().getTglApproveSpv()) : "";
            Double grossPremium = (klaimKur.get().getNetClaimApproved() != null) ? klaimKur.get().getNetClaimApproved().doubleValue() : null;
            Double transactionAccountingAmount = (klaimKur.get().getNetClaimApproved() != null) ? klaimKur.get().getNetClaimApproved().doubleValue() : null;

            if (penjaminanKurSpr.isPresent()) {
                idPenjaminan = penjaminanKurSpr.get().getIdPenjaminan();
                noPolis = penjaminanKurSpr.get().getNoSertifikatSpr();
                namaUker = "";
                jwkAwal = dateJournalFormat.format(penjaminanKurSpr.get().getTanggalAwalSpr());
                jwkAkhir = dateJournalFormat.format(penjaminanKurSpr.get().getTanggalAkhirSpr());
                debitur = "";
                plafond = penjaminanKurSpr.get().getPlafonSpr().longValue();
            } else {
                if (penjaminanKur.isPresent()) {
                    productAcs = productAcsRepo.findByJenisKreditAndJenisKur(penjaminanKur.get().getJenisKredit(), penjaminanKur.get().getJenisKur());
                    kodeProduk = (productAcs.isPresent()) ? productAcs.get().getProductId() : "";
                    namaProduk = (productAcs.isPresent()) ? productAcs.get().getProductName() : "";
                    idPenjaminan = penjaminanKur.get().getId();
                    noPolis = penjaminanKur.get().getNoSertifikat();
                    namaUker = penjaminanKur.get().getNamaUker();
                    jwkAwal = dateJournalFormat.format(penjaminanKur.get().getTanggalAwal());
                    jwkAkhir = dateJournalFormat.format(penjaminanKur.get().getTanggalAkhir());
                    debitur = penjaminanKur.get().getNamaDebitur();
                    plafond =  penjaminanKur.get().getPlafonKredit().longValue();
                }
            }

            String produk = kodeProduk+" "+namaProduk;
            String namaTertanggung = "BANK RAKYAT INDONESIA, UKER : "+namaUker+", DEBITUR : "+debitur;
            String journalNotes = "Nomor LPK : "+klaimKur.get().getNoKlaim()+", Polis : "+noPolis+", Nasabah : "+namaUker+", Periode : "+jwkAwal+"-"+jwkAkhir+", Produk : "+produk+", Debitur : "+debitur+", No Rekening Pinjaman : "+noRekeningPinjaman;

            //========================================================
            JournalProduksiKlaimDetailDTO detail1 = new JournalProduksiKlaimDetailDTO();
            detail1.setId(null);
            detail1.setJournalId(null);
            detail1.setBranchId(null);
            detail1.setBranchIdString(branchIdString);
            detail1.setAccountTypeIdString("Ledger");
            detail1.setAccountId(null);
            detail1.setAccountIdString("201110020161");
            detail1.setJournalDetailsTransactionSourceId(1020L);
            detail1.setAccountCashBankId(null);
            detail1.setMainAccountId("1.02.01.01.001.002");
            detail1.setDepartmentId(2L);
            detail1.setJournalDetailsTransactionCurrencyCode("IDR");
            detail1.setTransactionAmountDebit(transactionAccountingAmount);
            detail1.setTransactionAmountCredit((double) 0);
            detail1.setExchangeRate(null);
            detail1.setAccountingCurrencyCode("IDR");
            detail1.setAccountingAmountDebit(transactionAccountingAmount);
            detail1.setAccountingAmountCredit((double) 0);
            detail1.setJournalDetailNotes("");
            detail1.setSettledTransactionId(null);
            detail1.setTaxJournalDetailId(null);
            detail1.setTaxReclassStatus(0L);

            JournalProduksiKlaimDetailDTO detail2 = new JournalProduksiKlaimDetailDTO();
            detail2.setId(null);
            detail2.setJournalId(null);
            detail2.setBranchId(null);
            detail2.setBranchIdString(branchIdString);
            detail2.setAccountTypeIdString("Vendor");
            detail2.setAccountId(null);
            detail2.setAccountIdString(null);
            detail2.setJournalDetailsTransactionSourceId(null);
            detail2.setAccountCashBankId(null);
            detail2.setMainAccountId("4.01.01.01.001.002");
            detail2.setDepartmentId(2L);
            detail2.setJournalDetailsTransactionCurrencyCode("IDR");
            detail2.setTransactionAmountDebit((double) 0);
            detail2.setTransactionAmountCredit(transactionAccountingAmount);
            detail2.setExchangeRate(null);
            detail2.setAccountingCurrencyCode("IDR");
            detail2.setAccountingAmountDebit((double) 0);
            detail2.setAccountingAmountCredit(transactionAccountingAmount);
            detail2.setJournalDetailNotes(journalNotes);
            detail2.setSettledTransactionId(null);
            detail2.setTaxJournalDetailId(null);
            detail2.setTaxReclassStatus(0L);

            List<JournalProduksiKlaimDetailDTO> details = new ArrayList<>();
            details.add(detail1);
            details.add(detail2);
            //========================================================

            //========================================================
            JournalProduksiKlaimExtended journalProduksiKlaimExtended = new JournalProduksiKlaimExtended();
            journalProduksiKlaimExtended.setId(null);
            journalProduksiKlaimExtended.setJournalID(null);
            journalProduksiKlaimExtended.setKodeCob(kodeProduk);
            journalProduksiKlaimExtended.setNamaCob(namaProduk);
            journalProduksiKlaimExtended.setNomorPolis(noPolis);
            journalProduksiKlaimExtended.setTanggalTerbitPolis(tanggalTerbitPolis);
            journalProduksiKlaimExtended.setKodeBusinessPartner(null);
            journalProduksiKlaimExtended.setNamaBusinessPartner(null);
            journalProduksiKlaimExtended.setKodeTertanggung("201110020161");
            journalProduksiKlaimExtended.setNamaTertanggung(namaTertanggung);
            journalProduksiKlaimExtended.setReinsuranceFacultativeSlip(null);
            journalProduksiKlaimExtended.setPeriodeAwal(jwkAwal);
            journalProduksiKlaimExtended.setPeriodeAkhir(jwkAkhir);
            journalProduksiKlaimExtended.setCurrencyCode("IDR");
            journalProduksiKlaimExtended.setSumInsured(plafond);
            journalProduksiKlaimExtended.setDueDate(dueDate);
            journalProduksiKlaimExtended.setGrossPremium(grossPremium);
            journalProduksiKlaimExtended.setDiscount(0L);
            journalProduksiKlaimExtended.setPolicyFee(null);
            journalProduksiKlaimExtended.setStampDuty(null);
            journalProduksiKlaimExtended.setCommission(0L);
            journalProduksiKlaimExtended.setPph(0L);
            journalProduksiKlaimExtended.setPpn(0L);
            journalProduksiKlaimExtended.setCreatorIPAddress(null);
            journalProduksiKlaimExtended.setModifiedByIPAddress(null);
            journalProduksiKlaimExtended.setDataId(klaimKur.get().getId());
            journalProduksiKlaimExtended.setSppaNo(null);
            journalProduksiKlaimExtended.setDataSource(2L);
            //========================================================

            //========================================================
            JournalProduksiKlaim journalProduksiKlaim = new JournalProduksiKlaim();
            journalProduksiKlaim.setId(null);
            journalProduksiKlaim.setBranchId(null);
            journalProduksiKlaim.setBranchIdString(branchIdString);
            journalProduksiKlaim.setDocumentTypeId(7L);
            journalProduksiKlaim.setAccountingDate(dateJournalFormat.format(new Date()));
            journalProduksiKlaim.setEntryDate(dateJournalFormat.format(new Date()));
            journalProduksiKlaim.setDueDate(dateJournalFormat.format(new Date()));
            journalProduksiKlaim.setJournalNumber(null);
            journalProduksiKlaim.setAccountId(null);
            journalProduksiKlaim.setAccountCode(null);
            journalProduksiKlaim.setAccountFromTo(null);
            journalProduksiKlaim.setTransactionSourceId(21L);
            journalProduksiKlaim.setAccountCashBankId(null);
            journalProduksiKlaim.setMainAccountId(null);
            journalProduksiKlaim.setDepartmentId(2L);
            journalProduksiKlaim.setDebitCredit("D");
            journalProduksiKlaim.setTransactionCurrencyCode("IDR");
            journalProduksiKlaim.setTransactionAmount(0L);
            journalProduksiKlaim.setExchangeRate(null);
            journalProduksiKlaim.setAccountingCurrencyCode("IDR");
            journalProduksiKlaim.setAccountingAmount(0L);
            journalProduksiKlaim.setFinancialMonth(Long.parseLong(monthJournalFormat.format(new Date())));
            journalProduksiKlaim.setFinancialYear(Long.parseLong(yearJournalFormat.format(new Date())));
            journalProduksiKlaim.setPostingStatus("Posted");
            journalProduksiKlaim.setPostedDate(dateJournalFormat.format(new Date()));
            journalProduksiKlaim.setPostedByName("h2h-kur-bri");
            journalProduksiKlaim.setJournalNotes(journalNotes);
            journalProduksiKlaim.setPaymentTypeId(null);
            journalProduksiKlaim.setPaymentReferenceNumber(null);
            journalProduksiKlaim.setReferenceNumber(klaimKur.get().getNoKlaim());
            journalProduksiKlaim.setJournalStatusId(8L);
            journalProduksiKlaim.setJournalNotation(null);
            journalProduksiKlaim.setBudgetItemId(null);
            journalProduksiKlaim.setJournalDetailDTOs(details);
            journalProduksiKlaim.setSummaryDetailId("AOS_BRISURF#"+klaimKur.get().getId());
            journalProduksiKlaim.setAccountIdString(null);
            journalProduksiKlaim.setAccountTypeIdString(null);
            journalProduksiKlaim.setJournalExtended(journalProduksiKlaimExtended);
            journalProduksiKlaim.setVaNumberInternal(null);
            journalProduksiKlaim.setDataId(dataId);
            journalProduksiKlaim.setVaNumbers(null);
            //========================================================

            return journalProduksiKlaim;
        } catch (Exception e) {
            logger.error("FAILED to Construct Produksi Klaim Journal : "+e.getMessage());
            return null;
        }
    }

    private ResponseEntity<String> hitEndpointFMS(
            JournalProduksiIJP produksiIjp,
            JournalPelunasanIJP pelunasanIjp,
            JournalProduksiKlaim produksiKlaim,
            FinanceDataPosting financeDataPosting) throws JsonProcessingException {
        ResponseEntity<String> responseFms = null;
        String username = "fmsadmin";
        String password = "askrindo123";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        SSLContextHelper.disable();
        String url = env.getProperty("fms.api.journal");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity requestEntity = null;

//        if (produksiIjp != null) {
//            List<JournalProduksiIJP> produksiIJPs = new ArrayList<>();
//            produksiIJPs.add(produksiIjp);
//            requestEntity = new HttpEntity(produksiIJPs, headers);
//        }
//        else if (pelunasanIjp != null) {
//            List<JournalPelunasanIJP> pelunasanIJPs = new ArrayList<>();
//            pelunasanIJPs.add(pelunasanIjp);
//            requestEntity = new HttpEntity(pelunasanIJPs, headers);
//        }
//        else if ((produksiKlaim != null)) {
//            List<JournalProduksiKlaim> produksiKlaims = new ArrayList<>();
//            produksiKlaims.add(produksiKlaim);
//            requestEntity = new HttpEntity(produksiKlaims, headers);
//        }

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String message = "";
        if (produksiIjp != null) {
            List<JournalProduksiIJP> produksiIJPs = new ArrayList<>();
            produksiIJPs.add(produksiIjp);
            message = ow.writeValueAsString(produksiIJPs);
        } else if (pelunasanIjp != null) {
            List<JournalPelunasanIJP> pelunasanIJPS = new ArrayList<>();
            pelunasanIJPS.add(pelunasanIjp);
            message = ow.writeValueAsString(pelunasanIJPS);
        } else if ((produksiKlaim != null)) {
            List<JournalProduksiKlaim> produksiKlaims = new ArrayList<>();
            produksiKlaims.add(produksiKlaim);
            message = ow.writeValueAsString(produksiKlaims);
        }
        requestEntity = new HttpEntity(message, headers);

        try {
            responseFms = restTemplate.postForEntity(url, requestEntity, String.class);
            logger.info("PENJURNALAN CREATE, SUCCESS HIT API FMS : "+responseFms.toString());
        } catch (Exception e){
            FinanceDataPosting data = financeDataPosting;
            data.setRetryCount(data.getRetryCount()+1);
            data.setValueFromBackend(e.getMessage());
            data.setErrorMessage(e.getMessage());
            data.setModifiedBy("h2h-kur-bri");
            data.setModifiedDate(new Date());
            FinanceDataPosting updateData = financeDataPostingRepo.save(data);
            logger.error("PENJURNALAN CREATE, FAILED HIT API FMS : "+e.getMessage());

            LogBrisurf log = new LogBrisurf();
            if (produksiIjp != null) {
                Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(financeDataPosting.getTrxId());
                log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
                log.setJsonRequest(produksiIjp.toString());
            } else if (pelunasanIjp != null) {
                Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(financeDataPosting.getTrxId());
                log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
                log.setJsonRequest(pelunasanIjp.toString());
            } else if ((produksiKlaim != null)) {
                Optional<KlaimKur> klaimKur = klaimKurRepo.findById(financeDataPosting.getTrxId());
                log.setNoRekening(klaimKur.get().getNoRekening());
                log.setJsonRequest(produksiKlaim.toString());
            }
            log.setService("PenjurnalanService");
            log.setJsonResponse(e.getMessage());
            log.setResponseCode("500");
            log.setResponseDesc("FAILED HIT");
            log.setIsIncomingRequest(false);
            log.setEndpoint("http://10.10.1.233:8085/erp-api/journals/posts?postedBy=h2h-kur-bri&series=MEM");
            log.setHttpStatusCode(500);
            log.setHttpMethod("POST");
            LogBrisurf newLog = logBrisurfRepo.save(log);
        }
        return responseFms;
    }

    private String setDataType(String topic) {
        if (topic.equalsIgnoreCase("PRODUKSI IJP"))
            return "JURNAL IJP";
        else if (topic.equalsIgnoreCase("PELUNASAN IJP"))
            return "JURNAL IJP";
        else if (topic.equalsIgnoreCase("PRODUKSI KLAIM"))
            return "JURNAL KLAIM";
        return topic;
    }

    private void updateNoJurnalAndTanggalPosting(FinanceDataPosting data, String noJurnal) {
        try {
            if (data.getDataType().contains("KLAIM")) {
                Optional<KlaimKur> result = klaimKurRepo.findById(Integer.valueOf(data.getTrxId()));
                if (result.isPresent()) {
                    KlaimKur klaimKur = result.get();
                    klaimKur.setNoJurnal(noJurnal);
                    klaimKur.setTanggalPosting(new Date());
                    klaimKurRepo.save(klaimKur);
                }
            } else if (data.getDataType().contains("IJP")) {
                Optional<TIjpProjected> result = tIjpProjectedRepo.findById(Integer.valueOf(data.getTrxId()));
                if (result.isPresent()) {
                    TIjpProjected tIjpProjected = result.get();
                    tIjpProjected.setNoJurnal(noJurnal);
                    tIjpProjected.setTanggalPosting(new Date());
                    tIjpProjectedRepo.save(tIjpProjected);
                }
            }
            logger.info("SUCCESS UPDATE NO_JURNAL. ");
        } catch (Exception e) {
            logger.error("FAILED UPDATE NO_JURNAL : "+e.getMessage());
        }
    }

}