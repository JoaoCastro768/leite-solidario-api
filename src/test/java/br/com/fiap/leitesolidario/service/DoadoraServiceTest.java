package br.com.fiap.leitesolidario.service;

import br.com.fiap.leitesolidario.dto.DoadoraRequest;
import br.com.fiap.leitesolidario.entity.Doadora;
import br.com.fiap.leitesolidario.exception.*;
import br.com.fiap.leitesolidario.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoadoraServiceTest {
    @Mock DoadoraRepository repository;
    @Mock DoacaoRepository doacaoRepository;
    @InjectMocks DoadoraService service;

    @Test
    void emailRepetidoImpedeInsercao() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(true);
        assertThrows(ConflitoException.class, () -> service.criar(request("ANA@example.com")));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void normalizaEmailENomeAntesDeSalvar() {
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.criar(request("ANA@example.com"));
        assertEquals("Ana", response.nome());
        assertEquals("ana@example.com", response.email());
    }

    @Test
    void doadoraComDoacoesNaoPodeSerExcluida() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Doadora()));
        when(doacaoRepository.existsByDoadoraId(1L)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> service.excluir(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void idInexistenteRetornaErro() {
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(123L));
    }

    @Test
    void idNaoPositivoNaoConsultaBanco() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(0L));
        verifyNoInteractions(repository);
    }

    @Test
    void atualizacaoComEmailRepetidoNaoGrava() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Doadora()));
        when(repository.existsByEmailAndIdNot("ana@example.com", 1L)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> service.atualizar(1L, request("ana@example.com")));
        verify(repository, never()).saveAndFlush(any());
    }


    @Test
    void listaDevolveCamposDaDoadora() {
        Doadora doadora = doadora();
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(doadora));
        var lista = service.listar();
        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).id());
        assertEquals("Ana", lista.get(0).nome());
    }

    @Test
    void consultaIndividualDevolveDados() {
        when(repository.findById(1L)).thenReturn(Optional.of(doadora()));
        var response = service.buscar(1L);
        assertEquals("ana@example.com", response.email());
        assertNotNull(response.dataCadastro());
    }

    @Test
    void atualizaDadosSemAlterarIdEDataCadastro() {
        Doadora doadora = doadora();
        var cadastro = doadora.getDataCadastro();
        when(repository.findById(1L)).thenReturn(Optional.of(doadora));
        when(repository.saveAndFlush(doadora)).thenReturn(doadora);
        var response = service.atualizar(1L, new DoadoraRequest("Ana Maria", "novo@example.com", "11988888888", "Santos", false));
        assertEquals(1L, response.id());
        assertEquals(cadastro, response.dataCadastro());
        assertEquals("Santos", response.cidade());
        assertFalse(response.ativa());
    }

    @Test
    void excluiDoadoraSemDoacoes() {
        Doadora doadora = doadora();
        when(repository.findById(1L)).thenReturn(Optional.of(doadora));
        service.excluir(1L);
        verify(repository).delete(doadora);
        verify(repository).flush();
    }

    @Test
    void idNuloNaoConsultaBanco() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(null));
        verifyNoInteractions(repository);
    }

    private Doadora doadora() {
        Doadora doadora = new Doadora();
        ReflectionTestUtils.setField(doadora, "id", 1L);
        doadora.setNome("Ana");
        doadora.setEmail("ana@example.com");
        doadora.setTelefone("11999999999");
        doadora.setCidade("São Paulo");
        doadora.setAtiva(true);
        doadora.definirDataCadastro();
        return doadora;
    }

    private DoadoraRequest request(String email) {
        return new DoadoraRequest(" Ana ", email, "11999999999", "São Paulo", true);
    }
}
