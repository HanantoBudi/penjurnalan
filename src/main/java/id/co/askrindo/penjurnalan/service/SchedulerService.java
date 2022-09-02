package id.co.askrindo.penjurnalan.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import id.co.askrindo.penjurnalan.config.SSLContextHelper;
import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import id.co.askrindo.penjurnalan.entity.KlaimKur;
import id.co.askrindo.penjurnalan.entity.LogBrisurf;
import id.co.askrindo.penjurnalan.entity.TIjpProjected;
import id.co.askrindo.penjurnalan.model.JournalPelunasanIJP;
import id.co.askrindo.penjurnalan.model.JournalProduksiIJP;
import id.co.askrindo.penjurnalan.model.JournalProduksiKlaim;
import id.co.askrindo.penjurnalan.repository.FinanceDataPostingRepository;
import id.co.askrindo.penjurnalan.repository.KlaimKurRepository;
import id.co.askrindo.penjurnalan.repository.LogBrisurfRepository;
import id.co.askrindo.penjurnalan.repository.TIjpProjectedRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SchedulerService {

	public final Logger logger = LoggerFactory.getLogger("SchedulerService endpoints");

	@Autowired
	private FinanceDataPostingRepository financeDataPostingRepo;

	@Autowired
	private KlaimKurRepository klaimKurRepo;

	@Autowired
	private TIjpProjectedRepository tIjpProjectedRepo;

	@Autowired
	private LogBrisurfRepository logBrisurfRepo;

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private Environment env;

	public ResponseEntity<?> hitBackFmsApi() throws IOException {
		try {
			List<FinanceDataPosting> datas = financeDataPostingRepo.findByStatus(0);
			if (!datas.isEmpty()) {
				for (FinanceDataPosting data: datas) {

					data.setRetryCount(data.getRetryCount()+1);
					data.setModifiedBy("h2h-kur-bri");
					data.setModifiedDate(new Date());
					FinanceDataPosting updateData = financeDataPostingRepo.save(data);

					ResponseEntity<String> responseFms = hitEndpointFMS(data.getTrxId(), data.getDataJson(), data.getJournalName());

					if(responseFms != null) {
						try {
							JSONObject fmsResponse = new JSONObject(responseFms.getBody());
							if (responseFms.getStatusCodeValue() == 201) {
								data.setValueFromBackend(responseFms.toString());
								data.setStatus(1);
								FinanceDataPosting successData = financeDataPostingRepo.save(data);

								if (successData.getDataId() != null)
									updateNoJurnalAndTanggalPosting(successData, fmsResponse);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						data.setValueFromBackend(responseFms.toString());
						data.setErrorMessage(responseFms.toString());
						FinanceDataPosting failedData = financeDataPostingRepo.save(data);
					}
				}
			}

			return new ResponseEntity<>("PENJURNALAN UPDATED", HttpStatus.OK);
		} catch (Exception e) {
			logger.error("PENJURNALAN UPDATE, FAILED : "+e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ResponseEntity<String> hitEndpointFMS(String trxId, String jsonData, String journalName) throws IOException {
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
		ObjectMapper objectMapper = new ObjectMapper();
		JournalProduksiIJP journalProduksiIJP = null;
		JournalProduksiKlaim journalProduksiKlaim = null;
		JournalPelunasanIJP journalPelunasanIJP = null;
//		if (journalName.equalsIgnoreCase("PRODUKSI IJP")) {
//			List<JournalProduksiIJP> produksiIJPs = new ArrayList<>();
//			journalProduksiIJP = objectMapper.readValue(jsonData, JournalProduksiIJP.class);
//			produksiIJPs.add(journalProduksiIJP);
//			requestEntity = new HttpEntity(produksiIJPs, headers);
//		} else if (journalName.equalsIgnoreCase("PRODUKSI KLAIM")) {
//			List<JournalProduksiKlaim> produksiKlaims = new ArrayList<>();
//			journalProduksiKlaim = objectMapper.readValue(jsonData, JournalProduksiKlaim.class);
//			produksiKlaims.add(journalProduksiKlaim);
//			requestEntity = new HttpEntity(produksiKlaims, headers);
//		} else if (journalName.equalsIgnoreCase("PELUNASAN IJP")) {
//			List<JournalPelunasanIJP> pelunasanIJPs = new ArrayList<>();
//			journalPelunasanIJP = objectMapper.readValue(jsonData, JournalPelunasanIJP.class);
//			pelunasanIJPs.add(journalPelunasanIJP);
//			requestEntity = new HttpEntity(pelunasanIJPs, headers);
//		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String message = "";
		if (journalName.equalsIgnoreCase("PRODUKSI IJP")) {
			List<JournalProduksiIJP> produksiIJPs = new ArrayList<>();
			journalProduksiIJP = objectMapper.readValue(jsonData, JournalProduksiIJP.class);
			produksiIJPs.add(journalProduksiIJP);
			message = ow.writeValueAsString(produksiIJPs);
		} else if (journalName.equalsIgnoreCase("PRODUKSI KLAIM")) {
			List<JournalProduksiKlaim> produksiKlaims = new ArrayList<>();
			journalProduksiKlaim = objectMapper.readValue(jsonData, JournalProduksiKlaim.class);
			produksiKlaims.add(journalProduksiKlaim);
			message = ow.writeValueAsString(produksiKlaims);
		} else if (journalName.equalsIgnoreCase("PELUNASAN IJP")) {
			List<JournalPelunasanIJP> pelunasanIJPs = new ArrayList<>();
			journalPelunasanIJP = objectMapper.readValue(jsonData, JournalPelunasanIJP.class);
			pelunasanIJPs.add(journalPelunasanIJP);
			message = ow.writeValueAsString(pelunasanIJPs);
		}
		requestEntity = new HttpEntity(message, headers);

		try {
			responseFms = restTemplate.postForEntity(url, requestEntity, String.class);
			logger.info("PENJURNALAN CREATE, SUCCESS HIT API FMS : "+responseFms.toString());

			LogBrisurf log = new LogBrisurf();
			if (journalName.equalsIgnoreCase("PRODUKSI IJP")) {
				Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(trxId);
				log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
				log.setJsonRequest(journalProduksiIJP.toString());
			} else if (journalName.equalsIgnoreCase("PELUNASAN IJP")) {
				Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(trxId);
				log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
				log.setJsonRequest(journalPelunasanIJP.toString());
			} else if (journalName.equalsIgnoreCase("PRODUKSI KLAIM")) {
				Optional<KlaimKur> klaimKur = klaimKurRepo.findById(trxId);
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
		} catch (Exception e){
			logger.error("PENJURNALAN CREATE, FAILED : "+e.getMessage());

			LogBrisurf log = new LogBrisurf();
			if (journalName.equalsIgnoreCase("PRODUKSI IJP")) {
				Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(trxId);
				log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
				log.setJsonRequest(journalProduksiIJP.toString());
			} else if (journalName.equalsIgnoreCase("PELUNASAN IJP")) {
				Optional<TIjpProjected> tIjpProjected = tIjpProjectedRepo.findById(trxId);
				log.setNoRekening(tIjpProjected.get().getNoRekeningPinjaman());
				log.setJsonRequest(journalPelunasanIJP.toString());
			} else if (journalName.equalsIgnoreCase("PRODUKSI KLAIM")) {
				Optional<KlaimKur> klaimKur = klaimKurRepo.findById(trxId);
				log.setNoRekening(klaimKur.get().getNoRekening());
				log.setJsonRequest(journalProduksiKlaim.toString());
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

	private void updateNoJurnalAndTanggalPosting(FinanceDataPosting data, JSONObject fmsResponse) {
		try {
			if (data.getDataType().contains("CLAIM")) {
				Optional<KlaimKur> result = klaimKurRepo.findById(Integer.valueOf(data.getTrxId()));
				if (result != null) {
					KlaimKur klaimKur = result.get();
					String noJurnal = fmsResponse.getString("AOS_BRISURF#"+klaimKur.getId());
					klaimKur.setNoJurnal(noJurnal);
					klaimKur.setTanggalPosting(new Date());
					klaimKurRepo.save(klaimKur);
				}
			} else if (data.getDataType().contains("IJP")) {
				Optional<TIjpProjected> result = tIjpProjectedRepo.findById(Integer.valueOf(data.getTrxId()));
				if (result != null) {
					TIjpProjected tIjpProjected = result.get();
					String noJurnal = fmsResponse.getString("AOS_BRISURF#"+tIjpProjected.getId());
					tIjpProjected.setNoJurnal(noJurnal);
					tIjpProjected.setTanggalPosting(new Date());
					tIjpProjectedRepo.save(tIjpProjected);
				}
			}
		} catch (Exception e) {
			logger.error("FAILED UPDATE NO_JURNAL : "+e.getMessage());
		}
	}
}