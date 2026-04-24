package com.microshop.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.microshop.users", "com.microshop.rrhh"})
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
public class MicroshopUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroshopUsersApplication.class, args);
	}

}
