# 🗄️ Configuração H2 Persistente

O sistema agora está configurado para usar o banco H2 de forma persistente, salvando os dados em arquivos físicos.

## 📁 Estrutura de Arquivos

```
agr-gestao-ferias/
├── data/                          # Pasta do banco H2
│   ├── agr_ferias_db.mv.db       # Arquivo principal do banco
│   ├── agr_ferias_db.trace.db    # Arquivo de logs (se habilitado)
│   └── agr_ferias_db.lock.db     # Arquivo de lock
├── src/
└── application.properties         # Configuração do banco
```

## ⚙️ Configuração Atual

### **application.properties**
```properties
# Banco H2 Persistente
spring.datasource.url=jdbc:h2:file:./data/agr_ferias_db
spring.datasource.username=sa
spring.datasource.password=password123

# Console H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 🚀 Como Usar

### **1. Primeira Execução**
```bash
mvn spring-boot:run
```
- O banco será criado automaticamente na pasta `data/`
- As tabelas serão criadas automaticamente
- Os dados serão carregados do CSV

### **2. Acessar Console H2**
```
http://localhost:8080/h2-console
```

**Configurações do Console:**
- **JDBC URL:** `jdbc:h2:file:./data/agr_ferias_db`
- **Username:** `sa`
- **Password:** `password123`

### **3. Verificar Dados**
```sql
-- Ver funcionários
SELECT * FROM FUNCIONARIO;

-- Ver pedidos de férias
SELECT * FROM FERIAS_PEDIDO;

-- Ver parcelas usadas
SELECT nome, numero_de_parcelas_usadas, parcelas_disponiveis 
FROM FUNCIONARIO;
```

## 💾 Persistência dos Dados

### **✅ Vantagens:**
- **Dados permanecem** após reinicialização da aplicação
- **Backup automático** dos dados
- **Consultas SQL** diretas via console
- **Análise de dados** offline

### **📊 Exemplo de Dados Persistidos:**
```sql
-- Funcionários carregados do CSV
INSERT INTO FUNCIONARIO (ID, NOME, CPF, REGIME, DATA_ADMISSAO, TERMINO_PERIODO_AQUISITIVO, PARCELAS_USADAS, PARCELAS_DISPONIVEIS) 
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'João Silva', '12345678901', 'CLT', '2020-01-01', '2020-12-31', 1, 2);

-- Pedidos de férias criados automaticamente
INSERT INTO FERIAS_PEDIDO (ID, FUNCIONARIO_ID, PARCELA_NUMERO, QUANTIDADE_DIAS, DATA_INICIO, DATA_FIM, APROVADO)
VALUES (1, '123e4567-e89b-12d3-a456-426614174000', 1, 15, '2024-01-15', '2024-01-30', true);
```

## 🔄 Migração de Dados

### **Se você tinha dados em memória:**
1. **Execute a aplicação** com a nova configuração
2. **Carregue o CSV** novamente via API:
   ```bash
   POST http://localhost:8080/api/funcionarios/recarregar
   ```
3. **Os dados serão salvos** permanentemente no arquivo

### **Backup e Restore:**
```bash
# Backup
cp data/agr_ferias_db.mv.db backup/agr_ferias_db_backup.mv.db

# Restore
cp backup/agr_ferias_db_backup.mv.db data/agr_ferias_db.mv.db
```

## 🛡️ Segurança

### **Senha do Banco:**
- **Username:** `sa` (padrão)
- **Password:** `password123` (configurável)

### **Para Produção:**
```properties
# Altere a senha em application.properties
spring.datasource.password=sua_senha_segura_aqui
```

## 📈 Monitoramento

### **Logs SQL:**
```properties
# Habilita logs de SQL
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### **Console H2:**
- **URL:** `http://localhost:8080/h2-console`
- **Acesso:** Apenas em desenvolvimento
- **Desabilitar em produção:** `spring.h2.console.enabled=false`

## 🎯 Benefícios

1. **✅ Dados Persistidos:** Sobrevivem a reinicializações
2. **✅ Backup Simples:** Arquivo físico fácil de copiar
3. **✅ Consultas SQL:** Análise direta dos dados
4. **✅ Desenvolvimento:** Debug mais fácil
5. **✅ Migração:** Fácil backup/restore

Agora seus dados de funcionários e pedidos de férias ficarão salvos permanentemente! 🎉 