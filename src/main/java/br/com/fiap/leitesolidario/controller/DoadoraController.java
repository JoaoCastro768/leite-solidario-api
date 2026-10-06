package br.com.fiap.leitesolidario.controller;

import br.com.fiap.leitesolidario.dto.*;
import br.com.fiap.leitesolidario.service.DoadoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doadoras")
@Tag(name = "Doadoras", description = "Operações de cadastro, consulta, alteração e exclusão")
public class DoadoraController {
    private final DoadoraService service;

    public DoadoraController(DoadoraService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar doadoras")
    public List<DoadoraResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pelo ID")
    public DoadoraResponse buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @Operation(summary = "Cadastrar")
    public ResponseEntity<DoadoraResponse> criar(@Valid @RequestBody DoadoraRequest request) {
        DoadoraResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar todos os campos editáveis")
    public DoadoraResponse atualizar(@PathVariable Long id, @Valid @RequestBody DoadoraRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
