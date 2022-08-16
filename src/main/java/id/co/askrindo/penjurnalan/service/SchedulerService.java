package id.co.askrindo.penjurnalan.service;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

	Logger log = LoggerFactory.getLogger(SchedulerService.class);

	// schedule a job to add object in DB (Every 5 sec)
	@Scheduled(fixedRate = 5000)
	public void add2DBJob() {
		System.out.println("every 5 seconds ");
	}

	@Scheduled(cron = "0/15 * * * * *")
	public void fetchDBJob() {
		System.out.println("every 5 seconds maybe?");
	}

}