package id.co.askrindo.penjurnalan.service;

import id.co.askrindo.penjurnalan.config.SSLContextHelper;
import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import id.co.askrindo.penjurnalan.model.Journal;
import id.co.askrindo.penjurnalan.repository.FinanceDataPostingRepository;
import id.co.askrindo.penjurnalan.repository.KlaimKurRepository;
import id.co.askrindo.penjurnalan.repository.TIjpProjectedRepository;
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

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Environment env;

    @Transactional
    public ResponseEntity<?> penjurnalanProcess(String topic, String message, Journal journal) {
        try {
            String trxId = "";
            Optional<FinanceDataPosting> financeDataPosting = financeDataPostingRepo.findByTrxId(trxId);

            if (financeDataPosting.isEmpty()) {
                FinanceDataPosting newData = new FinanceDataPosting();
                newData.setDataType(setDataType(topic));
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
            ResponseEntity<String> responseFms = hitEndpointFMS(journal);

            String noJournal = "";

            //save log to table
            if(responseFms != null) {
                try {
                    JSONObject fmsResponse = new JSONObject(responseFms.getBody());
                    noJournal = fmsResponse.getString(journal.getSummaryDetailId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (responseFms.getStatusCodeValue() == 201) {
                    FinanceDataPosting data = financeDataPosting.get();
                    data.setValueFromBackend(responseFms.toString());
                    data.setStatus(1);
                    FinanceDataPosting updateData = financeDataPostingRepo.save(data);

                    //update klaim_kur/t_ijp
                }

                return new ResponseEntity<>("PENJURNALAN CREATED", HttpStatus.OK);
            } else {
                FinanceDataPosting data = financeDataPosting.get();
                data.setValueFromBackend(responseFms.toString());
                data.setErrorMessage(responseFms.toString());
                FinanceDataPosting updateData = financeDataPostingRepo.save(data);
                return new ResponseEntity<>("FAILED HIT FMS API", HttpStatus.EXPECTATION_FAILED);
            }
        } catch (Exception e) {
            logger.error("PENJURNALAN CREATE, FAILED : "+e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> hitEndpointFMS(Journal journal) {
        ResponseEntity<String> responseFms = null;
        String username = "fmsadmin";
        String password = "askrindo123";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        SSLContextHelper.disable();
        String url = env.getProperty("fms.api.journal");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity requestEntity = new HttpEntity(journal, headers);
        try {
            responseFms = restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (Exception e){
            logger.error("PENJURNALAN CREATE, FAILED : "+e.getMessage());
        }
        return responseFms;
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