# 🔄 Carregamento Dinâmico de Funcionários

O sistema agora possui carregamento dinâmico automático de funcionários, garantindo que os dados estejam sempre atualizados.

## 🚀 Funcionalidades

### 1. **Carregamento Automático na Inicialização**
- ✅ Carrega automaticamente quando o Spring Boot inicia
- ✅ Usa o arquivo: `C:\Users\leoam\vs-code-workspace\GestaoDeFerias---AGR\GestaoDeFerias---AGR-main\agr-gestao-ferias\PLANILHA CONTROLE DE FÉRIAS ATIVA(Controle de Férias).csv`
- ✅ Salva no banco H2 e mantém em memória
- ✅ **NOVO:** Cria automaticamente pedidos de férias baseados na coluna "dias de gozo"

### 2. **Monitoramento de Mudanças**
- ✅ Verifica a cada **30 segundos** se o arquivo CSV foi modificado
- ✅ Recarrega automaticamente quando detecta mudanças
- ✅ Verifica a cada **1 minuto** se há funcionários carregados (recarregamento de emergência)

### 3. **Criação Automática de Pedidos de Férias**
- ✅ Lê a coluna "dias de gozo" do CSV
- ✅ Lê a coluna "INICIO DAS FÉRIAS" do CSV
- ✅ **Se houver data na coluna 6:** Usa essa data como início das férias
- ✅ **Se não houver data:** Usa `LocalDate.now()` como início das férias
- ✅ Cria automaticamente pedidos de férias para funcionários com dias de gozo > 0
- ✅ Define como primeira parcela (parcela 1)
- ✅ **Incrementa automaticamente o `numeroDeParcelasUsadas` do funcionário**
- ✅ Aprova automaticamente o pedido
- ✅ Salva no banco de dados

### 4. **Endpoints de Controle**

#### **Status do Sistema**
```bash
GET http://localhost:8080/api/funcionarios/status
```
**Resposta:**
```
Status do Sistema:
Funcionários carregados: 196
CLT: 120
Temporário: 45
Estadual: 31
Parcelas usadas: 45
Parcelas disponíveis: 543
Monitoramento automático: ATIVO
Verificação a cada 30 segundos
```

#### **Parcelas de um Funcionário**
```bash
GET http://localhost:8080/api/funcionarios/12345678901/parcelas
```
**Resposta:**
```
Parcelas do Funcionário:
Nome: João Silva
CPF: 12345678901
Regime: CLT
Parcelas usadas: 1
Parcelas disponíveis: 2
Total de parcelas: 3
```

#### **Regras de Negócio**
```bash
GET http://localhost:8080/api/funcionarios/regras-negocio
```
**Resposta:**
```
Regras de Negócio - Sistema de Férias:

1. LIMITE DE PARCELAS:
   - Máximo de 3 parcelas por ano
   - Máximo de 30 dias de férias por ano

2. MÍNIMO DE DIAS POR TIPO DE FUNCIONÁRIO:
   - CLT: Mínimo 15 dias na primeira parcela
   - Temporário: Mínimo 5 dias na primeira parcela
   - Estadual: Mínimo 5 dias na primeira parcela

3. CONTROLE DE PARCELAS:
   - Cada pedido de férias incrementa numeroDeParcelasUsadas
   - Cada pedido de férias decrementa parcelasDisponiveis
   - Total sempre deve ser 3 (usadas + disponíveis)

4. VALIDAÇÕES AUTOMÁTICAS:
   - Verifica se funcionário tem parcelas disponíveis
   - Verifica se respeita mínimo de dias por tipo
   - Verifica se não ultrapassa 30 dias totais
   - Verifica se não ultrapassa 3 parcelas

5. CARREGAMENTO CSV:
   - Cria pedidos automaticamente baseado na coluna 'dias de gozo'
   - Usa data da coluna 'INICIO DAS FÉRIAS' se disponível
   - Aplica todas as validações de negócio
   - Incrementa parcelas usadas automaticamente
```

#### **Pedidos de Férias Criados**
```bash
GET http://localhost:8080/api/funcionarios/pedidos-ferias
```
**Resposta:**
```
Pedidos de Férias Criados Automaticamente:

Total de pedidos: 45

Funcionário: João Silva
Parcela: 1
Dias: 15
Data Início: 2024-01-15
Data Fim: 2024-01-30
Aprovado: Sim
---
Funcionário: Maria Santos
Parcela: 1
Dias: 20
Data Início: 2024-01-15
Data Fim: 2024-02-04
Aprovado: Sim
---
```

