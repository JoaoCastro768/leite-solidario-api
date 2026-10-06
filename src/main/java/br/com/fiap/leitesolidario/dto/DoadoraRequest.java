package br.com.fiap.leitesolidario.dto;

import jakarta.validation.constraints.*;

public record DoadoraRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 160) String email,
        @NotBlank(message = "Telefone é obrigatório") @Pattern(regexp = "(?=(?:[^0-9]*[0-9]){8})[0-9+() .-]{8,20}", message = "Telefone deve ter de 8 a 20 caracteres válidos") String telefone,
        @NotBlank(message = "Cidade é obrigatória") @Size(max = 100) String cidade,
        @NotNull(message = "Ativa é obrigatório") Boolean ativa
) {}
