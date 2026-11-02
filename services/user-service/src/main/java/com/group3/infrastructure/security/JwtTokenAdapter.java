package com.group3.infrastructure.security;

import com.group3.application.model.TokenResult;
import com.group3.application.port.out.AccessTokenPort;
import com.group3.domain.aggregate.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenAdapter implements AccessTokenPort {
    private final JwtEncoder encoder;
    private final String issuer;
    private final long expirationSeconds;

    public JwtTokenAdapter(JwtEncoder encoder, @Value("${app.jwt.issuer}") String issuer,
                           @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public TokenResult issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer).issuedAt(now).expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getId().value().toString())
                .claim("email", user.getEmail().value())
                .claim("role", user.getRole().name())
                .id(UUID.randomUUID().toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResult(token, expirationSeconds);
    }
}
