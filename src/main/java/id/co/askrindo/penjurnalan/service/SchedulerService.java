package id.co.askrindo.penjurnalan.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.askrindo.penjurnalan.config.SSLContextHelper;
import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import id.co.askrindo.penjurnalan.model.Journal;
import id.co.askrindo.penjurnalan.repository.FinanceDataPostingRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SchedulerService {

	public final Logger logger = LoggerFactory.getLogger("SchedulerService endpoints");

	@Autowired
	private FinanceDataPostingRepository financeDataPostingRepo;

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private Environment env;

	// schedule a job to add object in DB (Every 5 sec)
	@Scheduled(fixedRate = 5000)
	public void add2DBJob() {
		System.out.println("every 5 seconds ");
	}

	@Scheduled(cron = "0/15 * * * * *")
	public void fetchDBJob() {
		System.out.println("every 5 seconds maybe?");
	}

	public void hitBackFmsApi() throws IOException {
		List<FinanceDataPosting> datas = financeDataPostingRepo.findByStatus(0);
		for (FinanceDataPosting data: datas) {

			data.setRetryCount(data.getRetryCount()+1);
			data.setModifiedBy("h2h-kur-bri");
			data.setModifiedDate(new Date());
			FinanceDataPosting updateData = financeDataPostingRepo.save(data);

			//hit endpoint
			ResponseEntity<String> responseFms = hitEndpointFMS(data.getDataJson());

			if(responseFms != null) {
				try {
					JSONObject fmsResponse = new JSONObject(responseFms.getBody());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (responseFms.getStatusCodeValue() == 201) {
					data.setValueFromBackend(responseFms.toString());
					data.setStatus(1);
					FinanceDataPosting successData = financeDataPostingRepo.save(data);

					//update klaim_kur/t_ijp
				}
			} else {
				data.setValueFromBackend(responseFms.toString());
				data.setErrorMessage(responseFms.toString());
				FinanceDataPosting failedData = financeDataPostingRepo.save(data);
			}
		}
	}

	private ResponseEntity<String> hitEndpointFMS(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		Journal journal = objectMapper.readValue(jsonData, Journal.class);

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
}