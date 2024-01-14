package fr.insee;

import fr.insee.apic.config.ControllersConfiguration;
import fr.insee.apic.config.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ApiConsultation {

	public static void main(String[] args) {
		configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
	}

	private static SpringApplicationBuilder configureApplicationBuilder(SpringApplicationBuilder springApplicationBuilder) {
		return springApplicationBuilder.sources(ApiConsultation.class)
				.listeners(new PropertiesLogger(), new ControllersConfiguration());
	}

}
