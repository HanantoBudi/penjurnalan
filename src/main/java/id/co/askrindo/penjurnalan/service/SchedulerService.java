package id.co.askrindo.penjurnalan.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import id.co.askrindo.penjurnalan.model.KoreksiIjp;
import id.co.askrindo.penjurnalan.model.KoreksiKlaim;
import id.co.askrindo.penjurnalan.model.ProduksiIjp;
import id.co.askrindo.penjurnalan.model.ProduksiKlaim;
import id.co.askrindo.penjurnalan.repository.FinanceDataPostingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SchedulerService {

	public final Logger logger = LoggerFactory.getLogger("SchedulerService endpoints");

	@Autowired
	private FinanceDataPostingRepository financeDataPostingRepo;

	private String baseUrl = "http://10.10.1.233:8085/erp-api/journals/posts?postedBy=h2h-kur-bri&series=MEM";

	private RestTemplate restTemplate = new RestTemplate();

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
			String response = hitEndpointFMS(data.getDataFrom(), data.getDataJson());

			if (response == "success") {
				data.setValueFromBackend(response);
				FinanceDataPosting successData = financeDataPostingRepo.save(data);

				//update klaim_kur/t_ijp
			} else if (response == "FAILED") {
				data.setValueFromBackend(response);
				data.setErrorMessage(response);
				FinanceDataPosting failedData = financeDataPostingRepo.save(data);
			}
		}
	}

	private String hitEndpointFMS(String jsonType, String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String response = "";
		if (jsonType.equalsIgnoreCase("KOREKSI IJP")) {
			KoreksiIjp koreksiIjp = objectMapper.readValue(jsonData, KoreksiIjp.class);
			response = restTemplate.postForObject(baseUrl, koreksiIjp, String.class);
		} else if (jsonType.equalsIgnoreCase("KOREKSI KLAIM")) {
			KoreksiKlaim koreksiKlaim = objectMapper.readValue(jsonData, KoreksiKlaim.class);
			response = restTemplate.postForObject(baseUrl, koreksiKlaim, String.class);
		} else if (jsonType.equalsIgnoreCase("PRODUKSI IJP")) {
			ProduksiIjp produksiIjp = objectMapper.readValue(jsonData, ProduksiIjp.class);
			response = restTemplate.postForObject(baseUrl, produksiIjp, String.class);
		} else if (jsonType.equalsIgnoreCase("PRODUKSI KLAIM")) {
			ProduksiKlaim produksiKlaim = objectMapper.readValue(jsonData, ProduksiKlaim.class);
			response = restTemplate.postForObject(baseUrl, produksiKlaim, String.class);
		}
		logger.info("response - " + response);
		return response;
	}
}