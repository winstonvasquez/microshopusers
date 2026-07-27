package com.microshop.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.microshop.users", "com.microshop.rrhh"})
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy
public class MicroshopUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroshopUsersApplication.class, args);
	}

}
