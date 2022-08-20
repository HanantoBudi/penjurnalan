package id.co.askrindo.penjurnalan.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import id.co.askrindo.penjurnalan.model.Journal;
import id.co.askrindo.penjurnalan.service.PenjurnalanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @Value("${spring.kafka.topic.koreksiIjp}")
    private String topicKoreksiIjp;

    @Value("${spring.kafka.topic.koreksiKlaim}")
    private String topicKoreksiKlaim;

    @Value("${spring.kafka.topic.produksiIjp}")
    private String topicProduksiIjp;

    @Value("${spring.kafka.topic.produksiKlain}")
    private String topicProduksiKlaim;

    @Autowired
    private PenjurnalanService penjurnalanService;

     @KafkaListener(topics = "${spring.kafka.topic.koreksiIjp}", groupId = "${spring.kafka.consumer.group-id}")
     public void consumeKoreksiIjp(Journal journal) throws JsonProcessingException {
         LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicKoreksiIjp, journal));
         sendToService("KOREKSI IJP", journal);
         LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicKoreksiIjp, journal));
     }

    @KafkaListener(topics = "${spring.kafka.topic.koreksiKlaim}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKoreksiKlaim(Journal journal) throws JsonProcessingException {
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicKoreksiKlaim, journal));
        sendToService("KOREKSI KLAIM", journal);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicKoreksiKlaim, journal));
    }

    @KafkaListener(topics = "${spring.kafka.topic.produksiIjp}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiIjp(Journal journal) throws JsonProcessingException {
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicProduksiIjp, journal));
        sendToService("PRODUKSI IJP", journal);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicProduksiIjp, journal));
    }

    @KafkaListener(topics = "${spring.kafka.topic.produksiKlaim}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiKlaim(Journal journal) throws JsonProcessingException {
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicProduksiKlaim, journal));
        sendToService("PRODUKSI KLAIM", journal);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicProduksiKlaim, journal));
    }

    public void sendToService(String topic, Journal journal) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String message = ow.writeValueAsString(journal);
        ResponseEntity<?> penjurnalan = penjurnalanService.penjurnalanProcess(topic, message, journal);
    }
}
