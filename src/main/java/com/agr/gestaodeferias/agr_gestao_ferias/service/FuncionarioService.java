package com.agr.gestaodeferias.agr_gestao_ferias.service;

import com.agr.gestaodeferias.agr_gestao_ferias.dto.FuncionarioDTO;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioClt;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioEstadual;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioTemporario;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;

    public Funcionario fromDTO(FuncionarioDTO dto) {
        Funcionario funcionario;
        switch (dto.getRegime()) {
            case CLT:
                funcionario = new FuncionarioClt();
                break;
            case TEMPORARIO:
                funcionario = new FuncionarioTemporario();
                break;
            case ESTADUAL:
                funcionario = new FuncionarioEstadual();
                break;
            default:
                throw new IllegalArgumentException("Regime inválido");
        }
        funcionario.setNome(dto.getNome());
        funcionario.setCpf(dto.getCpf());
        funcionario.setRegime(dto.getRegime());
        funcionario.setDataAdmissao(dto.getDataAdmissao());
        funcionario.setTerminoPeriodoAquisitivo(dto.getTerminoPeriodoAquisitivo());
        return funcionario;
    }

    public FuncionarioDTO toDTO(Funcionario funcionario) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setNome(funcionario.getNome());
        dto.setCpf(funcionario.getCpf());
        dto.setRegime(funcionario.getRegime());
        dto.setDataAdmissao(funcionario.getDataAdmissao());
        dto.setTerminoPeriodoAquisitivo(funcionario.getTerminoPeriodoAquisitivo());
        return dto;
    }

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public Optional<Funcionario> buscarPorId(UUID id) {
        return funcionarioRepository.findById(id);
    }

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }
    
    public int importarFuncionariosCSV(MultipartFile arquivo) {
        // Implementação básica - retorna 0 para não quebrar o código
        return 0;
    }
}