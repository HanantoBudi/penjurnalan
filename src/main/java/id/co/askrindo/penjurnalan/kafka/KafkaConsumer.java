package id.co.askrindo.penjurnalan.kafka;

import id.co.askrindo.penjurnalan.model.KoreksiIjp;
import id.co.askrindo.penjurnalan.model.KoreksiKlaim;
import id.co.askrindo.penjurnalan.model.ProduksiIjp;
import id.co.askrindo.penjurnalan.model.ProduksiKlaim;
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
     public void consumeKoreksiIjp(KoreksiIjp koreksiIjp){
         LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicKoreksiIjp, koreksiIjp));
         sendToService("KOREKSI IJP", "", koreksiIjp, null, null, null);
         LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicKoreksiIjp, koreksiIjp));
     }

    @KafkaListener(topics = "${spring.kafka.topic.koreksiKlaim}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKoreksiKlaim(KoreksiKlaim koreksiKlaim){
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicKoreksiKlaim, koreksiKlaim));
        sendToService("KOREKSI KLAIM", "", null, koreksiKlaim, null, null);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicKoreksiKlaim, koreksiKlaim));
    }

    @KafkaListener(topics = "${spring.kafka.topic.produksiIjp}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiIjp(ProduksiIjp produksiIjp){
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicProduksiIjp, produksiIjp));
        sendToService("PRODUKSI IJP", "", null, null, produksiIjp, null);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicProduksiIjp, produksiIjp));
    }

    @KafkaListener(topics = "${spring.kafka.topic.produksiKlaim}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiKlaim(ProduksiKlaim produksiKlaim){
        LOGGER.info(String.format("Json message recieved : %s [start] -> %s", topicProduksiKlaim, produksiKlaim));
        sendToService("PRODUKSI KLAIM", "", null, null, null, produksiKlaim);
        LOGGER.info(String.format("Json message recieved : %s [end] -> %s", topicProduksiKlaim, produksiKlaim));
    }

    public void sendToService(String topic, String message, KoreksiIjp koreksiIjp, KoreksiKlaim koreksiKlaim, ProduksiIjp produksiIjp, ProduksiKlaim produksiKlaim) {
        ResponseEntity<?> penjurnalan = penjurnalanService.penjurnalanProcess(topic, message, koreksiIjp, koreksiKlaim, produksiIjp, produksiKlaim);
    }
}
