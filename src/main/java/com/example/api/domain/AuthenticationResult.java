package com.example.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResult {
    
    private Boolean success;
    
    private String errorMessage;
    
    private String token;
    
    private LocalDateTime expiresOn;
    
    private User user;
}
