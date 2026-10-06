package br.com.fiap.leitesolidario.repository;

import br.com.fiap.leitesolidario.entity.Doadora;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoadoraRepository extends JpaRepository<Doadora, Long> {
    List<Doadora> findAllByOrderByIdAsc();
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
