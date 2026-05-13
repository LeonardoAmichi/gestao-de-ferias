package com.agr.gestaodeferias.agr_gestao_ferias.service;

import com.agr.gestaodeferias.agr_gestao_ferias.dto.FuncionarioDTO;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioClt;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioEstadual;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioTemporario;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Regime;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FuncionarioRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FeriasPedidoRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido;
import com.agr.gestaodeferias.agr_gestao_ferias.config.CsvConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Service
public class FuncionarioExternoService {
    
    private final FuncionarioRepository funcionarioRepository;
    private final FeriasPedidoRepository feriasPedidoRepository;
    private final FeriasService feriasService;
    private final String csvFilePath;
    
    public FuncionarioExternoService(FuncionarioRepository funcionarioRepository, 
                                    FeriasPedidoRepository feriasPedidoRepository,
                                    FeriasService feriasService,
                                    CsvConfig csvConfig) {
        this.funcionarioRepository = funcionarioRepository;
        this.feriasPedidoRepository = feriasPedidoRepository;
        this.feriasService = feriasService;
        this.csvFilePath = csvConfig.getFilePath();
    }
    
    public List<Funcionario> buscarTodosFuncionarios() {
        return funcionarioRepository.findAll();
    }
    
    public Optional<Funcionario> buscarFuncionarioPorCPF(String cpf) {
        return funcionarioRepository.findAll().stream()
                .filter(f -> f.getCpf().equals(cpf))
                .findFirst();
    }
    
    public Optional<Funcionario> buscarFuncionarioPorId(UUID id) {
        return funcionarioRepository.findById(id);
    }
    
    public List<Funcionario> buscarFuncionariosPorTipo(Regime regime) {
        return funcionarioRepository.findAll().stream()
                .filter(f -> f.getRegime() == regime)
                .collect(Collectors.toList());
    }
    
