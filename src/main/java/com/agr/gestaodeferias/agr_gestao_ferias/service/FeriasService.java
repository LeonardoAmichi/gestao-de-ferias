package com.agr.gestaodeferias.agr_gestao_ferias.service;

import com.agr.gestaodeferias.agr_gestao_ferias.dto.FeriasPedidoDTO;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FeriasPedidoRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class FeriasService {
    private final FeriasPedidoRepository feriasPedidoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public FeriasService(FeriasPedidoRepository feriasPedidoRepository, FuncionarioRepository funcionarioRepository) {
        this.feriasPedidoRepository = feriasPedidoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    public FeriasPedido fromDTO(FeriasPedidoDTO dto, Funcionario funcionario) {
        FeriasPedido pedido = new FeriasPedido();
        pedido.setFuncionario(funcionario); 
        pedido.setParcelaNumero(dto.getParcelaNumero());
        pedido.setQuantidadeDias(dto.getQuantidadeDias());
        
        // Determina a data de início das férias
        LocalDate dataInicio = determinarDataInicioFerias(dto.getDataInicio());
        pedido.setDataInicio(dataInicio);
        pedido.setDataFim(dataInicio.plusDays(dto.getQuantidadeDias() - 1));
        // Preenche o CPF do funcionário
        pedido.setCpfFuncionario(funcionario.getCpf());
        return pedido;
    }

    public FeriasPedidoDTO toDTO(FeriasPedido pedido) {
        FeriasPedidoDTO dto = new FeriasPedidoDTO();
        dto.setFuncionarioId(pedido.getFuncionario().getId());
        dto.setParcelaNumero(pedido.getParcelaNumero());
        dto.setQuantidadeDias(pedido.getQuantidadeDias());
        dto.setDataInicio(pedido.getDataInicio());
        dto.setDataFim(pedido.getDataFim());
        dto.setNomeFuncionario(pedido.getFuncionario().getNome());
        // Lógica de status
        LocalDate hoje = LocalDate.now();
        if (pedido.getDataFim().isBefore(hoje)) {
            dto.setStatus("Finalizado");
        } else if (!hoje.isBefore(pedido.getDataInicio()) && !hoje.isAfter(pedido.getDataFim())) {
            dto.setStatus("Em andamento");
        } else if (hoje.isBefore(pedido.getDataInicio())) {
            dto.setStatus("Não iniciada");
        }
        return dto;
    }

    public FeriasPedido solicitarFerias(FeriasPedido pedido) {
        validarPedido(pedido);
        
        // As validações agora consideram o estado no banco, mas como estamos prestes a salvar,
        // vamos recalcular no próximo passo.

        
        // Salva o pedido de férias
        FeriasPedido pedidoSalvo = feriasPedidoRepository.save(pedido);
        
        // Atualiza o saldo de férias acumulados
        atualizarDiasDeFeriasAcumulados(pedido.getFuncionario());
        
        // Salva novamente o funcionário com saldo atualizado
        funcionarioRepository.save(pedido.getFuncionario());
        
        return pedidoSalvo;
    }

    public List<FeriasPedido> listarFeriasPorFuncionario(UUID funcionarioId) {
        return feriasPedidoRepository.findByFuncionarioId(funcionarioId);
    }
    
    /**
     * Atualiza o número de dias de férias acumulados do funcionário considerando o saldo de cada ano
     */
    public void atualizarDiasDeFeriasAcumulados(Funcionario funcionario) {
        if (funcionario.getDataAdmissao() == null) return;
        LocalDate admissao = funcionario.getDataAdmissao();
        LocalDate hoje = LocalDate.now();
        List<FeriasPedido> pedidos = feriasPedidoRepository.findByFuncionarioId(funcionario.getId());

        // Se não completou 1 ano de empresa, não tem direito a férias acumuladas ainda
        if (hoje.isBefore(admissao.plusYears(1))) {
            funcionario.setDiasDisponiveisAnoAtual(0);
            funcionario.setNumeroDeDiasDeFeriasAcumulados(0);
            funcionario.setNumeroDeParcelasUsadas(0);
            funcionario.setParcelasDisponiveis(Funcionario.MAXIMO_PARCELA_FERIAS);
            return;
        }

        // Calcula saldo histórico (Acumulados totais)
        long aniversariosCompletos = ChronoUnit.YEARS.between(admissao, hoje);
        int saldoBruto = (int) Math.min(aniversariosCompletos * Funcionario.MAXIMO_DIAS_FERIAS_ANO, 90);
        int diasGozadosTotal = pedidos.stream().mapToInt(FeriasPedido::getQuantidadeDias).sum();
        funcionario.setNumeroDeDiasDeFeriasAcumulados(Math.max(0, saldoBruto - diasGozadosTotal));

        // Calcula os limites dentro do CICLO AQUISITIVO VIGENTE
        LocalDate inicioCiclo = funcionario.getInicioCicloVigente(hoje);
        LocalDate fimCiclo = funcionario.getFimCicloVigente(hoje);

        List<FeriasPedido> pedidosNoCiclo = pedidos.stream()
            .filter(p -> p.getDataInicio() != null && !p.getDataInicio().isBefore(inicioCiclo) && !p.getDataInicio().isAfter(fimCiclo))
            .toList();

        int diasGozadosNoCiclo = pedidosNoCiclo.stream().mapToInt(FeriasPedido::getQuantidadeDias).sum();
        funcionario.setDiasDisponiveisAnoAtual(Math.max(0, Funcionario.MAXIMO_DIAS_FERIAS_ANO - diasGozadosNoCiclo));

        long parcelasNoCiclo = pedidosNoCiclo.stream().mapToInt(FeriasPedido::getParcelaNumero).distinct().count();
        funcionario.setNumeroDeParcelasUsadas((int) parcelasNoCiclo);
        funcionario.setParcelasDisponiveis(Funcionario.MAXIMO_PARCELA_FERIAS - (int) parcelasNoCiclo);
    }

    private void validarPedido(FeriasPedido pedido) {
        Funcionario funcionario = pedido.getFuncionario();
        // Atualiza a contagem dinâmica antes de validar
        atualizarDiasDeFeriasAcumulados(funcionario);

        if (!funcionario.temParcelasDisponiveis()) {
            throw new IllegalArgumentException("Funcionário não possui mais parcelas disponíveis neste ciclo");
        }
        
        List<FeriasPedido> pedidosFuncionario = listarFeriasPorFuncionario(funcionario.getId());
        
        LocalDate dataRef = pedido.getDataInicio() != null ? pedido.getDataInicio() : LocalDate.now();
        LocalDate inicioCiclo = funcionario.getInicioCicloVigente(dataRef);
        LocalDate fimCiclo = funcionario.getFimCicloVigente(dataRef);
        
        List<FeriasPedido> pedidosNoCiclo = pedidosFuncionario.stream()
            .filter(p -> p.getDataInicio() != null && !p.getDataInicio().isBefore(inicioCiclo) && !p.getDataInicio().isAfter(fimCiclo))
            .toList();

        int diasGozadosNoCiclo = pedidosNoCiclo.stream().mapToInt(FeriasPedido::getQuantidadeDias).sum();
        
        if (diasGozadosNoCiclo + pedido.getQuantidadeDias() > Funcionario.MAXIMO_DIAS_FERIAS_ANO) {
            throw new IllegalArgumentException("Total de dias de férias no ciclo anual não pode ultrapassar " + Funcionario.MAXIMO_DIAS_FERIAS_ANO + ".");
        }

        // Validação da primeira parcela
        if (pedido.getParcelaNumero() == 1) {
            int minimo = funcionario.getMinimoDiasPrimeiraParcela();
            if (pedido.getQuantidadeDias() < minimo) {
                throw new IllegalArgumentException("A primeira parcela deve ter no mínimo " + minimo + " dias.");
            }
        }
        
        long parcelasNoCiclo = pedidosNoCiclo.stream().mapToInt(FeriasPedido::getParcelaNumero).distinct().count();
        if (parcelasNoCiclo >= Funcionario.MAXIMO_PARCELA_FERIAS) {
            throw new IllegalArgumentException("O funcionário já atingiu o limite de " + Funcionario.MAXIMO_PARCELA_FERIAS + " parcelas de férias no ciclo anual vigente.");
        }
        
        // Validação do número da parcela
        if (pedido.getParcelaNumero() < 1 || pedido.getParcelaNumero() > Funcionario.MAXIMO_PARCELA_FERIAS) {
            throw new IllegalArgumentException("O número da parcela deve ser entre 1 e " + Funcionario.MAXIMO_PARCELA_FERIAS + ".");
        }
    }
    
    /**
     * Determina a data de início das férias
     * Se a data for nula, usa a data atual
     */
    private LocalDate determinarDataInicioFerias(LocalDate dataInicio) {
        if (dataInicio == null) {
            System.out.println("Data de início das férias não informada, usando data atual");
            return LocalDate.now();
        }
        
        System.out.println("Data de início das férias definida: " + dataInicio);
        return dataInicio;
    }

    public List<FeriasPedido> listarTodos() {
        return feriasPedidoRepository.findAll();
    }
} 