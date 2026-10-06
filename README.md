# Leite Solidário API

API REST para cadastro de doadoras e doações de leite humano, com persistência em SQL Server.

**Disciplina:** Microservices and Web Engineering & IT Services  
**Atividade:** Check Point 2 — 2º semestre/2026  
**Professor:** Antonio Carlos de Lima Júnior  
**Integrante:** João Castro — RM 554628  
**Repositório definido:** https://github.com/JoaoCastro768/leite-solidario-api

O domínio acompanha o Challenge Eurofarma de doação de leite humano. O projeto é independente e não utiliza `study-apir`.

## Tecnologias

Java 17, Spring Boot 3.5.16, Spring Data JPA/Hibernate, Bean Validation, Microsoft JDBC Driver, SQL Server, Maven Wrapper e Swagger/OpenAPI.

A aplicação não utiliza banco em memória. Os dados são armazenados no SQL Server.

## Executar no Windows

É necessário Docker Desktop instalado, com contêineres Linux. Reserve pelo menos 4 GB de memória para o Docker. As portas padrão são 9000 para a API e 1433 para o banco. O primeiro build exige internet para baixar as imagens e dependências.

Na pasta do projeto, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

O script verifica o Docker, tenta abrir o Docker Desktop se estiver parado, cria `.env` quando necessário e inicia os serviços. Depois aguarda a API e o banco ficarem disponíveis. Se o Docker Desktop apresentar um erro de WSL ou virtualização, esse problema precisa ser resolvido na máquina.

Abra http://localhost:9000 para usar o Swagger.

## Executar no Linux

Com o Docker instalado e em execução:

```bash
./iniciar.sh
```

Também é possível executar diretamente pelo Compose:

```bash
cp .env.example .env
docker compose up -d --build
```

No PowerShell, o comando equivalente de cópia é `Copy-Item .env.example .env`.

O Compose inicia SQL Server 2022 Developer, aguarda o banco aceitar conexões, cria `leite_solidario` e inicia a API. O Hibernate cria ou atualiza as tabelas mapeadas. A edição Developer é destinada a desenvolvimento e teste.

## Configuração da conexão

| Item | Valor local |
|---|---|
| Servidor | localhost |
| Porta | 1433 |
| Banco | leite_solidario |
| Usuário | sa |
| Senha de exemplo | LeiteLocal_2026! |
| API | http://localhost:9000 |

As senhas em `.env.example` são valores fictícios para o ambiente local. Copie para `.env`, mantenha `MSSQL_SA_PASSWORD` e `DB_PASSWORD` iguais e não publique `.env`. Alterar esse arquivo não altera a senha de uma instância já criada.

| Variável do Spring | Uso |
|---|---|
| DB_URL | URL JDBC com servidor, porta e banco |
| DB_USERNAME | Usuário SQL Server; padrão `sa` |
| DB_PASSWORD | Senha obrigatória |
| SERVER_PORT | Porta da API; padrão `9000` |

No Compose, a URL usa `sqlserver`, nome do serviço. Para Java executado fora do Docker, use o endereço real do banco.

### SQL Server instalado ou disponibilizado pelo professor

Crie o banco com `database/init.sql` ou utilize o banco disponibilizado. O usuário precisa ter acesso aos dados e permissão para criar/atualizar as tabelas do projeto. Em instâncias nomeadas, confira a porta TCP/IP configurada.

Com JDK 17, no PowerShell:

```powershell
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=leite_solidario;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME='sa'
$env:DB_PASSWORD='LeiteLocal_2026!'
.\mvnw.cmd spring-boot:run
```

Substitua servidor, porta, banco e credenciais pelos valores fornecidos. `trustServerCertificate=true` é usado no banco local de desenvolvimento; siga a configuração de certificado exigida no servidor remoto.

Maven é baixado automaticamente pelo Wrapper. O arquivo `.env` é lido pelo Compose; na execução Java local, configure as variáveis no terminal.

