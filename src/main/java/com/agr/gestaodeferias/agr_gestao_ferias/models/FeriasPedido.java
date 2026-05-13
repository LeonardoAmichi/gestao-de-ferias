package com.agr.gestaodeferias.agr_gestao_ferias.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Data
@Entity
public class FeriasPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;
    
    private String nomeFuncionario; // Nome do funcionário para facilitar consultas
    private int parcelaNumero; // 1, 2 ou 3
    private int quantidadeDias; //Dias de Gozo
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String cpfFuncionario;

    public FeriasPedido() {
    }

    public FeriasPedido(Long id, Funcionario funcionario, int parcelaNumero, int quantidadeDias,
            LocalDate dataInicio, LocalDate dataFim, String cpfFuncionario) {
        this.id = id;
        this.funcionario = funcionario;
        this.nomeFuncionario = funcionario.getNome(); // Armazena o nome do funcionário
        this.parcelaNumero = parcelaNumero;
        this.quantidadeDias = quantidadeDias;
        this.dataInicio = dataInicio;
        this.dataFim = dataInicio.plusDays(quantidadeDias);
        this.cpfFuncionario = cpfFuncionario;
    }

    public String getCpfFuncionario() {
        return cpfFuncionario;
    }
    public void setCpfFuncionario(String cpfFuncionario) {
        this.cpfFuncionario = cpfFuncionario;
    }
}
