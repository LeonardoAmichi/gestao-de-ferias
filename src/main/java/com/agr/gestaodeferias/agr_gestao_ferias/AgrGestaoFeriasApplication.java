package com.agr.gestaodeferias.agr_gestao_ferias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgrGestaoFeriasApplication {
	public static void main(String[] args) {
		SpringApplication.run(AgrGestaoFeriasApplication.class, args);
	}

}