Para iniciar apenas o banco fornecido pelo Docker:

```bash
docker compose up -d db-init
```

### JAR compilado

O ZIP inclui `dist/leite-solidario-api.jar`. Com Java 17, o banco criado e as variáveis anteriores configuradas:

```powershell
java -jar .\dist\leite-solidario-api.jar
```

No Linux: `java -jar dist/leite-solidario-api.jar`.

## Modelo e organização

| Tabela | Campos |
|---|---|
| doadoras | id, nome, email, telefone, cidade, ativa, data_cadastro |
| doacoes | id, doadora_id, volume_ml, data_coleta, status, observacoes |

Uma doadora pode ter várias doações. Cada doação pertence a uma doadora, por chave estrangeira. Os IDs são gerados pelo banco com `IDENTITY`. E-mails são únicos; nome, cidade e observações usam Unicode.

O fluxo é **Controller → Service → Repository → SQL Server**. Os DTOs definem os dados de entrada e saída. Os serviços concentram regras e transações.

## Endpoints

| Método | Endpoint | Operação | Sucesso |
|---|---|---|---|
| GET | /api/v1/doadoras | Listar doadoras | 200 |
| GET | /api/v1/doadoras/{id} | Buscar doadora | 200 |
| POST | /api/v1/doadoras | Cadastrar doadora | 201 |
| PUT | /api/v1/doadoras/{id} | Atualizar doadora | 200 |
| DELETE | /api/v1/doadoras/{id} | Excluir doadora sem doações | 204 |
| GET | /api/v1/doacoes | Listar doações | 200 |
| GET | /api/v1/doacoes?doadoraId={id} | Filtrar por doadora | 200 |
| GET | /api/v1/doacoes/{id} | Buscar doação | 200 |
| POST | /api/v1/doacoes | Cadastrar doação | 201 |
| PUT | /api/v1/doacoes/{id} | Atualizar doação | 200 |
| DELETE | /api/v1/doacoes/{id} | Excluir doação | 204 |

Swagger: http://localhost:9000/swagger-ui.html  
OpenAPI: http://localhost:9000/v3/api-docs  
Saúde da API e do banco: http://localhost:9000/actuator/health

### Cadastrar doadora

Envie em `POST /api/v1/doadoras`:

```json
{
  "nome": "Ana Silva",
  "email": "ana@example.com",
  "telefone": "11999999999",
  "cidade": "São Paulo",
  "ativa": true
}
```

Anote o ID retornado e use-o ao cadastrar a doação. O banco começa vazio; o ID não é necessariamente 1.

### Cadastrar doação

Envie em `POST /api/v1/doacoes`, substituindo `doadoraId`:

```json
{
  "doadoraId": 1,
  "volumeMl": 250,
  "dataColeta": "2026-10-05",
  "status": "COLETADA",
  "observacoes": "Coleta registrada"
}
```

Status: `AGENDADA`, `COLETADA`, `RECEBIDA` ou `CANCELADA`. A data deve ser `AAAA-MM-DD`; o volume é um inteiro positivo em mililitros. Use os mesmos campos no PUT, com os novos valores.

### Regras e erros

- 400: campos inválidos, volume zero/negativo/fracionado, data inválida, campos desconhecidos ou ID inválido.
- 404: recurso inexistente.
- 409: e-mail duplicado, exclusão de doadora vinculada ou nova doação para doadora inativa.
- 503: falha de acesso ao banco durante uma operação.

Exclua as doações antes de excluir uma doadora. O histórico de doadora inativa pode ser consultado e atualizado. Os estados são registros administrativos, sem validação clínica do leite.

## Testar a API

Com a aplicação iniciada:

```powershell
powershell -ExecutionPolicy Bypass -File .\testar-api.ps1
```

O script cadastra dados fictícios, consulta, atualiza, filtra e exclui os registros que criou. Para outra porta, use `-BaseUrl http://localhost:9001`.

