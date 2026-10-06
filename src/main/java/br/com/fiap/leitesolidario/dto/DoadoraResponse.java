package br.com.fiap.leitesolidario.dto;

import br.com.fiap.leitesolidario.entity.Doadora;
import java.time.LocalDateTime;

public record DoadoraResponse(Long id, String nome, String email, String telefone, String cidade,
                              boolean ativa, LocalDateTime dataCadastro) {
    public static DoadoraResponse from(Doadora doadora) {
        return new DoadoraResponse(doadora.getId(), doadora.getNome(), doadora.getEmail(),
                doadora.getTelefone(), doadora.getCidade(), doadora.isAtiva(), doadora.getDataCadastro());
    }
}
