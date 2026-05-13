package com.agr.gestaodeferias.agr_gestao_ferias.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import com.agr.gestaodeferias.agr_gestao_ferias.config.CsvConfig;
/**
 * Serviço para monitorar mudanças no arquivo CSV e recarregar automaticamente
 */
@Service
public class FileWatcherService {
    
    private final FuncionarioExternoService funcionarioExternoService;
    private String caminhoArquivo;
    private long ultimaModificacao = 0;
    private boolean carregando = false;
    
    public FileWatcherService(FuncionarioExternoService funcionarioExternoService, 
                              CsvConfig csvConfig) {
        this.funcionarioExternoService = funcionarioExternoService;
        this.caminhoArquivo = csvConfig.getFilePath();
    }
    
    /**
     * Verifica a cada 30 segundos se o arquivo CSV foi modificado
     */
    @Scheduled(fixedRate = 30000) // 30 segundos
    public synchronized void verificarMudancasArquivo() {
        if (carregando) {
            System.out.println("Carregamento já em andamento. Ignorando chamada simultânea.");
            return;
        }
        carregando = true;
        try {
            Path path = Paths.get(caminhoArquivo);
            if (!Files.exists(path)) {
                System.out.println("Arquivo CSV não encontrado: " + caminhoArquivo);
                return;
            }
            long modificacaoAtual = Files.getLastModifiedTime(path).toMillis();
            if (modificacaoAtual > ultimaModificacao) {
                System.out.println("Arquivo CSV modificado! Recarregando funcionários...");
                funcionarioExternoService.carregarFuncionariosDoCSV();
                System.out.println("Recarregamento automático concluído às " + LocalTime.now() + "! " + funcionarioExternoService.buscarTodosFuncionarios().size() + " funcionários carregados.");
                ultimaModificacao = modificacaoAtual;
            }
        } catch (Exception e) {
            System.err.println("Erro no recarregamento automático: " + e.getMessage());
        } finally {
            carregando = false;
        }
    }
    
    /**
     * Verifica se há funcionários carregados e recarrega se necessário
     */
    //@Scheduled(fixedRate = 60000) // 1 minuto
    public void verificarFuncionariosCarregados() {
        try {
            int totalFuncionarios = funcionarioExternoService.buscarTodosFuncionarios().size();
            
            if (totalFuncionarios == 0) {
                System.out.println("Nenhum funcionário carregado. Tentando recarregar...");
                
                Path path = Paths.get(caminhoArquivo);
                if (Files.exists(path)) {
                    funcionarioExternoService.carregarFuncionariosDoCSV();
                    ultimaModificacao = Files.getLastModifiedTime(path).toMillis();
                    
                    totalFuncionarios = funcionarioExternoService.buscarTodosFuncionarios().size();
                    System.out.println("Recarregamento de emergência concluído! " + totalFuncionarios + " funcionários carregados.");
                } else {
                    System.err.println("Arquivo CSV não encontrado para recarregamento de emergência.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro no recarregamento de emergência: " + e.getMessage());
        }
    }
    
    /**
     * Define um novo caminho para o arquivo CSV
     */
    public void setCaminhoArquivo(String novoCaminho) {
        this.caminhoArquivo = novoCaminho;
        this.ultimaModificacao = 0; // Reset para forçar recarregamento
        System.out.println("Novo caminho do arquivo CSV definido: " + novoCaminho);
    }
    
    /**
     * Força um recarregamento manual
     */
    public void forcarRecarregamento() {
        try {
            System.out.println("Forçando recarregamento manual...");
            funcionarioExternoService.carregarFuncionariosDoCSV();
            
            System.out.println("Recarregamento manual concluído às " + LocalTime.now() + "! " + funcionarioExternoService.buscarTodosFuncionarios().size() + " funcionários carregados.");
            
            // Atualiza timestamp de modificação
            Path path = Paths.get(caminhoArquivo);
            if (Files.exists(path)) {
                ultimaModificacao = Files.getLastModifiedTime(path).toMillis();
            }
            
        } catch (Exception e) {
            System.err.println("Erro no recarregamento manual: " + e.getMessage());
        }
    }
} 