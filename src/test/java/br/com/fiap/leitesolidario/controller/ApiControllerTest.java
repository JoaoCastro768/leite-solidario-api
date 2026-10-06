package br.com.fiap.leitesolidario.controller;

import br.com.fiap.leitesolidario.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import br.com.fiap.leitesolidario.entity.StatusDoacao;
import br.com.fiap.leitesolidario.exception.*;
import br.com.fiap.leitesolidario.service.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.*;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiControllerTest {
    private MockMvc mvc;
    private DoadoraService doadoras;
    private DoacaoService doacoes;
    private static final String DOADORA = """
        {"nome":"Ana","email":"ana@example.com","telefone":"11999999999","cidade":"São Paulo","ativa":true}
        """;

    @BeforeEach
    void configurar() {
        doadoras = mock(DoadoraService.class);
        doacoes = mock(DoacaoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new DoadoraController(doadoras), new DoacaoController(doacoes))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules()
                        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT))).build();
    }

    @Test
    void cadastroRetorna201ELocation() throws Exception {
        when(doadoras.criar(any())).thenReturn(response());
        mvc.perform(post("/api/v1/doadoras").contentType(MediaType.APPLICATION_JSON).content(DOADORA))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/v1/doadoras/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void consultaListaRetornaJson() throws Exception {
        when(doadoras.listar()).thenReturn(List.of(response()));
        mvc.perform(get("/api/v1/doadoras")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void consultaIndividualRetorna200() throws Exception {
        when(doadoras.buscar(7L)).thenReturn(response());
        mvc.perform(get("/api/v1/doadoras/7")).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void atualizacaoRetorna200() throws Exception {
        when(doadoras.atualizar(eq(7L), any())).thenReturn(response());
        mvc.perform(put("/api/v1/doadoras/7").contentType(MediaType.APPLICATION_JSON).content(DOADORA))
                .andExpect(status().isOk());
        verify(doadoras).atualizar(eq(7L), any());
    }

    @Test
    void exclusaoRetorna204SemCorpo() throws Exception {
        mvc.perform(delete("/api/v1/doacoes/7")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(doacoes).excluir(7L);
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        mvc.perform(post("/api/v1/doadoras").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nome").exists());
        verifyNoInteractions(doadoras);
    }

    @Test
    void volumeZeroRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON).content("""
            {"doadoraId":1,"volumeMl":0,"dataColeta":"2026-10-05","status":"AGENDADA"}
            """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.volumeMl").exists());
        verifyNoInteractions(doacoes);
    }

    @Test
    void enumInvalidoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON).content("""
            {"doadoraId":1,"volumeMl":200,"dataColeta":"2026-10-05","status":"INVALIDO"}
            """)).andExpect(status().isBadRequest());
        verifyNoInteractions(doacoes);
    }

    @Test
    void idInexistenteRetorna404() throws Exception {
        when(doadoras.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Doadora não encontrada"));
        mvc.perform(get("/api/v1/doadoras/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Doadora não encontrada"));
    }

    @Test
    void conflitoRetorna409() throws Exception {
        doThrow(new ConflitoException("Doadora possui doações")).when(doadoras).excluir(7L);
        mvc.perform(delete("/api/v1/doadoras/7")).andExpect(status().isConflict());
    }

    @Test
    void filtroEncaminhaIdDaDoadora() throws Exception {
        when(doacoes.listar(7L)).thenReturn(List.of(new DoacaoResponse(2L, 7L, "Ana", 200,
                LocalDate.of(2026, 10, 5), StatusDoacao.AGENDADA, null)));
        mvc.perform(get("/api/v1/doacoes?doadoraId=7")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doadoraId").value(7));
    }

    @Test
    void parametroComTipoInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/v1/doadoras/abc")).andExpect(status().isBadRequest());
    }


    @Test
    void criarDoacaoRetorna201ELocation() throws Exception {
        when(doacoes.criar(any())).thenReturn(doacaoResponse());
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(250)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/v1/doacoes/2"))
                .andExpect(jsonPath("$.doadoraId").value(7));
    }

    @Test
    void atualizarDoacaoRetorna200() throws Exception {
        when(doacoes.atualizar(eq(2L), any())).thenReturn(doacaoResponse());
        mvc.perform(put("/api/v1/doacoes/2").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(250)))
                .andExpect(status().isOk());
        verify(doacoes).atualizar(eq(2L), any());
    }

    @Test
    void consultarDoacaoRetorna200() throws Exception {
        when(doacoes.buscar(2L)).thenReturn(doacaoResponse());
        mvc.perform(get("/api/v1/doacoes/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeMl").value(250));
    }

    @Test
    void listarDoacoesSemFiltroEncaminhaNull() throws Exception {
        when(doacoes.listar(null)).thenReturn(List.of(doacaoResponse()));
        mvc.perform(get("/api/v1/doacoes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(2));
        verify(doacoes).listar(null);
    }

    @Test
    void excluirDoadoraRetorna204() throws Exception {
        mvc.perform(delete("/api/v1/doadoras/7")).andExpect(status().isNoContent());
        verify(doadoras).excluir(7L);
    }

    @Test
    void emailInvalidoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doadoras").contentType(MediaType.APPLICATION_JSON)
                .content(DOADORA.replace("ana@example.com", "sem-arroba")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void telefoneSemDigitosRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doadoras").contentType(MediaType.APPLICATION_JSON)
                .content(DOADORA.replace("11999999999", "----------")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.telefone").exists());
    }

    @Test
    void volumeFracionadoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON)
                .content(doacaoJson(250).replace("250", "250.5"))).andExpect(status().isBadRequest());
        verifyNoInteractions(doacoes);
    }

    @Test
    void campoDesconhecidoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON)
                .content(doacaoJson(250).replace("}", ",\"outroCampo\":1}"))).andExpect(status().isBadRequest());
        verifyNoInteractions(doacoes);
    }

    @Test
    void dataImpossivelRetorna400() throws Exception {
        mvc.perform(post("/api/v1/doacoes").contentType(MediaType.APPLICATION_JSON)
                .content(doacaoJson(250).replace("2026-10-05", "2026-02-30"))).andExpect(status().isBadRequest());
        verifyNoInteractions(doacoes);
    }

    @Test
    void violacaoDeIntegridadeRetorna409() throws Exception {
        when(doadoras.criar(any())).thenThrow(new DataIntegrityViolationException("SQL interno"));
        mvc.perform(post("/api/v1/doadoras").contentType(MediaType.APPLICATION_JSON).content(DOADORA))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Operação viola uma restrição do banco de dados"));
    }

    @Test
    void bancoIndisponivelRetorna503() throws Exception {
        when(doadoras.listar()).thenThrow(new DataAccessResourceFailureException("Conexão indisponível"));
        mvc.perform(get("/api/v1/doadoras")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensagem").value("Banco de dados indisponível"));
    }

    @Test
    void argumentoInvalidoRetorna400() throws Exception {
        when(doadoras.buscar(0L)).thenThrow(new IllegalArgumentException("ID deve ser maior que zero"));
        mvc.perform(get("/api/v1/doadoras/0")).andExpect(status().isBadRequest());
    }

    private String doacaoJson(int volume) {
        return "{\"doadoraId\":7,\"volumeMl\":" + volume + ",\"dataColeta\":\"2026-10-05\",\"status\":\"COLETADA\"}";
    }

    private DoacaoResponse doacaoResponse() {
        return new DoacaoResponse(2L, 7L, "Ana", 250, LocalDate.of(2026, 10, 5), StatusDoacao.COLETADA, null);
    }

    private DoadoraResponse response() {
        return new DoadoraResponse(7L, "Ana", "ana@example.com", "11999999999", "São Paulo", true,
                LocalDateTime.of(2026, 10, 5, 12, 0));
    }
}
