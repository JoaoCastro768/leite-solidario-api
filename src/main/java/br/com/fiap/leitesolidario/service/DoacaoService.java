package br.com.fiap.leitesolidario.service;

import br.com.fiap.leitesolidario.dto.*;
import br.com.fiap.leitesolidario.entity.*;
import br.com.fiap.leitesolidario.exception.*;
import br.com.fiap.leitesolidario.repository.DoacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class DoacaoService {
    private final DoacaoRepository repository;
    private final DoadoraService doadoraService;

    public DoacaoService(DoacaoRepository repository, DoadoraService doadoraService) {
        this.repository = repository;
        this.doadoraService = doadoraService;
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listar(Long doadoraId) {
        if (doadoraId != null) doadoraService.encontrar(doadoraId);
        List<Doacao> doacoes = doadoraId == null ? repository.findAllByOrderByIdAsc()
                : repository.findByDoadoraIdOrderByIdAsc(doadoraId);
        return doacoes.stream().map(DoacaoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoacaoResponse buscar(Long id) { return DoacaoResponse.from(encontrar(id)); }

    public DoacaoResponse criar(DoacaoRequest request) {
        Doacao doacao = new Doacao();
        preencher(doacao, request, true);
        return DoacaoResponse.from(repository.saveAndFlush(doacao));
    }

    public DoacaoResponse atualizar(Long id, DoacaoRequest request) {
        Doacao doacao = encontrar(id);
        boolean novaDoadora = !doacao.getDoadora().getId().equals(request.doadoraId());
        preencher(doacao, request, novaDoadora);
        return DoacaoResponse.from(repository.saveAndFlush(doacao));
    }

    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private Doacao encontrar(Long id) {
        DoadoraService.validarId(id);
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada"));
    }

    private void preencher(Doacao doacao, DoacaoRequest request, boolean exigirAtiva) {
        Doadora doadora = doadoraService.encontrar(request.doadoraId());
        if (exigirAtiva && !doadora.isAtiva()) throw new ConflitoException("Doadora inativa não pode receber novas doações");
        doacao.setDoadora(doadora);
        doacao.setVolumeMl(request.volumeMl());
        doacao.setDataColeta(request.dataColeta());
        doacao.setStatus(request.status());
        doacao.setObservacoes(request.observacoes() == null ? null : request.observacoes().strip());
    }
}
