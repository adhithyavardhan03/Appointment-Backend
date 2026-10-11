package com.anurag.appointment.security;

import com.anurag.appointment.dto.AccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long tokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-minutes}") long tokenMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.tokenMinutes = tokenMinutes;
    }

    public String createToken(AccountResponse account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(tokenMinutes * 60))
                .subject(account.email())
                .claim("accountId", account.id().toString())
                .claim("role", account.role())
                .claim("name", account.name())
                .claim("phone", account.phone())
                .build();
        JwsHeader header = JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getTokenLifetimeSeconds() {
        return tokenMinutes * 60;
    }
}
