USE leite_solidario;
GO
SELECT @@VERSION AS versao_sql_server, DB_NAME() AS banco_atual;
SELECT id, nome, email, telefone, cidade, ativa, data_cadastro FROM dbo.doadoras ORDER BY id;
SELECT id, doadora_id, volume_ml, data_coleta, status, observacoes FROM dbo.doacoes ORDER BY id;
GO
