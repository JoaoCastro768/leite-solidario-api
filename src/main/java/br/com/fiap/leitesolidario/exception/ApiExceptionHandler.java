package br.com.fiap.leitesolidario.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ApiError(OffsetDateTime timestamp, int status, String erro,
                           String mensagem, String caminho, Map<String, String> campos) {}

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiError> conflito(ConflitoException e, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridade(DataIntegrityViolationException e, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Operação viola uma restrição do banco de dados", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", request, campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> formato(Exception e, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON ou parâmetro inválido. Confira campos, tipos, data e status", request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> argumento(IllegalArgumentException e, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> banco(DataAccessException e, HttpServletRequest request) {
        log.error("Falha no acesso ao banco", e);
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível", request, Map.of());
    }

    private ResponseEntity<ApiError> resposta(HttpStatus status, String mensagem,
                                            HttpServletRequest request, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(),
                status.getReasonPhrase(), mensagem, request.getRequestURI(), campos));
    }
}
