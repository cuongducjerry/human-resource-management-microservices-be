package com.hrm.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class HrmRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmRegistryApplication.class, args);
	}

}
