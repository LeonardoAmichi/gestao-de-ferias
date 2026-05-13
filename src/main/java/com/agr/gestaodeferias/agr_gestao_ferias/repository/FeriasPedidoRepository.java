package com.agr.gestaodeferias.agr_gestao_ferias.repository;

import com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeriasPedidoRepository extends JpaRepository<FeriasPedido, Long> {
    //Buscar todos os pedidos de férias de um funcionário
    List<FeriasPedido> findByFuncionarioId(UUID funcionarioId);
}
