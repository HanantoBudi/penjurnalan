package id.co.askrindo.penjurnalan.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

//    @Value("${spring.kafka.topic-json.name}")
//    private String topicName;

//    private KafkaTemplate<String, CreateSor> kafkaTemplate;

//    public KafkaProducer(KafkaTemplate<String, CreateSor> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }

    // public void sendMessage(CreateSor data){
    //     Message<CreateSor> message = MessageBuilder
    //             .withPayload(data)
    //             .setHeader(KafkaHeaders.TOPIC, topicName)
    //             .build();

    //     LOGGER.info(String.format("Message sent : %s [start] -> %s", topicName, data.toString()));
    //     kafkaTemplate.send(message);
    //     LOGGER.info(String.format("Message sent : %s [end] -> %s", topicName, data.toString()));
    // }
    
}
