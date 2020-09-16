package com.example.api.service.security;

import com.example.api.domain.User;
import com.example.api.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtManager {
    
    private String secret;
    
    private Long expirationInSecs;
    
    public JwtManager(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-secs}") Long expirationInSecs) {
        this.secret = secret;
        this.expirationInSecs = expirationInSecs;
    }
    
    public Claims getAllClaimsFromToken(String token) {
        
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }
    
    public String getUsernameFromToken(String token) {
        
        return getAllClaimsFromToken(token).getSubject();
    }
    
    public LocalDateTime getExpirationDateFromToken(String token) {
        
        return DateUtils.asLocalDateTime(getAllClaimsFromToken(token).getExpiration());
    }
    
    private Boolean isTokenExpired(String token) {
        
        final LocalDateTime expiration = getExpirationDateFromToken(token);
        return expiration.isBefore(LocalDateTime.now());
    }
    
    public String generateToken(User user) {
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        
        return doGenerateToken(claims, user.getUsername());
    }
    
    private String doGenerateToken(Map<String, Object> claims, String username) {
        
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationInSecs * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();
    }
    
    public Boolean validateToken(String token) {
        
        return !isTokenExpired(token);
    }
    
    public boolean validateJwtToken(String authToken) {
        
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
            
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        
        return false;
    }
}
