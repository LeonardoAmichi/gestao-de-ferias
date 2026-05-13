package com.agr.gestaodeferias.agr_gestao_ferias.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agr.gestaodeferias.agr_gestao_ferias.dto.FeriasPedidoDTO;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FeriasService;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FuncionarioExternoService;

@RestController
@RequestMapping("/ferias-pedidos")
public class FeriasPedidoController {
    private final FeriasService feriasService;
    private final FuncionarioExternoService funcionarioExternoService;

    public FeriasPedidoController(FeriasService feriasService, FuncionarioExternoService funcionarioExternoService) {
        this.feriasService = feriasService;
        this.funcionarioExternoService = funcionarioExternoService;
    }

    @PostMapping
    public ResponseEntity<?> criarPedido(@RequestBody FeriasPedidoDTO dto) {
        try {
            if (dto.getFuncionarioId() == null) {
                return ResponseEntity.badRequest().body("ID do funcionário é obrigatório.");
            }
            Optional<Funcionario> funcionario = funcionarioExternoService.buscarFuncionarioPorId(dto.getFuncionarioId());
            if (funcionario.isEmpty()) {
                return ResponseEntity.status(404).body("Funcionário não encontrado.");
            }
            FeriasPedido pedido = feriasService.fromDTO(dto, funcionario.get());
            FeriasPedido salvo = feriasService.solicitarFerias(pedido);
            FeriasPedidoDTO resposta = feriasService.toDTO(salvo);
            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/funcionario/{cpf}")
    public ResponseEntity<?> listarPedidosPorFuncionario(@PathVariable String cpf) {
        Optional<Funcionario> funcionario = funcionarioExternoService.buscarFuncionarioPorCPF(cpf);
        if (funcionario.isEmpty()) {
            return ResponseEntity.status(404).body("Funcionário não encontrado.");
        }
        List<FeriasPedido> pedidos = feriasService.listarFeriasPorFuncionario(funcionario.get().getId());
        List<FeriasPedidoDTO> dtos = pedidos.stream().map(feriasService::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/funcionario/id/{id}")
    public ResponseEntity<?> listarPedidosPorFuncionarioId(@PathVariable UUID id) {
        Optional<Funcionario> funcionario = funcionarioExternoService.buscarFuncionarioPorId(id);
        if (funcionario.isEmpty()) {
            return ResponseEntity.status(404).body("Funcionário não encontrado.");
        }
        List<FeriasPedido> pedidos = feriasService.listarFeriasPorFuncionario(id);
        List<FeriasPedidoDTO> dtos = pedidos.stream().map(feriasService::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    public ResponseEntity<List<FeriasPedidoDTO>> listarTodos() {
        List<FeriasPedido> pedidos = feriasService.listarTodos();
        List<FeriasPedidoDTO> dtos = pedidos.stream().map(feriasService::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }
}
