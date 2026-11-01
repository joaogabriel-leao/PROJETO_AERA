package br.org.aera.financeiro.repo;
import br.org.aera.financeiro.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UsuarioRepository extends JpaRepository<Usuario,UUID> {
    Optional<Usuario> findByUsernameAndAtivoTrue(String username);
}
