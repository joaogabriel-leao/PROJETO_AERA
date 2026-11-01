package br.org.aera.financeiro.repo;
import br.org.aera.financeiro.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CategoriaRepository extends JpaRepository<Categoria,UUID> {
    List<Categoria> findAllByAtivaTrueOrderByNomeAsc();
}
