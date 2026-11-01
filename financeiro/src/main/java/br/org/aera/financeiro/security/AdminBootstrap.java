package br.org.aera.financeiro.security;

import br.org.aera.financeiro.domain.Usuario;
import br.org.aera.financeiro.repo.UsuarioRepository;
import org.springframework.boot.*;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
 private final UsuarioRepository repository;
 private final PasswordEncoder encoder;
 private final Environment env;
 public AdminBootstrap(UsuarioRepository repository, PasswordEncoder encoder, Environment env){this.repository=repository;this.encoder=encoder;this.env=env;}
 @Override public void run(ApplicationArguments args){
  if(repository.count()!=0) return;
  String username=env.getProperty("ADMIN_INITIAL_USERNAME","admin");
  String password=env.getProperty("ADMIN_INITIAL_PASSWORD");
  if(password==null || password.length()<12) throw new IllegalStateException("Defina ADMIN_INITIAL_PASSWORD com pelo menos 12 caracteres antes do primeiro uso.");
  repository.save(new Usuario(username,encoder.encode(password),"ADMIN"));
 }
}
