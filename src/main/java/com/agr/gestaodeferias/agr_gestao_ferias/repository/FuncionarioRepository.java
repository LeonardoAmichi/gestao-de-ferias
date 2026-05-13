package com.agr.gestaodeferias.agr_gestao_ferias.repository;

import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {
    // Métodos de consulta customizados podem ser adicionados aqui
    
    /**
     * Busca funcionário por CPF
     */
    java.util.Optional<Funcionario> findByCpf(String cpf);
} 