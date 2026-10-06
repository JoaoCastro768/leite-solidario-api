package br.com.fiap.leitesolidario.dto;

import br.com.fiap.leitesolidario.entity.Doacao;
import br.com.fiap.leitesolidario.entity.StatusDoacao;
import java.time.LocalDate;

public record DoacaoResponse(Long id, Long doadoraId, String nomeDoadora, Integer volumeMl,
                             LocalDate dataColeta, StatusDoacao status, String observacoes) {
    public static DoacaoResponse from(Doacao doacao) {
        return new DoacaoResponse(doacao.getId(), doacao.getDoadora().getId(), doacao.getDoadora().getNome(),
                doacao.getVolumeMl(), doacao.getDataColeta(), doacao.getStatus(), doacao.getObservacoes());
    }
}
