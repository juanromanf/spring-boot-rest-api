package com.example.api.service.security;

import com.example.api.domain.RoleName;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    
    private JwtManager jwtManager;
    
    public AuthenticationManager(JwtManager jwtManager) {
        
        this.jwtManager = jwtManager;
    }
    
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        
        String authToken = authentication.getCredentials().toString();
        
        return Mono.justOrEmpty(jwtManager.getUsernameFromToken(authToken))
                .map(username -> validateToken(authToken, username));
    }
    
    private Authentication validateToken(String authToken, String username) {
        
        if (jwtManager.validateToken(authToken)) {
            
            Claims claims = jwtManager.getAllClaimsFromToken(authToken);
            List<String> rolesMap = claims.get("role", List.class);
            
            return buildAuthenticationToken(username, rolesMap);
            
        } else {
            
            throw new IllegalArgumentException("invalid token detected");
        }
    }
    
    private UsernamePasswordAuthenticationToken buildAuthenticationToken(String username, List<String> rolesMap) {
        
        List<SimpleGrantedAuthority> authorities = rolesMap.stream()
                .map(s -> RoleName.valueOf(s))
                .map(roleName -> new SimpleGrantedAuthority(roleName.name()))
                .collect(Collectors.toList());
        
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
