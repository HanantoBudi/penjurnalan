package id.co.askrindo.penjurnalan.scheduler;

import id.co.askrindo.penjurnalan.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

@Configuration
@EnableScheduling
public class PenjurnalanScheduler {

    @Autowired
    private SchedulerService schedulerService;

    @Scheduled(fixedRateString = "${scheduler}")
    public ResponseEntity<?> hitBackStatusZero() throws IOException {
        return schedulerService.hitBackFmsApi();
    }

}