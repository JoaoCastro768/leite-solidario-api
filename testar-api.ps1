param([string]$BaseUrl = "http://localhost:9000")
$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')
$health = Invoke-RestMethod "$BaseUrl/actuator/health"
if ($health.status -ne 'UP') { throw 'API ou SQL Server indisponível' }
$email = "teste-$([guid]::NewGuid().ToString('N'))@example.com"
$doadora = @{ nome = 'Doadora de Teste'; email = $email; telefone = '11999999999'; cidade = 'São Paulo'; ativa = $true }
$criada = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/doadoras" -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes(($doadora | ConvertTo-Json)))
if (-not $criada.id) { throw 'Cadastro de doadora não devolveu ID' }
$consulta = Invoke-RestMethod "$BaseUrl/api/v1/doadoras/$($criada.id)"
if ($consulta.email -ne $email) { throw 'Consulta de doadora divergente' }
$doadora.cidade = 'Santos'
$atualizada = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/v1/doadoras/$($criada.id)" -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes(($doadora | ConvertTo-Json)))
if ($atualizada.cidade -ne 'Santos') { throw 'Atualização de doadora divergente' }
$doacao = @{ doadoraId = $criada.id; volumeMl = 250; dataColeta = '2026-10-05'; status = 'COLETADA'; observacoes = 'Teste automatizado' }
$registrada = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/doacoes" -ContentType 'application/json' -Body ($doacao | ConvertTo-Json)
if (-not $registrada.id) { throw 'Cadastro de doação não devolveu ID' }
$doacao.volumeMl = 350
$doacao.status = 'RECEBIDA'
$alterada = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/v1/doacoes/$($registrada.id)" -ContentType 'application/json' -Body ($doacao | ConvertTo-Json)
if ($alterada.volumeMl -ne 350) { throw 'Atualização de doação divergente' }
$consultaDoacao = Invoke-RestMethod "$BaseUrl/api/v1/doacoes/$($registrada.id)"
if ($consultaDoacao.status -ne 'RECEBIDA') { throw 'Consulta de doação divergente' }
$lista = @(Invoke-RestMethod "$BaseUrl/api/v1/doacoes?doadoraId=$($criada.id)")
if (-not ($lista | Where-Object { $_.id -eq $registrada.id })) { throw 'Filtro de doações divergente' }
Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/v1/doacoes/$($registrada.id)" | Out-Null
Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/v1/doadoras/$($criada.id)" | Out-Null
Write-Host 'OK: saúde, cadastro, consulta, atualização, filtro e exclusão.'
