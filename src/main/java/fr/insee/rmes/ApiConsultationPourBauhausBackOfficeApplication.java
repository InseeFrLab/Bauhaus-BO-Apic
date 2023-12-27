package fr.insee.rmes;

import fr.insee.rmes.config.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class ApiConsultationPourBauhausBackOfficeApplication {

	public static void main(String[] args) {
		configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
	}

	private static SpringApplicationBuilder configureApplicationBuilder(SpringApplicationBuilder springApplicationBuilder) {
		return springApplicationBuilder.sources(ApiConsultationPourBauhausBackOfficeApplication.class)
				.listeners(new PropertiesLogger());

	}

}
