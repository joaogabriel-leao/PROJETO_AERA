package br.org.aera.financeiro.security;

import br.org.aera.financeiro.repo.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService usuarios(UsuarioRepository repository){
  return username -> repository.findByUsernameAndAtivoTrue(username)
      .map(u -> User.builder().username(u.getUsername()).password(u.getSenhaHash())
          .authorities(new SimpleGrantedAuthority("ROLE_"+u.getPerfil())).build())
      .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
 }
 @Bean SecurityFilterChain filtro(HttpSecurity http) throws Exception {
  http.authorizeHttpRequests(a -> a
       .requestMatchers("/login", "/css/**", "/favicon.ico").permitAll()
       .requestMatchers("/config/**").hasRole("ADMIN")
       .requestMatchers("/movimentacoes/nova", "/movimentacoes/salvar").hasAnyRole("ADMIN","TESOUREIRO","OPERADOR")
       .anyRequest().authenticated())
      .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/",true).permitAll())
      .logout(l -> l.logoutSuccessUrl("/login?logout"));
  return http.build();
 }
}
