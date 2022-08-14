package id.co.askrindo.penjurnalan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import id.co.askrindo.penjurnalan.kafka.KafkaProducer;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    
    @Autowired
    KafkaProducer kafkaProducer;

    // @PostMapping(value="/publish")
    // public void sendMessage(@RequestBody CreateSor createSor) {
    //     kafkaProducer.sendMessage(createSor);
    // }

}
