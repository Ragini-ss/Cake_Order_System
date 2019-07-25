package com.cakemake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories
@SpringBootApplication
@EnableTransactionManagement
public class CakeMakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CakeMakeApplication.class, args);
	}


}