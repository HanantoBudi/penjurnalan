package id.co.askrindo.penjurnalan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class PenjurnalanApplication {

	public static void main(String[] args) {
		SpringApplication.run(PenjurnalanApplication.class, args);
	}

}