    public FuncionarioDTO toDTO(Funcionario funcionario) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setNome(funcionario.getNome());
        dto.setCpf(funcionario.getCpf());
        dto.setRegime(funcionario.getRegime());
        dto.setDataAdmissao(funcionario.getDataAdmissao());
        dto.setTerminoPeriodoAquisitivo(funcionario.getTerminoPeriodoAquisitivo());
        dto.setNumeroDeDiasDeFeriasAcumulados(funcionario.getNumeroDeDiasDeFeriasAcumulados());
        dto.setDiasDisponiveisAnoAtual(funcionario.getDiasDisponiveisAnoAtual());
        // Calcular parcelas usadas no ciclo anual vigente
        List<FeriasPedido> pedidosFuncionario = feriasPedidoRepository.findByFuncionarioId(funcionario.getId());
        LocalDate hoje = LocalDate.now();
        LocalDate inicioCiclo = funcionario.getInicioCicloVigente(hoje);
        LocalDate fimCiclo = funcionario.getFimCicloVigente(hoje);
        List<FeriasPedido> pedidosNoCiclo = pedidosFuncionario.stream()
            .filter(p -> {
                LocalDate data = p.getDataInicio();
                return (data != null && !data.isBefore(inicioCiclo) && !data.isAfter(fimCiclo));
            })
            .toList();
        long parcelasNoCiclo = pedidosNoCiclo.stream().mapToInt(FeriasPedido::getParcelaNumero).distinct().count();
        dto.setParcelasUsadasNoCicloAnual((int) parcelasNoCiclo);
        return dto;
    }
    

    @Transactional
    public void limparBancoParaRecarregamento() {
        feriasPedidoRepository.deleteAll();
        feriasPedidoRepository.flush();
        funcionarioRepository.deleteAll();
        funcionarioRepository.flush();
    }

    public synchronized void carregarFuncionariosDoCSV() {
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            System.out.println("Iniciando carregamento de funcionários do CSV...");
            limparBancoParaRecarregamento();
            System.out.println("Dados anteriores limpos. Iniciando carregamento...");
            
            int contador = 0;
            int linhaAtual = 1; 
            
            List<Funcionario> funcionariosBatch = new java.util.ArrayList<>();
            List<FeriasPedido> pedidosBatch = new java.util.ArrayList<>();
            
            for (CSVRecord record : csvParser) {
                linhaAtual++;
                try {
                    if (record.size() < 4) continue;
                    
                    String nome = record.get(1).trim();
                    String cpf = record.get(2).trim();
                    String regimeStr = record.get(3).trim();
                    
                    if (nome.isEmpty() || cpf.isEmpty() || regimeStr.isEmpty()) continue;
                    
                    String regimeNormalizado = regimeStr.toLowerCase()
                        .replace("á", "a").replace("ã", "a").replace("â", "a")
                        .replace("é", "e").replace("ê", "e")
                        .replace("í", "i")
                        .replace("ó", "o").replace("õ", "o").replace("ô", "o")
                        .replace("ú", "u").replace("ç", "c").trim();
                    
                    Regime regime = Regime.CLT;
                    if (regimeNormalizado.contains("estatutario")) regime = Regime.ESTADUAL;
                    else if (regimeNormalizado.contains("temporario")) regime = Regime.TEMPORARIO;
                    
                    LocalDate dataAdmissao = null;
                    LocalDate terminoPeriodoAquisitivo = null;
                    
                    if (record.size() > 4) dataAdmissao = parseDateUniversal(record.get(4).trim());
                    if (record.size() > 5) terminoPeriodoAquisitivo = parseDateUniversal(record.get(5).trim());
                    
                    if (dataAdmissao == null) dataAdmissao = LocalDate.now().minusYears(1);
                    if (terminoPeriodoAquisitivo == null) terminoPeriodoAquisitivo = LocalDate.now();
                    
                    Funcionario funcionario = criarFuncionarioPorRegime(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
                    funcionariosBatch.add(funcionario);
                    contador++;
                    
                    if (record.size() >= 8) {
                        String diasGozoStr = record.get(7).trim();
                        String inicioFeriasStr = record.get(6).trim();
                        
                        try {
                            if (diasGozoStr != null && !diasGozoStr.isEmpty() && !diasGozoStr.equals("0")) {
                                int diasGozo = Integer.parseInt(diasGozoStr);
                                if (diasGozo >= funcionario.getMinimoDiasPrimeiraParcela()) {
                                    LocalDate dataInicioFerias = parseDateUniversal(inicioFeriasStr);
                                    if (dataInicioFerias == null) dataInicioFerias = LocalDate.now();
                                    
                                    FeriasPedido pedido = new FeriasPedido();
                                    pedido.setFuncionario(funcionario);
                                    pedido.setNomeFuncionario(funcionario.getNome());
                                    pedido.setParcelaNumero(1);
                                    pedido.setQuantidadeDias(diasGozo);
                                    pedido.setDataInicio(dataInicioFerias);
                                    pedido.setDataFim(dataInicioFerias.plusDays(diasGozo - 1));
                                    pedido.setCpfFuncionario(funcionario.getCpf());
                                    
                                    pedidosBatch.add(pedido);
                                }
                            }
                        } catch (Exception e) {}
                    }
                    
                } catch (Exception e) {
                    System.out.println("Erro ao processar linha " + linhaAtual + ": " + e.getMessage());
                }
            }
            
            // Processando batches
            System.out.println("Salvando lote de funcionários...");
            funcionarioRepository.saveAll(funcionariosBatch);
            
            System.out.println("Salvando lote de pedidos...");
            feriasPedidoRepository.saveAll(pedidosBatch);
            
            System.out.println("Recalculando saldos baseados nos pedidos...");
            for (Funcionario f : funcionariosBatch) {
                feriasService.atualizarDiasDeFeriasAcumulados(f);
            }
            funcionarioRepository.saveAll(funcionariosBatch);
            
            System.out.println("Carregamento concluído! " + contador + " funcionários processados.");
            
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        }
    }
    
    public static LocalDate parseDateUniversal(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) return null;
        String data = dataStr.trim().toLowerCase().replaceAll("\\s+", " ");
        data = data.replace("jan", "01").replace("fev", "02").replace("mar", "03").replace("abr", "04")
                   .replace("mai", "05").replace("jun", "06").replace("jul", "07").replace("ago", "08")
                   .replace("set", "09").replace("out", "10").replace("nov", "11").replace("dez", "12");
                   
        if (data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = data.split("/");
            data = parts[0] + "-" + parts[1] + "-" + parts[2].substring(2);
        }

        String[] formats = {"dd-MM-yy", "dd-MM-yyyy", "d-MM-yy", "d-MM-yyyy", "dd/MM/yy", "dd/MM/yyyy", "d/MM/yy", "d/MM/yyyy"};
        for (String f : formats) {
            try { return LocalDate.parse(data, DateTimeFormatter.ofPattern(f)); } catch (Exception ignored) {}
        }
        
        String dataEng = dataStr.trim().replaceAll("(?i)jan", "Jan").replaceAll("(?i)fev", "Feb")
                   .replaceAll("(?i)mar", "Mar").replaceAll("(?i)abr", "Apr").replaceAll("(?i)mai", "May")
                   .replaceAll("(?i)jun", "Jun").replaceAll("(?i)jul", "Jul").replaceAll("(?i)ago", "Aug")
                   .replaceAll("(?i)set", "Sep").replaceAll("(?i)out", "Oct").replaceAll("(?i)nov", "Nov").replaceAll("(?i)dez", "Dec");
                   
        String[] formatsEng = {"dd-MMM-yy", "dd-MMM-yyyy", "d-MMM-yy", "d-MMM-yyyy"};
        for (String f : formatsEng) {
            try { return LocalDate.parse(dataEng, DateTimeFormatter.ofPattern(f, java.util.Locale.ENGLISH)); } catch (Exception ignored) {}
        }
        return null;
    }

    private Funcionario criarFuncionarioPorRegime(String nome, String cpf, Regime regime, LocalDate dataAdmissao, LocalDate terminoPeriodoAquisitivo) {
        switch (regime) {
            case CLT:
                return new FuncionarioClt(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
            case ESTADUAL:
                return new FuncionarioEstadual(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
            case TEMPORARIO:
                return new FuncionarioTemporario(nome, cpf, regime, dataAdmissao, terminoPeriodoAquisitivo);
            default:
                throw new IllegalArgumentException("Regime não suportado: " + regime);
        }
    }
} 