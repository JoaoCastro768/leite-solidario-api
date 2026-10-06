package br.com.fiap.leitesolidario.dto;

import br.com.fiap.leitesolidario.entity.StatusDoacao;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record DoacaoRequest(
        @NotNull(message = "Doadora é obrigatória") @Positive(message = "ID da doadora deve ser positivo") Long doadoraId,
        @NotNull(message = "Volume é obrigatório") @Positive(message = "Volume deve ser maior que zero") Integer volumeMl,
        @NotNull(message = "Data de coleta é obrigatória") LocalDate dataColeta,
        @NotNull(message = "Status é obrigatório") StatusDoacao status,
        @Size(max = 500) String observacoes
) {}
