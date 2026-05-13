package com.agr.gestaodeferias.agr_gestao_ferias.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data 
public class FeriasPedidoDTO {
    private UUID funcionarioId;
    private int parcelaNumero;
    private int quantidadeDias;
    private LocalDate dataInicio; // Opcional - se não informada, usa a data atual
    private LocalDate dataFim;
    private String nomeFuncionario;
    private String status;
}
