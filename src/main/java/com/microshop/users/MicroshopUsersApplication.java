package com.microshop.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
public class MicroshopUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroshopUsersApplication.class, args);
	}

}
