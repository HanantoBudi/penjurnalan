package id.co.askrindo.penjurnalan.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

//    @Value("${spring.kafka.topic.koreksiIjp}")
//    private String topicKoreksiIjp;
//
//    @Value("${spring.kafka.topic.koreksiKlaim}")
//    private String topicKoreksiKlaim;
//
//    @Value("${spring.kafka.topic.produksiIjp}")
//    private String topicProduksiIjp;
//
//    @Value("${spring.kafka.topic.produksiKlain}")
//    private String topicProduksiKlaim;

//    @Bean
//    public NewTopic javaguidesTopicKoreksiIjp() {
//        return TopicBuilder.name(topicKoreksiIjp)
//            .build();
//    }
//
//    @Bean
//    public NewTopic javaguidesTopicKoreksiKlain() {
//        return TopicBuilder.name(topicKoreksiKlaim)
//            .build();
//    }
//
//    @Bean
//    public NewTopic javaguidesTopicProduksiIjp() {
//        return TopicBuilder.name(topicProduksiIjp)
//            .build();
//    }
//
//    @Bean
//    public NewTopic javaguidesTopicProduksiKlain() {
//        return TopicBuilder.name(topicProduksiKlaim)
//            .build();
//    }

}