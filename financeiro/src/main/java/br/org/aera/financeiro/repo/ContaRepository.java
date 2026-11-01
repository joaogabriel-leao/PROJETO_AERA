package br.org.aera.financeiro.repo;
import br.org.aera.financeiro.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ContaRepository extends JpaRepository<Conta,UUID> {
    List<Conta> findAllByAtivaTrueOrderByNomeAsc();
}
