package com.patrickjed.promotionservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * DEV ONLY: hands out tokens for two hardcoded users.
 * In a real company this job belongs to an identity provider
 * (Okta, Azure AD/Entra, Ping...). This service would only *verify* tokens.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtEncoder encoder;

    private static final Map<String, DevUser> USERS = Map.of(
            "admin", new DevUser("admin123", List.of("ADMIN")),
            "viewer", new DevUser("viewer123", List.of("USER")));

    @PostMapping("/token")
    public Map<String, String> token(@RequestBody LoginRequest req) {
        DevUser user = USERS.get(req.username());
        if (user == null || !user.password().equals(req.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad username or password");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("promotion-service")
                .subject(req.username())
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .claim("roles", user.roles())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return Map.of("token", token);
    }

    record LoginRequest(String username, String password) {}

    record DevUser(String password, List<String> roles) {}
}
