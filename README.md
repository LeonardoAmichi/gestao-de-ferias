# Gestão de Férias - AGR

Sistema de gestão de férias para funcionários, com carregamento dinâmico de dados via CSV e persistência em banco de dados H2.

## 🚀 Funcionalidades

- **Carregamento Automático**: O sistema carrega funcionários de um arquivo CSV na inicialização.
- **Monitoramento em Tempo Real**: Verifica mudanças no arquivo CSV a cada 30 segundos e recarrega os dados automaticamente.
- **Criação de Pedidos de Férias**: Gera automaticamente pedidos de férias baseados na coluna "dias de gozo" e "INICIO DAS FÉRIAS" do CSV.
- **Persistência H2**: Os dados são salvos de forma persistente em arquivos locais na pasta `data/`.

## 📋 Estrutura do CSV

O arquivo CSV deve conter as seguintes colunas (índices):
0. ID (ignorado)
1. Nome
2. CPF
3. Regime (Celetista/Estatutário/Temporário)
4. Início do Período Aquisitivo (dd-MMM-yy)
5. Término do Período Aquisitivo (dd-MMM-yy)
6. INICIO DAS FÉRIAS (dd-MMM-yy)
7. Dias de Gozo

## ⚙️ Configuração

O sistema utiliza um banco de dados H2 persistente. As configurações estão localizadas em `src/main/resources/application.properties`.

- **Console H2**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:file:./data/agr_ferias_db`
- **Username**: `sa`
- **Password**: `password123`

## 📊 Principais Endpoints

### Funcionários
- `GET /api/funcionarios/status`: Status geral do sistema.
- `GET /api/funcionarios/{cpf}/parcelas`: Consulta parcelas de um funcionário.
- `POST /api/funcionarios/recarregar`: Força o recarregamento do CSV.
- `POST /api/funcionarios/configurar-caminho?caminho=...`: Altera o caminho do arquivo CSV monitorado.

### Férias
- `POST /api/ferias/solicitar`: Cria um novo pedido de férias manualmente.
- `GET /api/funcionarios/pedidos-ferias`: Lista pedidos criados automaticamente.

## 🔧 Regras de Negócio

1. **Limite de Parcelas**: Máximo de 3 parcelas por ano.
2. **Dias Totais**: Máximo de 30 dias de férias por ano.
3. **Mínimo por Parcela**:
   - CLT: Mínimo 15 dias na primeira parcela.
   - Temporário/Estadual: Mínimo 5 dias na primeira parcela.

## 🛠️ Como Executar

```bash
mvn spring-boot:run
```

O sistema criará automaticamente a pasta `data/` e carregará os dados se o arquivo CSV estiver no caminho configurado.
