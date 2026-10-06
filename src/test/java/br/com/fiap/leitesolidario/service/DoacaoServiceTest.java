package br.com.fiap.leitesolidario.service;

import br.com.fiap.leitesolidario.dto.DoacaoRequest;
import br.com.fiap.leitesolidario.entity.*;
import br.com.fiap.leitesolidario.exception.*;
import br.com.fiap.leitesolidario.repository.DoacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {
    @Mock DoacaoRepository repository;
    @Mock DoadoraService doadoraService;
    @InjectMocks DoacaoService service;

    @Test
    void doadoraInativaNaoRecebeNovaDoacao() {
        when(doadoraService.encontrar(1L)).thenReturn(new Doadora());
        assertThrows(ConflitoException.class, () -> service.criar(request()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void doadoraInexistenteNaoRecebeDoacao() {
        when(doadoraService.encontrar(1L)).thenThrow(new RecursoNaoEncontradoException("Doadora não encontrada"));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(request()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void filtroVerificaExistenciaDaDoadora() {
        when(doadoraService.encontrar(9L)).thenThrow(new RecursoNaoEncontradoException("Doadora não encontrada"));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(9L));
        verifyNoInteractions(repository);
    }


    @Test
    void criaDoacaoComDoadoraAtiva() {
        Doadora doadora = doadora(1L, true);
        when(doadoraService.encontrar(1L)).thenReturn(doadora);
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            Doacao doacao = i.getArgument(0);
            ReflectionTestUtils.setField(doacao, "id", 2L);
            return doacao;
        });
        var response = service.criar(new DoacaoRequest(1L, 300, LocalDate.of(2026, 10, 5), StatusDoacao.COLETADA, " Coleta "));
        assertEquals(2L, response.id());
        assertEquals(1L, response.doadoraId());
        assertEquals(300, response.volumeMl());
        assertEquals("Coleta", response.observacoes());
    }

    @Test
    void consultaDevolveDoacao() {
        when(repository.findById(2L)).thenReturn(Optional.of(doacao(doadora(1L, true))));
        var response = service.buscar(2L);
        assertEquals(200, response.volumeMl());
        assertEquals("Ana", response.nomeDoadora());
    }

    @Test
    void listaSemFiltroDevolveDoacoes() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(doacao(doadora(1L, true))));
        assertEquals(1, service.listar(null).size());
        verifyNoInteractions(doadoraService);
    }

    @Test
    void filtroDevolveSomenteDoacoesDaDoadora() {
        Doadora doadora = doadora(1L, true);
        when(doadoraService.encontrar(1L)).thenReturn(doadora);
        when(repository.findByDoadoraIdOrderByIdAsc(1L)).thenReturn(List.of(doacao(doadora)));
        assertEquals(1L, service.listar(1L).get(0).doadoraId());
    }

    @Test
    void permiteAtualizarHistoricoDaDoadoraInativa() {
        Doadora doadora = doadora(1L, false);
        Doacao doacao = doacao(doadora);
        when(repository.findById(2L)).thenReturn(Optional.of(doacao));
        when(doadoraService.encontrar(1L)).thenReturn(doadora);
        when(repository.saveAndFlush(doacao)).thenReturn(doacao);
        var response = service.atualizar(2L, new DoacaoRequest(1L, 350, LocalDate.of(2026, 10, 5), StatusDoacao.RECEBIDA, null));
        assertEquals(350, response.volumeMl());
        assertEquals(StatusDoacao.RECEBIDA, response.status());
    }

    @Test
    void impedeTransferirDoacaoParaDoadoraInativa() {
        when(repository.findById(2L)).thenReturn(Optional.of(doacao(doadora(1L, true))));
        when(doadoraService.encontrar(9L)).thenReturn(doadora(9L, false));
        assertThrows(ConflitoException.class, () -> service.atualizar(2L,
                new DoacaoRequest(9L, 250, LocalDate.of(2026, 10, 5), StatusDoacao.COLETADA, null)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void permiteTransferirDoacaoParaDoadoraAtiva() {
        Doacao doacao = doacao(doadora(1L, true));
        when(repository.findById(2L)).thenReturn(Optional.of(doacao));
        when(doadoraService.encontrar(9L)).thenReturn(doadora(9L, true));
        when(repository.saveAndFlush(doacao)).thenReturn(doacao);
        var response = service.atualizar(2L, new DoacaoRequest(9L, 250, LocalDate.of(2026, 10, 5), StatusDoacao.COLETADA, null));
        assertEquals(9L, response.doadoraId());
    }

    @Test
    void excluiDoacaoExistente() {
        Doacao doacao = doacao(doadora(1L, true));
        when(repository.findById(2L)).thenReturn(Optional.of(doacao));
        service.excluir(2L);
        verify(repository).delete(doacao);
        verify(repository).flush();
    }

    @Test
    void consultaInexistenteRetornaErro() {
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L));
    }

    @Test
    void idNegativoNaoConsultaBanco() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(-1L));
        verifyNoInteractions(repository);
    }

    private Doadora doadora(Long id, boolean ativa) {
        Doadora doadora = new Doadora();
        ReflectionTestUtils.setField(doadora, "id", id);
        doadora.setNome("Ana");
        doadora.setAtiva(ativa);
        return doadora;
    }

    private Doacao doacao(Doadora doadora) {
        Doacao doacao = new Doacao();
        ReflectionTestUtils.setField(doacao, "id", 2L);
        doacao.setDoadora(doadora);
        doacao.setVolumeMl(200);
        doacao.setDataColeta(LocalDate.of(2026, 10, 5));
        doacao.setStatus(StatusDoacao.AGENDADA);
        return doacao;
    }

    private DoacaoRequest request() {
        return new DoacaoRequest(1L, 200, LocalDate.of(2026, 10, 5), StatusDoacao.AGENDADA, null);
    }
}
