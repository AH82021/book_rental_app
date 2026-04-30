package com.bookstore.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret ;

    @Value("${jwt.expiration:86400000}")
    private Long expiration ;



    public String generateToken(String email,Long userId,String role) {

        Map<String, Object> claims = new HashMap<>();
         claims.put("userId", userId);
         claims.put("role", role);
         return  createToken(claims,email);
    }


    private Key getSigningKey() {

        return  Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }



    private String createToken(Map<String, Object> claims, String email) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractEmail(String token) {
      return getClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return getClaimsFromToken(token).get("userId", String.class);
    }


    public boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }


    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }




}
