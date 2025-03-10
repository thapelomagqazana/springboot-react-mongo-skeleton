package com.example.backend.security;

import io.jsonwebtoken.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {

    private final Dotenv dotenv = Dotenv.load();

    private final String SECRET_KEY = dotenv.get("JWT_SECRET");
    private final long EXPIRATION_TIME = Long.parseLong(dotenv.get("JWT_EXPIRATION_MS"));

    /**
     * Generate JWT with id, email, and role.
     */
    public String generateToken(String id, String email, String role) {
        return Jwts.builder()
                .setSubject(id)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * Validate the token and return true if valid.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extract claims from token.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Build UserDetails from token.
     */
    public org.springframework.security.core.userdetails.UserDetails validateTokenAndGetUserDetails(String token) {
        if (!validateToken(token)) {
            return null;
        }

        String email = extractEmail(token);
        String role = extractRole(token);

        return new User(
                email, 
                "", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /**
     * Build Authentication from UserDetails.
     */
    public UsernamePasswordAuthenticationToken getAuthentication(
            org.springframework.security.core.userdetails.UserDetails userDetails,
            HttpServletRequest request
    ) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
