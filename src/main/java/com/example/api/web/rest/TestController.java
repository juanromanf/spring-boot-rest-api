package com.example.api.web.rest;

import com.example.api.web.rest.reqres.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/secure")
public class TestController {
    
    @GetMapping("/all")
    public Mono<ResponseEntity<?>> allAccess() {
        
        return Mono.just(toApiResponse("Public Content."));
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Mono<ResponseEntity<?>> userAccess() {
        
        return Mono.just(toApiResponse("User Content."));
    }
    
    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public Mono<ResponseEntity<?>> moderatorAccess() {
        
        return Mono.just(toApiResponse("Moderator Board."));
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<?>> adminAccess() {
        
        return Mono.just(toApiResponse("Admin Board."));
    }
    
    private ResponseEntity<ApiResponse<String>> toApiResponse(String msg) {
        
        ApiResponse<String> body = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .payload(msg)
                .build();
        
        return ResponseEntity.ok(body);
    }
}
