package br.org.aera.financeiro.repo;
import br.org.aera.financeiro.domain.Movimentacao;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface MovimentacaoRepository extends JpaRepository<Movimentacao,UUID> {
 @Query("SELECT m FROM Movimentacao m JOIN FETCH m.conta JOIN FETCH m.categoria JOIN FETCH m.usuario ORDER BY m.dataMovimentacao DESC, m.criadoEm DESC")
 List<Movimentacao> buscarTodas();
}
