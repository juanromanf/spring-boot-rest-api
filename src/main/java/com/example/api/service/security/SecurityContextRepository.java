package com.example.api.service.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {
    
    private static final String BEARER = "Bearer ";
    
    private AuthenticationManager authenticationManager;
    
    public SecurityContextRepository(AuthenticationManager authenticationManager) {
        
        this.authenticationManager = authenticationManager;
    }
    
    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        
        ServerHttpRequest request = serverWebExchange.getRequest();
        
        return Mono.justOrEmpty(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith(BEARER))
                .map(authHeader -> authHeader.substring(BEARER.length()))
                .map(token -> new UsernamePasswordAuthenticationToken(token, token))
                .flatMap(authToken -> authenticationManager.authenticate(authToken))
                .map(authentication -> new SecurityContextImpl(authentication));
    }
}
