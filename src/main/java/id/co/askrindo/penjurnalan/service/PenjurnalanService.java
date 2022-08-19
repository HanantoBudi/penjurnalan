package id.co.askrindo.penjurnalan.service;

import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import id.co.askrindo.penjurnalan.model.KoreksiIjp;
import id.co.askrindo.penjurnalan.model.KoreksiKlaim;
import id.co.askrindo.penjurnalan.model.ProduksiIjp;
import id.co.askrindo.penjurnalan.model.ProduksiKlaim;
import id.co.askrindo.penjurnalan.repository.FinanceDataPostingRepository;
import id.co.askrindo.penjurnalan.repository.KlaimKurRepository;
import id.co.askrindo.penjurnalan.repository.TIjpProjectedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
public class PenjurnalanService {
    public final Logger logger = LoggerFactory.getLogger("PenjurnalanService endpoints");

    @Autowired
    private FinanceDataPostingRepository financeDataPostingRepo;

    @Autowired
    private KlaimKurRepository klaimKurRepo;

    @Autowired
    private TIjpProjectedRepository tIjpProjectedRepo;

    private String baseUrl = "http://10.10.1.233:8085/erp-api/journals/posts?postedBy=h2h-kur-bri&series=MEM";

    private RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public ResponseEntity<?> penjurnalanProcess(String topic, String message,
            KoreksiIjp koreksiIjp, KoreksiKlaim koreksiKlaim,
            ProduksiIjp produksiIjp, ProduksiKlaim produksiKlaim) {
        try {
            String trxId = "";
            Optional<FinanceDataPosting> financeDataPosting = financeDataPostingRepo.findByTrxId(trxId);

            if (financeDataPosting.isEmpty()) {
                FinanceDataPosting newData = new FinanceDataPosting();
                newData.setDataType(setDataType(topic));
                newData.setDataFrom(topic);
                newData.setDataJson(message);
                newData.setStatus(0);
                newData.setRetryCount(0);
                newData.setErrorMessage("");
                newData.setTrxId(trxId);
                newData.setValueFromBackend("");
                newData.setCreatedBy("h2h-kur-bri");
                newData.setCreatedDate(new Date());
                FinanceDataPosting saveData = financeDataPostingRepo.save(newData);
            } {
                FinanceDataPosting data = financeDataPosting.get();
                data.setDataJson(message);
                data.setRetryCount(data.getRetryCount()+1);
                data.setModifiedBy("h2h-kur-bri");
                data.setModifiedDate(new Date());
                FinanceDataPosting updateData = financeDataPostingRepo.save(data);
            }

            //hit endpoint
            String response = hitEndpointFMS(koreksiIjp, koreksiKlaim, produksiIjp, produksiKlaim);

            if (response == "success") {
                FinanceDataPosting data = financeDataPosting.get();
                data.setValueFromBackend(response);
                FinanceDataPosting updateData = financeDataPostingRepo.save(data);

                //update klaim_kur/t_ijp
            } else if (response == "FAILED") {
                FinanceDataPosting data = financeDataPosting.get();
                data.setValueFromBackend(response);
                data.setErrorMessage(response);
                FinanceDataPosting updateData = financeDataPostingRepo.save(data);
                return new ResponseEntity<>("FAILED HIT FMS API", HttpStatus.EXPECTATION_FAILED);
            }

            return new ResponseEntity<>("PENJURNALAN CREATED", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("PENJURNALAN CREATE, FAILED : "+e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String hitEndpointFMS(KoreksiIjp koreksiIjp, KoreksiKlaim koreksiKlaim, ProduksiIjp produksiIjp, ProduksiKlaim produksiKlaim) {
        String response = "";
        if (koreksiIjp != null) {
            response = restTemplate.postForObject(baseUrl, koreksiIjp, String.class);
        } else if (koreksiKlaim != null) {
            response = restTemplate.postForObject(baseUrl, koreksiKlaim, String.class);
        } else if (produksiIjp != null) {
            response = restTemplate.postForObject(baseUrl, produksiIjp, String.class);
        } else if (produksiKlaim != null) {
            response = restTemplate.postForObject(baseUrl, produksiKlaim, String.class);
        }
        logger.info("response - " + response);
        return response;
    }

    private String setDataType(String topic) {
        if (topic.equalsIgnoreCase("KOREKSI IJP"))
            return "JURNAL IJP";
        else if (topic.equalsIgnoreCase("KOREKSI KLAIM"))
            return "JURNAL KLAIM";
        else if (topic.equalsIgnoreCase("PRODUKSI IJP"))
            return "JURNAL IJP";
        else if (topic.equalsIgnoreCase("PRODUKSI KLAIM"))
            return "JURNAL KLAIM";
        return topic;
    }
}