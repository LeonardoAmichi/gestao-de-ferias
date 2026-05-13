package com.agr.gestaodeferias.agr_gestao_ferias.models;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_vinculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Funcionario {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    public static final int MAXIMO_DIAS_FERIAS_ANO = 30;
    public static final int MAXIMO_PARCELA_FERIAS = 3;
   
    private String nome;
    private String cpf;
    private int numeroDeDiasDeFeriasAcumulados;
    private int diasDisponiveisAnoAtual = MAXIMO_DIAS_FERIAS_ANO;

    @Enumerated(EnumType.STRING)
    private Regime regime;
    
    private LocalDate dataAdmissao; //Início do período aquisitivo de férias
    private LocalDate terminoPeriodoAquisitivo; //Fim do período aquisitivo de férias
    
    // Controle dinâmico de parcelas no ciclo
    private int parcelasDisponiveis = MAXIMO_PARCELA_FERIAS;
    private int numeroDeParcelasUsadas = 0;

    public Funcionario(String nome, String cpf, Regime regime, LocalDate dataAdmissao, LocalDate terminoPeriodoAquisitivo) {
        this.nome = nome;
        this.cpf = cpf;
        this.regime = regime;
        this.dataAdmissao = dataAdmissao;
        this.terminoPeriodoAquisitivo = terminoPeriodoAquisitivo;
    }

    public abstract int getMinimoDiasPrimeiraParcela();
    
    // Verifica se o funcionário ainda tem parcelas disponíveis no ciclo vigente
    public boolean temParcelasDisponiveis() {
        return this.parcelasDisponiveis > 0;
    }
    
    /**
     * Calcula o início do ciclo aquisitivo vigente com base numa data de referência
     */
    public LocalDate getInicioCicloVigente(LocalDate referencia) {
        if (dataAdmissao == null) return referencia;
        LocalDate aniversario = dataAdmissao.withYear(referencia.getYear());
        if (aniversario.isAfter(referencia)) {
            aniversario = aniversario.minusYears(1);
        }
        return aniversario;
    }
    
    /**
     * Calcula o fim do ciclo aquisitivo vigente com base numa data de referência
     */
    public LocalDate getFimCicloVigente(LocalDate referencia) {
        return getInicioCicloVigente(referencia).plusYears(1).minusDays(1);
    }
    
    /**
     * Verifica se o funcionário está no período aquisitivo
     */
    public boolean estaNoPeriodoAquisitivo() {
        LocalDate hoje = LocalDate.now();
        return hoje.isAfter(dataAdmissao) && hoje.isBefore(terminoPeriodoAquisitivo.plusDays(1));
    }
    
    /**
     * Verifica se o período aquisitivo já venceu
     */
    public boolean periodoAquisitivoVencido() {
        LocalDate hoje = LocalDate.now();
        return hoje.isAfter(terminoPeriodoAquisitivo);
    }
}
