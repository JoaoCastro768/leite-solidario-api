# Demonstração do Check Point 2

João Castro — RM 554628.

1. Inicie o projeto conforme o README. Abra `http://localhost:9000` e o SQL Server Management Studio ou um terminal SQL.
2. Explique o objetivo: registrar doadoras e doações de leite humano, mantendo os dados no SQL Server.
3. Mostre `pom.xml`: Java 17, Spring Boot, Spring Data JPA e driver Microsoft JDBC.
4. Mostre `application.yml`: URL e credenciais por variáveis, driver SQL Server e porta 9000.
5. No Swagger, cadastre uma doadora e anote o ID retornado. Mostre o código HTTP 201 e o cabeçalho Location.
6. Consulte a doadora na lista e pelo ID. Cadastre uma doação vinculada usando o ID retornado.
7. Atualize o volume/status da doação usando PUT. Mostre GET com o valor atualizado.
8. Execute as consultas de `database/verificar-persistencia.sql`. Mostre `@@VERSION`, o nome do banco e os mesmos IDs gravados pela API.
9. Reinicie apenas a API e consulte os registros novamente para mostrar persistência.
10. Tente excluir a doadora enquanto houver doações: deve retornar 409. Exclua a doação, depois a doadora: deve retornar 204. Mostre a ausência dos registros nas tabelas.
11. Mostre uma validação inválida, por exemplo volume zero: retorno 400 com o campo informado.
12. Abra um Controller, Service e Repository. Explique o fluxo e o relacionamento JPA entre `Doacao` e `Doadora`.
13. Confira que o código demonstrado é o publicado e envie o TXT no Portal.

Use dados fictícios. Não há exigência de vídeo no enunciado enviado; este roteiro serve à avaliação presencial ou à demonstração solicitada pelo professor.
