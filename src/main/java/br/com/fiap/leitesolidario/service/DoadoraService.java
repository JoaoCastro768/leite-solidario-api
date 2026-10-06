package br.com.fiap.leitesolidario.service;

import br.com.fiap.leitesolidario.dto.*;
import br.com.fiap.leitesolidario.entity.Doadora;
import br.com.fiap.leitesolidario.exception.*;
import br.com.fiap.leitesolidario.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class DoadoraService {
    private final DoadoraRepository repository;
    private final DoacaoRepository doacaoRepository;

    public DoadoraService(DoadoraRepository repository, DoacaoRepository doacaoRepository) {
        this.repository = repository;
        this.doacaoRepository = doacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<DoadoraResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(DoadoraResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoadoraResponse buscar(Long id) { return DoadoraResponse.from(encontrar(id)); }

    @Transactional(readOnly = true)
    public Doadora encontrar(Long id) {
        validarId(id);
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doadora não encontrada"));
    }

    public DoadoraResponse criar(DoadoraRequest request) {
        String email = normalizarEmail(request.email());
        if (repository.existsByEmail(email)) throw new ConflitoException("E-mail já cadastrado");
        Doadora doadora = new Doadora();
        preencher(doadora, request, email);
        return DoadoraResponse.from(repository.saveAndFlush(doadora));
    }

    public DoadoraResponse atualizar(Long id, DoadoraRequest request) {
        Doadora doadora = encontrar(id);
        String email = normalizarEmail(request.email());
        if (repository.existsByEmailAndIdNot(email, id)) throw new ConflitoException("E-mail já cadastrado");
        preencher(doadora, request, email);
        return DoadoraResponse.from(repository.saveAndFlush(doadora));
    }

    public void excluir(Long id) {
        Doadora doadora = encontrar(id);
        if (doacaoRepository.existsByDoadoraId(id)) {
            throw new ConflitoException("Exclua as doações vinculadas antes de excluir a doadora");
        }
        repository.delete(doadora);
        repository.flush();
    }

    public static void validarId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID deve ser maior que zero");
    }

    private String normalizarEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }

    private void preencher(Doadora doadora, DoadoraRequest request, String email) {
        doadora.setNome(request.nome().strip());
        doadora.setEmail(email);
        doadora.setTelefone(request.telefone().strip());
        doadora.setCidade(request.cidade().strip());
        doadora.setAtiva(request.ativa());
    }
}