A coleção `postman/Leite-Solidario.postman_collection.json` contém 17 solicitações ordenadas e guarda os IDs retornados. Importe no Postman e execute `Run collection`. O arquivo `docs/api.http` pode ser usado com REST Client no VS Code, ajustando os IDs.

### Conferir no SQL Server

Depois de cadastrar os registros pela API:

```bash
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -C -b -d leite_solidario -Q "SELECT @@VERSION AS versao; SELECT id,nome,email FROM doadoras; SELECT id,doadora_id,volume_ml,status FROM doacoes;"
```

No SSMS, execute `database/verificar-persistencia.sql`.

Reinicie apenas a API e consulte os mesmos IDs para demonstrar persistência:

```bash
docker compose restart api
```

Para parar os serviços sem apagar os dados: `docker compose down`.

## Testes automatizados

Testes de serviços e HTTP:

```powershell
.\mvnw.cmd clean verify
```

Testes com SQL Server real: crie um banco exclusivo de teste e execute o perfil de integração:

```bash
docker compose run --rm -v ./database/init-test.sql:/init.sql:ro db-init
```

```powershell
$env:IT_DB_PASSWORD='LeiteLocal_2026!'
.\mvnw.cmd -Psqlserver-it verify
```

No Linux: `IT_DB_PASSWORD='LeiteLocal_2026!' ./mvnw -Psqlserver-it verify`.

Para outro servidor, defina `IT_DB_URL` e `IT_DB_USERNAME`. O banco deve terminar com `_test`; os testes limpam suas tabelas antes de cada cenário. Não use o banco de demonstração nesses testes.

**Resultado da revisão:** 49 testes de código e 8 de integração aprovados. O JAR foi executado com SQL Server 2025 Developer real, incluindo o script PowerShell, as 17 solicitações da coleção e a persistência após reiniciar a API. O Compose fornece SQL Server 2022 e foi conferido estruturalmente; o fluxo completo Docker não foi executado nesta revisão. Os detalhes estão em `docs/VALIDACAO.md`.

## Publicação e entrega

O código precisa estar publicado em https://github.com/JoaoCastro768/leite-solidario-api antes da entrega do TXT. Crie o repositório com esse nome, sem README inicial, e execute na pasta do projeto:

```bash
git init -b main
git add .
git commit -m "Implementa API de doadoras e doacoes com SQL Server"
git remote add origin https://github.com/JoaoCastro768/leite-solidario-api.git
git push -u origin main
```

`.env`, `target` e `dist` estão no `.gitignore`. O GitHub Actions executa os testes com SQL Server após a publicação.

No Portal do Aluno, envie somente `ENTREGA_CP2_RM554628.txt`, com o link, João Castro e RM 554628. Confira que o repositório abre e corresponde à versão demonstrada. Se alterar o nome do repositório, atualize o TXT e o README.

## Problemas de execução

| Problema | Verificação |
|---|---|
| Docker Desktop não inicia | Confira a mensagem de WSL/virtualização na janela do aplicativo |
| Porta ocupada | Altere `API_PORT` ou `SQLSERVER_PORT` no `.env` e use a porta correspondente |
| Banco não fica saudável | Execute `docker compose logs sqlserver` e confira senha e memória |
| Login failed for user sa | Confira a senha real da instância e `DB_PASSWORD` |
| Cannot open database | Crie o banco indicado em `DB_URL`; confira o log de `db-init` |
| Erro de certificado local | Confira `encrypt=true;trustServerCertificate=true` na URL local |
| DB_PASSWORD não definido | Configure no terminal para Java local ou execute pelo Compose |
| Wrapper sem permissão no Linux | Execute `chmod +x mvnw iniciar.sh` |

Enunciado: https://github.com/acnaweb/microservices-2026/blob/main/docs/3si/checkpoint2-sem2-2026.md
