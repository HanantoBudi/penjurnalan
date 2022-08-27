package id.co.askrindo.penjurnalan.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.co.askrindo.penjurnalan.service.PenjurnalanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @Value("${spring.kafka.topic.produksiIjp}")
    private String topicProduksiIjp;

    @Value("${spring.kafka.topic.pelunasanIjp}")
    private String topicPelunasanIjp;

    @Value("${spring.kafka.topic.produksiKlaim}")
    private String topicProduksiKlaim;

    @Autowired
    private PenjurnalanService penjurnalanService;

    @KafkaListener(topics = "${spring.kafka.topic.produksiIjp}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiIjp(String tIjpProjectedId) throws JsonProcessingException {
        LOGGER.info(String.format("Json message received : %s [start] -> %s", topicProduksiIjp, tIjpProjectedId));
        sendToService("PRODUKSI IJP", tIjpProjectedId);
        LOGGER.info(String.format("Json message received : %s [end] -> %s", topicProduksiIjp, tIjpProjectedId));
    }

    @KafkaListener(topics = "${spring.kafka.topic.pelunasanIjp}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePelunasanIjp(String tIjpProjectedId) throws JsonProcessingException {
        LOGGER.info(String.format("Json message received : %s [start] -> %s", topicPelunasanIjp, tIjpProjectedId));
        sendToService("PELUNASAN IJP", tIjpProjectedId);
        LOGGER.info(String.format("Json message received : %s [end] -> %s", topicPelunasanIjp, tIjpProjectedId));
    }

    @KafkaListener(topics = "${spring.kafka.topic.produksiKlaim}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProduksiKlaim(String klaimId) throws JsonProcessingException {
        LOGGER.info(String.format("Json message received : %s [start] -> %s", topicProduksiKlaim, klaimId));
        sendToService("PRODUKSI KLAIM", klaimId);
        LOGGER.info(String.format("Json message received : %s [end] -> %s", topicProduksiKlaim, klaimId));
    }

    public void sendToService(String topic, String uuid){
        penjurnalanService.penjurnalanProcess(topic, uuid);
    }
}