#### **Recarregamento Manual**
```bash
POST http://localhost:8080/api/funcionarios/recarregar
```
**Resposta:** `Recarregamento forçado concluído! 196 funcionários carregados.`

#### **Configurar Novo Caminho**
```bash
POST http://localhost:8080/api/funcionarios/configurar-caminho?caminho=novo/caminho/arquivo.csv
```
**Resposta:** `Caminho do CSV configurado: novo/caminho/arquivo.csv`

#### **Carregamento Manual**
```bash
POST http://localhost:8080/api/funcionarios/carregar-csv?caminho=caminho/arquivo.csv
```

## 📊 Estrutura do CSV

O sistema usa **Apache Commons CSV** para leitura robusta de arquivos CSV com encoding UTF-8, suportando caracteres acentuados (como "é", "ã", "ç").

**Características do CSV Reader:**
- ✅ **Encoding UTF-8:** Suporte completo a caracteres acentuados
- ✅ **Tratamento automático de cabeçalhos:** Pula automaticamente a primeira linha
- ✅ **Validação de campos:** Verifica campos obrigatórios
- ✅ **Tratamento de erros:** Continua processando mesmo com registros inválidos
- ✅ **Logs detalhados:** Registra cada etapa do processamento

O sistema espera o seguinte formato de colunas:

| Coluna | Índice | Descrição |
|--------|--------|-----------|
| ID | 0 | Identificador (ignorado) |
| Nome | 1 | Nome do funcionário |
| CPF | 2 | CPF do funcionário |
| Regime | 3 | Celetista/Estatutário/Temporário |
| Data Início | 4 | Data de início do período (dd-MMM-yy) |
| Data Término | 5 | Data de término do período (dd-MMM-yy) |
| **INICIO DAS FÉRIAS** | **6** | **Data de início das férias (dd-MMM-yy)** |
| **Dias de Gozo** | **7** | **Quantidade de dias de férias** |

## 🎯 API de Pedidos de Férias

### **Criar Pedido de Férias**
```bash
POST http://localhost:8080/api/ferias/solicitar
```

**Corpo da Requisição:**
```json
{
  "funcionarioId": "uuid-do-funcionario",
  "parcelaNumero": 1,
  "quantidadeDias": 15,
  "dataInicio": "2024-01-15"  // Opcional - se não informada, usa a data atual
}
```

**Resposta:**
```json
{
  "funcionarioId": "uuid-do-funcionario",
  "parcelaNumero": 1,
  "quantidadeDias": 15,
  "dataInicio": "2024-01-15",
  "aprovado": false
}
```

**Comportamento:**
- Se `dataInicio` não for informada, o sistema usa a data atual
- O sistema aplica todas as validações de negócio
- O número de parcelas usadas é incrementado automaticamente

### **Exemplo de Linha CSV:**
```
1,João Silva,12345678901,Celetista,01-Jan-20,31-Dez-20,15-Jan-24,15
```

## 🔧 Benefícios do Apache Commons CSV

### **Antes (String.split):**
```java
// Problemas com vírgulas dentro de campos, aspas, etc.
String[] colunas = linha.split(",", -1);
```

### **Agora (CSV Reader):**
```java
// Tratamento robusto de CSV com Apache Commons
CSVParser csvParser = CSVFormat.DEFAULT
    .withFirstRecordAsHeader()
    .withIgnoreHeaderCase()
    .withTrim()
    .parse(isr);

for (CSVRecord record : csvParser) {
    String nome = record.get(1).trim();
    String cpf = record.get(2).trim();
    // ...
}
```

### **Vantagens:**
- ✅ **Campos com vírgulas:** Trata corretamente campos que contêm vírgulas
- ✅ **Campos com aspas:** Suporta campos entre aspas duplas
- ✅ **Campos vazios:** Trata adequadamente campos vazios
- ✅ **Encoding:** Suporte nativo a UTF-8
- ✅ **Validação:** Verifica automaticamente o número de colunas
- ✅ **Performance:** Leitura otimizada de arquivos grandes

## ⚙️ Configuração

### **Arquivo de Configuração**
O caminho padrão está definido em:
- `AutoLoadConfig.java` (carregamento inicial)
- `FileWatcherService.java` (monitoramento)

### **Alterar Caminho Padrão**
1. **Via Endpoint:**
```bash
POST /api/funcionarios/configurar-caminho?caminho=novo/caminho
```

