package com.agr.gestaodeferias.agr_gestao_ferias.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "csv")
@Data
public class CsvConfig {
    /**
     * Caminho do arquivo CSV para importação de funcionários
     */
    private String filePath;
}
