package com.agr.gestaodeferias.agr_gestao_ferias.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.agr.gestaodeferias.agr_gestao_ferias.dto.FuncionarioDTO;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Regime;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FuncionarioExternoService;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FuncionarioService;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FileWatcherService;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FeriasPedidoRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FuncionarioRepository;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {
    private final FuncionarioExternoService funcionarioExternoService;
    private final FuncionarioService funcionarioService;
    private final FileWatcherService fileWatcherService;
    private final FeriasPedidoRepository feriasPedidoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioController(FuncionarioExternoService funcionarioExternoService, 
                                FuncionarioService funcionarioService,
                                FileWatcherService fileWatcherService,
                                FeriasPedidoRepository feriasPedidoRepository,
                                FuncionarioRepository funcionarioRepository) {
        this.funcionarioExternoService = funcionarioExternoService;
        this.funcionarioService = funcionarioService;
        this.fileWatcherService = fileWatcherService;
        this.feriasPedidoRepository = feriasPedidoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> listarTodos() {
        List<FuncionarioDTO> lista = funcionarioExternoService.buscarTodosFuncionarios()
            .stream()
            .map(funcionarioExternoService::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
    
    @GetMapping("/{cpf}")
    public ResponseEntity<FuncionarioDTO> buscarPorCPF(@PathVariable String cpf) {
        Optional<Funcionario> funcionario = funcionarioExternoService.buscarFuncionarioPorCPF(cpf);
        if (funcionario.isPresent()) {
            FuncionarioDTO dto = funcionarioExternoService.toDTO(funcionario.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/regime/{regime}")
    public ResponseEntity<List<FuncionarioDTO>> buscarPorRegime(@PathVariable String regime) {
        try {
            Regime regimeEnum = Regime.valueOf(regime.toUpperCase());
            List<FuncionarioDTO> lista = funcionarioExternoService.buscarFuncionariosPorTipo(regimeEnum)
                .stream()
                .map(funcionarioExternoService::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/estatisticas")
    public ResponseEntity<String> obterEstatisticas() {
        List<Funcionario> funcionarios = funcionarioExternoService.buscarTodosFuncionarios();
        
        long totalCLT = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.CLT).size();
        long totalTemporario = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.TEMPORARIO).size();
        long totalEstadual = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.ESTADUAL).size();
        
        String estatisticas = String.format(
            "Total de funcionários: %d\n" +
            "CLT: %d\n" +
            "Temporário: %d\n" +
            "Estadual: %d",
            funcionarios.size(), totalCLT, totalTemporario, totalEstadual
        );
        
        return ResponseEntity.ok(estatisticas);
    }
    
    @PostMapping("/importar-csv")
    public ResponseEntity<String> importarCSV(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            int funcionariosImportados = funcionarioService.importarFuncionariosCSV(arquivo);
            return ResponseEntity.ok("Importação realizada com sucesso! " + funcionariosImportados + " funcionários importados.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro na importação: " + e.getMessage());
        }
    }
    
    @PostMapping("/carregar-csv")
    public ResponseEntity<String> carregarCSV() {
        try {
            funcionarioExternoService.carregarFuncionariosDoCSV();
            return ResponseEntity.ok("Funcionários carregados com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar funcionários: " + e.getMessage());
        }
    }
    
    @PostMapping("/recarregar")
    public ResponseEntity<String> recarregarFuncionarios() {
        try {
            fileWatcherService.forcarRecarregamento();
            List<Funcionario> funcionarios = funcionarioExternoService.buscarTodosFuncionarios();
            return ResponseEntity.ok("Recarregamento forçado concluído! " + funcionarios.size() + " funcionários carregados.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro no recarregamento: " + e.getMessage());
        }
    }
    
    @PostMapping("/configurar-caminho")
    public ResponseEntity<String> configurarCaminhoCSV(@RequestParam("caminho") String novoCaminho) {
        try {
            fileWatcherService.setCaminhoArquivo(novoCaminho);
            return ResponseEntity.ok("Caminho do CSV configurado: " + novoCaminho);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao configurar caminho: " + e.getMessage());
        }
    }
    
    @PostMapping("/limpar-duplicatas")
    public ResponseEntity<String> limparDuplicatas() {
        try {
            List<Funcionario> funcionariosExistentes = funcionarioExternoService.buscarTodosFuncionarios();
            Set<String> cpfsProcessados = new HashSet<>();
            List<Funcionario> funcionariosParaRemover = new ArrayList<>();
            
            for (Funcionario funcionario : funcionariosExistentes) {
                if (cpfsProcessados.contains(funcionario.getCpf())) {
                    funcionariosParaRemover.add(funcionario);
                } else {
                    cpfsProcessados.add(funcionario.getCpf());
                }
            }
            
            if (!funcionariosParaRemover.isEmpty()) {
                // Remove todos os funcionários duplicados de uma vez
                funcionarioRepository.deleteAll(funcionariosParaRemover);
                
                return ResponseEntity.ok("Limpeza concluída! " + funcionariosParaRemover.size() + 
                                       " funcionários duplicados removidos. " + 
                                       (funcionariosExistentes.size() - funcionariosParaRemover.size()) + 
                                       " funcionários únicos mantidos.");
            } else {
                return ResponseEntity.ok("Nenhuma duplicata encontrada. " + funcionariosExistentes.size() + " funcionários únicos.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao limpar duplicatas: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<String> obterStatus() {
        try {
            List<Funcionario> funcionarios = funcionarioExternoService.buscarTodosFuncionarios();
            long totalCLT = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.CLT).size();
            long totalTemporario = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.TEMPORARIO).size();
            long totalEstadual = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.ESTADUAL).size();
            
            // Calcula estatísticas de parcelas
            int totalParcelasUsadas = funcionarios.stream().mapToInt(Funcionario::getNumeroDeParcelasUsadas).sum();
            int totalParcelasDisponiveis = funcionarios.stream().mapToInt(Funcionario::getParcelasDisponiveis).sum();
            
            String status = String.format(
                "Status do Sistema:\n" +
                "Funcionários carregados: %d\n" +
                "CLT: %d\n" +
                "Temporário: %d\n" +
                "Estadual: %d\n" +
                "Parcelas usadas: %d\n" +
                "Parcelas disponíveis: %d\n" +
                "Monitoramento automático: ATIVO\n" +
                "Verificação a cada 30 segundos",
                funcionarios.size(), totalCLT, totalTemporario, totalEstadual, 
                totalParcelasUsadas, totalParcelasDisponiveis
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter status: " + e.getMessage());
        }
    }
    
    @GetMapping("/pedidos-ferias")
    public ResponseEntity<String> obterPedidosFerias() {
        try {
            List<com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido> pedidos = feriasPedidoRepository.findAll();
            
            StringBuilder resultado = new StringBuilder();
            resultado.append("Pedidos de Férias Criados Automaticamente:\n\n");
            
            if (pedidos.isEmpty()) {
                resultado.append("Nenhum pedido de férias encontrado.\n");
            } else {
                resultado.append("Total de pedidos: ").append(pedidos.size()).append("\n\n");
                
                for (com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido pedido : pedidos) {
                    resultado.append("Funcionário: ").append(pedido.getFuncionario().getNome()).append("\n");
                    resultado.append("Parcela: ").append(pedido.getParcelaNumero()).append("\n");
                    resultado.append("Dias: ").append(pedido.getQuantidadeDias()).append("\n");
                    resultado.append("Data Início: ").append(pedido.getDataInicio()).append("\n");
                    resultado.append("Data Fim: ").append(pedido.getDataFim()).append("\n");
                }
            }
            
            return ResponseEntity.ok(resultado.toString());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter pedidos de férias: " + e.getMessage());
        }
    }
    
    @GetMapping("/{cpf}/parcelas")
    public ResponseEntity<String> obterParcelasFuncionario(@PathVariable String cpf) {
        try {
            Optional<Funcionario> funcionarioOpt = funcionarioExternoService.buscarFuncionarioPorCPF(cpf);
            
            if (funcionarioOpt.isPresent()) {
                Funcionario funcionario = funcionarioOpt.get();
                
                String parcelas = String.format(
                    "Parcelas do Funcionário:\n" +
                    "Nome: %s\n" +
                    "CPF: %s\n" +
                    "Regime: %s\n" +
                    "Parcelas usadas: %d\n" +
                    "Parcelas disponíveis: %d\n" +
                    "Total de parcelas: %d",
                    funcionario.getNome(),
                    funcionario.getCpf(),
                    funcionario.getRegime(),
                    funcionario.getNumeroDeParcelasUsadas(),
                    funcionario.getParcelasDisponiveis(),
                    funcionario.getNumeroDeParcelasUsadas() + funcionario.getParcelasDisponiveis()
                );
                
                return ResponseEntity.ok(parcelas);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter parcelas do funcionário: " + e.getMessage());
        }
    }
    
    @GetMapping("/regras-negocio")
    public ResponseEntity<String> obterRegrasNegocio() {
        try {
            String regras = String.format(
                "Regras de Negócio - Sistema de Férias:\n\n" +
                "1. LIMITE DE PARCELAS:\n" +
                "   - Máximo de 3 parcelas por ano\n" +
                "   - Máximo de 30 dias de férias por ano\n\n" +
                "2. MÍNIMO DE DIAS POR TIPO DE FUNCIONÁRIO:\n" +
                "   - CLT: Mínimo 15 dias na primeira parcela\n" +
                "   - Temporário: Mínimo 5 dias na primeira parcela\n" +
                "   - Estadual: Mínimo 5 dias na primeira parcela\n\n" +
                "3. CONTROLE DE PARCELAS:\n" +
                "   - Cada pedido de férias incrementa numeroDeParcelasUsadas\n" +
                "   - Cada pedido de férias decrementa parcelasDisponiveis\n" +
                "   - Total sempre deve ser 3 (usadas + disponíveis)\n\n" +
                "4. VALIDAÇÕES AUTOMÁTICAS:\n" +
                "   - Verifica se funcionário tem parcelas disponíveis\n" +
                "   - Verifica se respeita mínimo de dias por tipo\n" +
                "   - Verifica se não ultrapassa 30 dias totais\n" +
                "   - Verifica se não ultrapassa 3 parcelas\n\n" +
                "5. CARREGAMENTO CSV:\n" +
                "   - Cria pedidos automaticamente baseado na coluna 'dias de gozo'\n" +
                "   - Usa data da coluna 'INICIO DAS FÉRIAS' se disponível\n" +
                "   - Aplica todas as validações de negócio\n" +
                "   - Incrementa parcelas usadas automaticamente"
            );
            
            return ResponseEntity.ok(regras);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter regras de negócio: " + e.getMessage());
        }
    }
}
