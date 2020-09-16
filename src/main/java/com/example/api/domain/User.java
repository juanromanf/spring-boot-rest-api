package com.example.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class User {
    
    private String id;
    
    private String username;
    
    @JsonIgnore
    private String password;
    
    private Set<String> roles;
    
}
