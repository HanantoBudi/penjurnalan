package id.co.askrindo.penjurnalan.controller;

import id.co.askrindo.penjurnalan.service.PenjurnalanService;
import id.co.askrindo.penjurnalan.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "/penjurnalan")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private PenjurnalanService penjurnalanService;

    @PostMapping("/scheduler")
    public ResponseEntity<?> create() throws IOException {
        return schedulerService.hitBackFmsApi();
    }

    @PostMapping("/penjurnalan")
    public ResponseEntity<?> penjurnalanCreate(@RequestParam String topic, @RequestParam String message) throws IOException {
        return penjurnalanService.penjurnalanProcess(topic, message);
    }
}
