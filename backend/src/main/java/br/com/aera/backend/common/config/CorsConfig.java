package br.com.aera.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao de CORS (Cross-Origin Resource Sharing).
 *
 * <p>O frontend (Vite) e o backend rodam em origens diferentes (portas
 * diferentes), e o navegador bloqueia essas chamadas por padrao. Aqui
 * liberamos explicitamente as origens permitidas e os metodos usados.</p>
 *
 * <p>As origens permitidas sao lidas da propriedade
 * {@code app.cors.allowed-origins}, que varia por ambiente:
 * em dev aponta para o localhost; em producao, para o dominio real
 * (definido por variavel de ambiente). Assim, nao ficam fixas no codigo.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        // A propriedade vem como lista separada por virgula; quebramos em array.
        this.allowedOrigins = allowedOrigins.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
        // allowCredentials foi removido: o frontend nao usa cookies/credenciais,
        // entao manter true so ampliaria a superficie de risco sem necessidade.
    }
}
