package com.patrickjed.promotionservice;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Who can call what:
 *  - GET /promotions/**       anyone
 *  - POST / DELETE promotions  only tokens with role ADMIN
 *  - /auth/token               anyone (this is how you log in)
 *
 * Tokens are JWTs signed with a shared secret (HS256). The same secret
 * signs tokens in AuthController and verifies them on every request here.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Bean
    SecretKey jwtKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Checks incoming tokens: valid signature + not expired. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtKey) {
        return NimbusJwtDecoder.withSecretKey(jwtKey).build();
    }

    /** Creates tokens (used by AuthController). */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // No browser sessions/cookies: every request carries its own token,
            // so CSRF protection isn't needed.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/token", "/actuator/health", "/error", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/promotions/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/promotions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/promotions/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            // Read "Authorization: Bearer <token>" and validate it with jwtDecoder
            .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesFromToken())))
            // H2 console runs in a frame
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    /** Turns the token's "roles": ["ADMIN"] claim into Spring's ROLE_ADMIN authority. */
    private JwtAuthenticationConverter rolesFromToken() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
