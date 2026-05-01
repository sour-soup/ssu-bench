package org.soup.ssu.bench.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssu.bench.model.RoleEnum;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String USERNAME_CLAIM = "username";
    private static final String ROLE_CLAIM = "roles";

    private final JwtProperties jwtProperties;

    public String createToken(String username, BigInteger userId, RoleEnum role) {
        Map<String, Object> claims = Map.of(
            USERNAME_CLAIM, username,
            ROLE_CLAIM, role
        );

        long expirationMillis = Instant.now().plus(jwtProperties.getExpiration())
            .toEpochMilli();

        return Jwts.builder()
            .claims(claims)
            .subject(String.valueOf(userId))
            .issuer(jwtProperties.getIssuer())
            .expiration(new Date(expirationMillis))
            .signWith(getSecretKey(), Jwts.SIG.HS256)
            .compact();
    }

    public Optional<AuthenticatedUser> parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                .decryptWith(getSecretKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            return Optional.empty();
        }

        if (claims.getExpiration().before(new Date())) {
            return Optional.empty();
        }

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .userId(new BigInteger(claims.getSubject()))
            .username(claims.get(USERNAME_CLAIM, String.class))
            .role(claims.get(ROLE_CLAIM, String.class))
            .build();

        return Optional.of(authenticatedUser);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
