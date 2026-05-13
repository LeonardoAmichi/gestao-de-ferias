package com.agr.gestaodeferias.agr_gestao_ferias.models;

import java.time.LocalDate;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode; 

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("CLT")
public class FuncionarioClt extends Funcionario {
    
    public FuncionarioClt(){
        super();
    }

    public FuncionarioClt(String nome, String cpf, Regime regime, LocalDate dataAdmissao, LocalDate terminoPeriodoAquisitivo) {
        super(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
    }

    @Override
    public int getMinimoDiasPrimeiraParcela() {
        return 15;
    }
}
