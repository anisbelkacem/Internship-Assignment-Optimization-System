package com.aspd.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final String secret = "Aspd_Team3_Anis_Youssef_Hadir_Fekher_Hedi";
    private final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    private final long expirationHours = 24;

    public String generateToken(String username, Map<String, Object> claims) {

        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}