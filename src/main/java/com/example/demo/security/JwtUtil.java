package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import java.util.List;

@Component

public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET_KEY = "Vyz1ekXuIXbhI7RVtm0BzcahfJFegPls003YD3f1C3Q=";
    private final SecretKey key;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
        log.info("Decoded Secret Key: {}", new String(Base64.getDecoder().decode(SECRET_KEY)));
    }

    public String generateToken(String email, String role, Long seekerId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("authorities", role) // Store role as is
                .claim("seekerId", seekerId) // Add seekerId claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractAuthority(String token) {
        Claims claims = getClaims(token);
        Object authorities = claims.get("authorities");

        if (authorities instanceof String) {
            return (String) authorities; // If it's a single role
        } else if (authorities instanceof List) {
            List<?> roles = (List<?>) authorities;
            if (!roles.isEmpty() && roles.get(0) instanceof String) {
                return (String) roles.get(0); // Get first role
            }
        }
        return "ROLE_USER"; // Default role if none found
    }

    public boolean validateToken(String token) {
        try {
            log.info("Validating token: {}", token);
            Claims claims = getClaims(token);

            if (claims == null) {
                log.error("Failed to extract claims");
                return false;
            }

            String authority = claims.get("authorities", String.class);
            if (authority == null) {
                log.error("Missing authorities claim in token");
                return false;
            }

            if (isTokenExpired(token)) {
                log.error("Token expired");
                return false;
            }
            log.info("Token validation successful");
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getClaims(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
