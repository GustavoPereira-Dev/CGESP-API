# ⛈️ API CGE Crawler - Monitoramento de SP

Esta API foi desenvolvida em **Java com Spring Boot** para coletar, estruturar e armazenar dados meteorológicos e de emergência do site do **Centro de Gerenciamento de Emergências (CGE)** da Prefeitura de São Paulo.

O sistema utiliza técnicas de **Web Scraping (Jsoup)** para extrair dados de páginas HTML legadas e expô-los em uma API REST moderna (JSON), persistindo o histórico em um banco de dados **SQL Server**.

---

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21+
* **Framework:** Spring Boot 4.x (Web, Data JPA)
* **Banco de Dados:** SQL Server
* **Extração de Dados:** Jsoup (HTML Parser)
* **Utilitários:** Lombok

---

## ⚙️ Configuração Inicial

### 1. Banco de Dados
Certifique-se de que o SQL Server esteja rodando e configure o acesso no arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=cge_db;encrypt=true;trustServerCertificate=true;
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

# Configurações do Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
2. Dependências (Maven)Certifique-se de ter o Jsoup no seu pom.xml:XML<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```
## 📚 Documentação da API

Abaixo estão os detalhes de consumo dos endpoints. Para testar, você pode importar os comandos curl diretamente no Postman (File > Import > Raw Text).

### 1. 🌊 Alagamentos
Gerencia os pontos de alagamento na cidade (ativos, inativos, transitáveis ou não).

#### A. Checar em Tempo Real (Sem salvar)
Coleta os dados do site agora e apenas exibe. Útil para debug ou dashboards rápidos.

- Método: GET
- URL: /api/alagamentos/checar-agora

``` curl --location 'http://localhost:8080/api/alagamentos/checar-agora' ```

#### B. Sincronizar (Coletar e Salvar)
Coleta os dados atuais e salva no banco SQL Server para histórico.

- Método: POST
- URL: /api/alagamentos/sincronizar

### C. Buscar Histórico (Data Específica)
Busca no site os dados de uma data passada.

- Método: GET
- URL: /api/alagamentos/historico?data=25/01/2026
- Parâmetro: data (Formato dd/MM/yyyy)

```curl --location 'http://localhost:8080/api/alagamentos/historico?data=25/01/2026'```


### 2. 📰 Notícias
Coleta avisos oficiais e boletins meteorológicos.

#### A. Sincronizar Recentes
Pega as notícias da página inicial e salva no banco.

- Método: POST
- URL: /api/noticias/sincronizar

``` curl --location --request POST 'http://localhost:8080/api/noticias/sincronizar'```

### B. Buscar por Data
Busca notícias de um dia específico no site.
- Método: GET
- URL: /api/noticias/historico?data=2026-01-01
- Parâmetro: data (Formato yyyy-MM-dd)
```curl --location 'http://localhost:8080/api/noticias/historico?data=2026-01-01'```

### 3. 🌡️ Estações Meteorológicas
Gerencia sensores espalhados pela cidade (Temperatura, Umidade, Chuva).

#### A. Sincronizar Lista de Estações
Lê a lista de estações disponíveis (Nome e ID) e salva no banco. Execute isso primeiro.

- Método: GET
- URL: /api/estacoes/sincronizar-lista

```curl --location 'http://localhost:8080/api/estacoes/sincronizar-lista'```

#### B. Ler Dados Atuais de uma Estação
Pega a temperatura/chuva atual de uma estação específica pelo ID e salva o histórico.

- Método: GET
- URL: /api/estacoes/{id_posto}/atual
- Exemplo: ID 1000635 (Pinheiros) ou 1000887 (Penha).

```curl --location 'http://localhost:8080/api/estacoes/1000635/atual'```

### 4. 🌦️ Previsão do Tempo
Coleta a previsão estendida (dias e períodos: manhã, tarde, noite).

#### A. Sincronizar Previsão
Lê a página de previsão estendida e atualiza o banco de dados.

- Método: POST
- URL: /api/previsao/sincronizar

```curl --location --request POST 'http://localhost:8080/api/previsao/sincronizar'```

#### B. Ver Previsão de Hoje
Retorna do banco a previsão salva para a data atual.

- Método: GET
- URL: /api/previsao/hoje

```curl --location 'http://localhost:8080/api/previsao/hoje'```

## 🗄️ Estrutura do Banco de Dados
O Hibernate criará as tabelas automaticamente (ddl-auto=update). As principais são:

- tb_alagamento: Local, zona, bairro, horários de início/fim e status.
- tb_noticia: Título, conteúdo (texto longo), data de publicação e link original.
- tb_estacao: Cadastro das estações (ID e Nome).
- tb_leitura_meteorologica: Histórico de leituras (temp, chuva, umidade) vinculado a uma estação.
- tb_previsao_dia: Cabeçalho da previsão diária (Data, Min/Max Temp).
- tb_previsao_periodo: Detalhes dos períodos (Manhã/Tarde/Noite) vinculados ao dia.


## ⚠️ Avisos Importantes
- Web Scraping: Esta API depende da estrutura HTML do site da CGE. Se o site mudar o layout, os seletores CSS do Jsoup precisarão ser atualizados.
- Bloqueios: Evite fazer chamadas em loop infinito sem delay, pois o servidor da prefeitura pode bloquear seu IP temporariamente.
- Formatos de Data: A CGE utiliza formatos diferentes na URL (dd/MM/yyyy para alagamentos e yyyy-MM-dd para notícias). A API trata isso, mas atente-se aos parâmetros.