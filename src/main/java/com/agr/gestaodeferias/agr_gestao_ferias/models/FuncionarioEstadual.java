package com.agr.gestaodeferias.agr_gestao_ferias.models;

import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode; 

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ESTADUAL")
public class FuncionarioEstadual extends Funcionario{

    public FuncionarioEstadual(){
        super();
    }

    public FuncionarioEstadual(String nome, String cpf, Regime regime, LocalDate dataAdmissao, LocalDate terminoPeriodoAquisitivo) {
        super(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
    }

    @Override
    public int getMinimoDiasPrimeiraParcela() {
        return 5;
    }
}
