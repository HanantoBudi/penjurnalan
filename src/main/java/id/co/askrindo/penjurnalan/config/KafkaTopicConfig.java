package id.co.askrindo.penjurnalan.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.produksiIjp}")
    private String topicProduksiIjp;

    @Value("${spring.kafka.topic.pelunasanIjp}")
    private String topicPelunasanIjp;

    @Value("${spring.kafka.topic.produksiKlaim}")
    private String topicProduksiKlaim;

    @Bean
    public NewTopic javaguidesTopicPelunasanIjp() {
        return TopicBuilder.name(topicPelunasanIjp)
            .build();
    }

    @Bean
    public NewTopic javaguidesTopicProduksiIjp() {
        return TopicBuilder.name(topicProduksiIjp)
            .build();
    }

    @Bean
    public NewTopic javaguidesTopicProduksiKlaim() {
        return TopicBuilder.name(topicProduksiKlaim)
            .build();
    }

}