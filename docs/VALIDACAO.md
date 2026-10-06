# Validação

Revisão: 05/10/2026.

## Ambiente

- Java: OpenJDK 17.0.20.
- Maven: 3.9.11.
- Spring Boot: 3.5.16.
- Banco de validação: SQL Server 2025 RTM-CU7, versão 17.0.4065.4, Enterprise Developer Edition, Ubuntu 24.04.
- Script PowerShell executado em PowerShell 7.6.2. Os scripts foram salvos com UTF-8 BOM para leitura dos acentos pelo Windows PowerShell 5.1.

## Resultados

| Verificação | Resultado |
|---|---|
| Compilação e empacotamento | BUILD SUCCESS |
| Testes de serviços e Controllers | 49 aprovados, sem falhas ou erros |
| Testes de integração com SQL Server real | 8 aprovados, sem falhas ou erros |
| Script testar-api.ps1 contra JAR e banco reais | Aprovado |
| 17 solicitações da coleção executadas por HTTP | Todas com o status esperado |
| Consulta SQL direta dos dados inseridos pela API | Aprovada |
| Reinício do JAR e consulta dos mesmos registros | Dados mantidos |
| Swagger e OpenAPI com a aplicação iniciada | Disponíveis |
| Sintaxe dos scripts PowerShell e shell | Válida |
| Fluxo iniciar.ps1 com comandos simulados | 7 cenários aprovados |

Os testes de integração verificam inserção, consulta, atualização e exclusão diretamente nas tabelas. As requisições HTTP conferem os dois CRUDs, filtro, e-mail duplicado, exclusão com vínculo e validação do volume. A persistência foi conferida depois de encerrar e iniciar novamente o processo da API.

Exemplo da consulta SQL direta realizada:

```text
id=3 nome=Persistência João cidade=São Paulo volume_ml=275 status=COLETADA
```

Os dados fictícios usados no fluxo de demonstração foram excluídos ao término da verificação.

## Correções

- Inicialização no Windows verifica o mecanismo Docker, tenta abrir o Desktop e aguarda a saúde da API.
- Mensagens dos scripts preservam acentos.
- Telefone exige pelo menos 8 dígitos; apenas símbolos não são aceitos.
- Volume fracionado é rejeitado, evitando conversão silenciosa para inteiro.
- Arquivo de configuração, coleção e JAR correspondem aos fontes revisados.

## Limites da validação

O banco foi executado diretamente no Linux, sem Docker. O Compose fornece SQL Server 2022; sua estrutura e comandos foram revisados, mas a execução completa dos contêineres não foi realizada. Os cenários do script de inicialização foram testados com comandos simulados; não comprovam a configuração do Docker Desktop de outra máquina.

O endereço no TXT é o repositório definido para publicação. A revisão não publicou o código nem executou o GitHub Actions. A entrega no Portal depende de esse endereço estar publicado e acessível.
