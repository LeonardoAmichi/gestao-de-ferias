package com.agr.gestaodeferias.agr_gestao_ferias.dto;

import java.time.LocalDate;

import com.agr.gestaodeferias.agr_gestao_ferias.models.Regime;

import lombok.Data;

@Data
public class FuncionarioDTO {
    private String nome;
    private String cpf;
    private Regime regime; //CLT, ESTADUAL, TEMPORARIO
    private LocalDate dataAdmissao;
    private LocalDate terminoPeriodoAquisitivo;
    private int numeroDeDiasDeFeriasAcumulados;
    private int diasDisponiveisAnoAtual;
    private int parcelasUsadasNoCicloAnual;
}
