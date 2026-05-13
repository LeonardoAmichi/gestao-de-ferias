# Carregamento de Funcionários via CSV

Este sistema permite carregar funcionários de um arquivo CSV para o sistema de gestão de férias.

## 📋 Estrutura do CSV

O arquivo CSV deve ter a seguinte estrutura:

```csv
,NOME,CPF,REGIME,"Início do período aquisitivo","Término do período aquisitivo"
1,João Silva,123.456.789-00,Celetista,01-Jan-20,31-Dec-20
2,Maria Santos,987.654.321-00,Estatutário,15-Mar-19,14-Mar-20
3,Pedro Costa,456.789.123-00,Temporário,10-Jun-21,09-Jun-22
```

### Colunas:
- **Coluna 0**: ID (não usado)
- **Coluna 1**: Nome do funcionário
- **Coluna 2**: CPF
- **Coluna 3**: Regime (Celetista, Estatutário, Temporário)
- **Coluna 4**: Início do período aquisitivo (formato: dd-MMM-yy)
- **Coluna 5**: Término do período aquisitivo (formato: dd-MMM-yy)

### Regimes Suportados:
- **Celetista** → CLT
- **Estatutário** → ESTADUAL  
- **Temporário** → TEMPORARIO

## 🚀 Como Usar

### 1. Via Código Java

```java
@Autowired
private FuncionarioExternoService funcionarioExternoService;

// Carregar funcionários do CSV
funcionarioExternoService.carregarFuncionariosDoCSV("caminho/para/arquivo.csv");

// Listar funcionários carregados
List<Funcionario> funcionarios = funcionarioExternoService.buscarTodosFuncionarios();
```

### 2. Via HTTP POST

```bash
curl -X POST "http://localhost:8080/funcionarios/carregar-csv" \
     -d "caminho=PLANILHA%20CONTROLE%20DE%20F%C3%89RIAS%20ATIVA(Controle%20de%20F%C3%A9rias).csv"
```

### 3. Via Postman/Insomnia

**URL:** `POST http://localhost:8080/funcionarios/carregar-csv`

**Body (form-data):**
- `caminho`: `PLANILHA CONTROLE DE FÉRIAS ATIVA(Controle de Férias).csv`

## 📊 Endpoints Disponíveis

### Carregar CSV
```
POST /funcionarios/carregar-csv
```

### Listar Todos os Funcionários
```
GET /funcionarios
```

### Buscar por CPF
```
GET /funcionarios/{cpf}
```

### Buscar por Regime
```
GET /funcionarios/regime/{regime}
```

### Estatísticas
```
GET /funcionarios/estatisticas
```

## 🔧 Configuração

### Dependências
O projeto usa `BufferedReader` nativo do Java para leitura de arquivos CSV, não requerendo dependências externas.

### Formato de Data
O sistema espera datas no formato `dd-MMM-yy` (ex: 01-Jan-20) e converte automaticamente para `dd-MM-yy` (ex: 01-01-20).

## ⚠️ Observações

1. **Arquivo CSV**: Deve estar no diretório raiz do projeto ou especificar o caminho completo
2. **Encoding**: Recomenda-se usar UTF-8 para caracteres especiais
3. **Cabeçalho**: A primeira linha é ignorada (cabeçalho)
4. **Validação**: O sistema valida o formato do regime e das datas
5. **Memória**: Os funcionários são carregados em memória (não persistidos no banco)
6. **Período Aquisitivo**: O sistema armazena início e fim do período aquisitivo de férias

## 🐛 Tratamento de Erros

O sistema trata os seguintes erros:

- **Arquivo não encontrado**: Verifica se o caminho está correto
- **Formato de data inválido**: Verifica se as datas estão no formato correto
- **Regime inválido**: Verifica se o regime é um dos valores suportados
- **CPF duplicado**: Permite CPFs duplicados (último registro prevalece)

## 🔍 Métodos de Validação

O sistema inclui métodos para validar o período aquisitivo:

- **`estaNoPeriodoAquisitivo()`**: Verifica se o funcionário está no período aquisitivo atual
- **`periodoAquisitivoVencido()`**: Verifica se o período aquisitivo já venceu

## 📝 Exemplo de Uso Completo

```java
@Component
public class ExemploUso {
    
    @Autowired
    private FuncionarioExternoService funcionarioExternoService;
    
    public void carregarEListarFuncionarios() {
        try {
            // Carrega do CSV
            funcionarioExternoService.carregarFuncionariosDoCSV("funcionarios.csv");
            
            // Lista todos
            List<Funcionario> todos = funcionarioExternoService.buscarTodosFuncionarios();
            System.out.println("Total: " + todos.size());
            
            // Busca por regime
            List<Funcionario> clts = funcionarioExternoService.buscarFuncionariosPorTipo(Regime.CLT);
            System.out.println("CLTs: " + clts.size());
            
            // Busca por CPF
            Optional<Funcionario> funcionario = funcionarioExternoService.buscarFuncionarioPorCPF("123.456.789-00");
            if (funcionario.isPresent()) {
                System.out.println("Encontrado: " + funcionario.get().getNome());
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
} 