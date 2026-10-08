package fr.hm.tarificateur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"fr.hm.tarificateur.adapters", "fr.hm.tarificateur.domain", "fr.hm.tarificateur.ports"})
@EnableJpaRepositories(basePackages = "fr.hm.tarificateur.adapters.outbound.database")
@EntityScan(basePackages = "fr.hm.tarificateur.adapters.outbound.database")
public class TarificateurApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarificateurApplication.class, args);
	}

}

