package br.com.fiap.leitesolidario.repository;

import br.com.fiap.leitesolidario.entity.Doacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {
    @EntityGraph(attributePaths = "doadora")
    List<Doacao> findAllByOrderByIdAsc();
    @EntityGraph(attributePaths = "doadora")
    List<Doacao> findByDoadoraIdOrderByIdAsc(Long doadoraId);
    boolean existsByDoadoraId(Long doadoraId);
}
