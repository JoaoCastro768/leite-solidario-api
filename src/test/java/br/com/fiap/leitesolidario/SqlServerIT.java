package br.com.fiap.leitesolidario;

import br.com.fiap.leitesolidario.dto.*;
import br.com.fiap.leitesolidario.entity.StatusDoacao;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=${IT_DB_URL:jdbc:sqlserver://localhost:1433;databaseName=leite_solidario_test;encrypt=true;trustServerCertificate=true}",
        "spring.datasource.username=${IT_DB_USERNAME:sa}",
        "spring.datasource.password=${IT_DB_PASSWORD:}"
})
class SqlServerIT {
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limparBancoDeTeste() {
        String database = jdbc.queryForObject("SELECT DB_NAME()", String.class);
        assertNotNull(database);
        assertTrue(database.endsWith("_test"), "Use um banco exclusivo cujo nome termine com _test");
        jdbc.update("DELETE FROM doacoes");
        jdbc.update("DELETE FROM doadoras");
    }

    @Test
    void crudDoadoraPersisteNoSqlServer() {
        DoadoraResponse criada = cadastrar(true);
        assertEquals("Ana", jdbc.queryForObject("SELECT nome FROM doadoras WHERE id = ?", String.class, criada.id()));
        assertEquals(HttpStatus.OK, http.getForEntity("/api/v1/doadoras/" + criada.id(), DoadoraResponse.class).getStatusCode());
        var atualizada = http.exchange("/api/v1/doadoras/" + criada.id(), HttpMethod.PUT,
                new HttpEntity<>(new DoadoraRequest("Ana Maria", "ana@example.com", "11999999999", "Santos", true)), DoadoraResponse.class);
        assertEquals(HttpStatus.OK, atualizada.getStatusCode());
        assertEquals("Santos", jdbc.queryForObject("SELECT cidade FROM doadoras WHERE id = ?", String.class, criada.id()));
        assertEquals(HttpStatus.NO_CONTENT, http.exchange("/api/v1/doadoras/" + criada.id(), HttpMethod.DELETE, null, Void.class).getStatusCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM doadoras WHERE id = ?", Integer.class, criada.id()));
    }

    @Test
    void crudDoacaoPersisteNoSqlServer() {
        var doadora = cadastrar(true);
        var criada = http.postForEntity("/api/v1/doacoes", doacao(doadora.id(), 250), DoacaoResponse.class);
        assertEquals(HttpStatus.CREATED, criada.getStatusCode());
        assertNotNull(criada.getBody());
        Long id = criada.getBody().id();
        assertEquals(250, jdbc.queryForObject("SELECT volume_ml FROM doacoes WHERE id = ?", Integer.class, id));
        assertEquals(HttpStatus.OK, http.getForEntity("/api/v1/doacoes/" + id, DoacaoResponse.class).getStatusCode());
        var atualizada = http.exchange("/api/v1/doacoes/" + id, HttpMethod.PUT, new HttpEntity<>(doacao(doadora.id(), 350)), DoacaoResponse.class);
        assertEquals(HttpStatus.OK, atualizada.getStatusCode());
        assertEquals(350, jdbc.queryForObject("SELECT volume_ml FROM doacoes WHERE id = ?", Integer.class, id));
        assertEquals(HttpStatus.NO_CONTENT, http.exchange("/api/v1/doacoes/" + id, HttpMethod.DELETE, null, Void.class).getStatusCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM doacoes WHERE id = ?", Integer.class, id));
    }

    @Test
    void emailDuplicadoRetorna409() {
        cadastrar(true);
        assertEquals(HttpStatus.CONFLICT, http.postForEntity("/api/v1/doadoras", doadora(true), Map.class).getStatusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM doadoras", Integer.class));
    }

    @Test
    void excluirDoadoraVinculadaRetorna409() {
        var doadora = cadastrar(true);
        http.postForEntity("/api/v1/doacoes", doacao(doadora.id(), 250), DoacaoResponse.class);
        assertEquals(HttpStatus.CONFLICT, http.exchange("/api/v1/doadoras/" + doadora.id(), HttpMethod.DELETE, null, Map.class).getStatusCode());
    }

    @Test
    void doadoraInativaRetorna409() {
        var doadora = cadastrar(false);
        assertEquals(HttpStatus.CONFLICT, http.postForEntity("/api/v1/doacoes", doacao(doadora.id(), 250), Map.class).getStatusCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM doacoes", Integer.class));
    }

    @Test
    void volumeInvalidoRetorna400() {
        var doadora = cadastrar(true);
        assertEquals(HttpStatus.BAD_REQUEST, http.postForEntity("/api/v1/doacoes", doacao(doadora.id(), 0), Map.class).getStatusCode());
    }

    @Test
    void recursoAusenteRetorna404() {
        assertEquals(HttpStatus.NOT_FOUND, http.getForEntity("/api/v1/doadoras/9223372036854775807", Map.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.postForEntity("/api/v1/doacoes", doacao(Long.MAX_VALUE, 250), Map.class).getStatusCode());
    }

    @Test
    void swaggerESaudeEstaoDisponiveis() {
        assertEquals(HttpStatus.OK, http.getForEntity("/swagger-ui/index.html", String.class).getStatusCode());
        var docs = http.getForEntity("/v3/api-docs", Map.class);
        assertEquals(HttpStatus.OK, docs.getStatusCode());
        assertNotNull(docs.getBody());
        assertTrue(docs.getBody().containsKey("paths"));
        assertEquals(HttpStatus.OK, http.getForEntity("/actuator/health", Map.class).getStatusCode());
    }

    private DoadoraResponse cadastrar(boolean ativa) {
        var response = http.postForEntity("/api/v1/doadoras", doadora(ativa), DoadoraResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private DoadoraRequest doadora(boolean ativa) {
        return new DoadoraRequest("Ana", "ana@example.com", "11999999999", "São Paulo", ativa);
    }

    private DoacaoRequest doacao(Long id, int volume) {
        return new DoacaoRequest(id, volume, LocalDate.of(2026, 10, 5), StatusDoacao.COLETADA, "Cadastro de teste");
    }
}