2. **Via Código:**
```java
@Autowired
private FileWatcherService fileWatcherService;

fileWatcherService.setCaminhoArquivo("novo/caminho/arquivo.csv");
```

## 🔍 Logs do Sistema

### **Inicialização:**
```
Iniciando carregamento automático de funcionários...
Carregamento automático concluído! 196 funcionários carregados.
Usando data de início das férias da planilha: 2024-01-15 para João Silva
Pedido de férias criado para João Silva - 15 dias de gozo, início: 2024-01-15
Usando data atual como início das férias para Maria Santos
Pedido de férias criado para Maria Santos - 20 dias de gozo, início: 2024-01-15
```

### **Monitoramento:**
```
Arquivo CSV modificado! Recarregando funcionários...
Recarregamento automático concluído às 14:30:25! 197 funcionários carregados.
Usando data de início das férias da planilha: 2024-02-01 para Novo Funcionário
Pedido de férias criado para Novo Funcionário - 10 dias de gozo, início: 2024-02-01
```

### **Recarregamento de Emergência:**
```
Nenhum funcionário carregado. Tentando recarregar...
Recarregamento de emergência concluído! 196 funcionários carregados.
```

## 🛡️ Tratamento de Erros

### **Arquivo não encontrado:**
```
Arquivo CSV não encontrado: C:\Users\leoam\vs-code-workspace\GestaoDeFerias---AGR\GestaoDeFerias---AGR-main\agr-gestao-ferias\PLANILHA CONTROLE DE FÉRIAS ATIVA(Controle de Férias).csv
```

### **Erro no carregamento:**
```
Erro no carregamento automático: Arquivo não encontrado
A aplicação continuará funcionando, mas sem dados de funcionários.
```

### **Erro nos dias de gozo:**
```
Erro ao converter dias de gozo para João Silva: abc - For input string: "abc"
Pedido de férias criado para Maria Santos - 20 dias de gozo
```

### **Validação de mínimo de dias:**
```
Funcionário João Silva (CLT) possui apenas 10 dias, mas precisa de no mínimo 15 dias para primeira parcela
Funcionário Maria Santos (Temporário) possui apenas 3 dias, mas precisa de no mínimo 5 dias para primeira parcela
```

## 📊 Benefícios

- ✅ **Sempre atualizado:** Dados refletem mudanças no CSV automaticamente
- ✅ **Resiliente:** Recarrega mesmo se houver falhas
- ✅ **Flexível:** Permite mudança de caminho via API
- ✅ **Monitorado:** Logs detalhados de todas as operações
- ✅ **Performance:** Cache em memória + persistência no banco
- ✅ **Controle:** Endpoints para gerenciamento manual
- ✅ **Automatizado:** Cria pedidos de férias automaticamente

## 🎯 Exemplo de Uso Completo

```bash
# 1. Iniciar aplicação (carrega automaticamente)
mvn spring-boot:run

# 2. Verificar status
curl http://localhost:8080/api/funcionarios/status

# 3. Ver pedidos de férias criados
curl http://localhost:8080/api/funcionarios/pedidos-ferias

# 4. Modificar arquivo CSV (recarrega automaticamente em 30s)

# 5. Forçar recarregamento imediato
curl -X POST http://localhost:8080/api/funcionarios/recarregar

# 6. Mudar caminho do arquivo
curl -X POST "http://localhost:8080/api/funcionarios/configurar-caminho?caminho=novo/caminho/arquivo.csv"
```

## 🔧 Configurações Avançadas

### **Alterar Intervalos de Verificação**
No `FileWatcherService.java`:
```java
@Scheduled(fixedRate = 30000) // 30 segundos para mudanças
@Scheduled(fixedRate = 60000) // 1 minuto para emergência
```

### **Desabilitar Carregamento Automático**
Comente a anotação `@Component` em `AutoLoadConfig.java`

### **Desabilitar Monitoramento**
Comente a anotação `@Service` em `FileWatcherService.java`

### **Personalizar Criação de Pedidos**
No método `determinarDataInicioFerias()` em `FuncionarioExternoService.java`:
```java
// Lógica atual:
// Se coluna 6 tem data → usa essa data
// Se coluna 6 está vazia → usa LocalDate.now()

// Para personalizar:
// Alterar parcela
pedido.setParcelaNumero(2); // Segunda parcela

// Não aprovar automaticamente
pedido.setAprovado(false);
```

---

**🎉 Agora seu sistema carrega funcionários automaticamente, cria pedidos de férias e se mantém sempre atualizado!** 