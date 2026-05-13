package com.agr.gestaodeferias.agr_gestao_ferias.models;

import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode; 

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("TEMPORARIO")
public class FuncionarioTemporario extends Funcionario{

    public FuncionarioTemporario(){
        super();
    }

    public FuncionarioTemporario(String nome, String cpf, Regime regime, LocalDate dataAdmissao, LocalDate terminoPeriodoAquisitivo) {
        super(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
    }

    @Override
    public int getMinimoDiasPrimeiraParcela() {
        return 5;
    }
}
