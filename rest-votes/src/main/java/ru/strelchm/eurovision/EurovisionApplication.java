package ru.strelchm.eurovision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EurovisionApplication {
	public static void main(String[] args) {
		SpringApplication.run(EurovisionApplication.class, args);
	}
}
