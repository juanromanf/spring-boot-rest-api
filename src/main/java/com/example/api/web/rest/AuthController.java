package com.example.api.web.rest;

import com.example.api.domain.AuthenticationResult;
import com.example.api.domain.User;
import com.example.api.service.UsersService;
import com.example.api.web.rest.reqres.ApiError;
import com.example.api.web.rest.reqres.ApiResponse;
import com.example.api.web.rest.reqres.LoginRequest;
import com.example.api.web.rest.reqres.SignupRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private UsersService usersService;
    
    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }
    
    @PostMapping("/signin")
    public Mono<ResponseEntity<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        return usersService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword())
                .map(this::toAuthenticationResponse);
    }
    
    @PostMapping("/signup")
    public Mono<ResponseEntity<?>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password(signUpRequest.getPassword())
                .roles(signUpRequest.getRoles())
                .build();
        
        return usersService.registerUser(user)
                .map(this::registrationResponse);
    }
    
    private ResponseEntity<ApiResponse<String>> registrationResponse(User user) {
        
        ApiResponse<String> body = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .payload("User registered successfully")
                .build();
        
        return ResponseEntity.ok(body);
    }
    
    private ResponseEntity<ApiResponse<AuthenticationResult>> toAuthenticationResponse(AuthenticationResult authenticationResult) {
        
        if (authenticationResult.getSuccess()) {
            
            authenticationResult.setSuccess(null);
            ApiResponse<AuthenticationResult> body = ApiResponse.<AuthenticationResult>builder()
                    .status(HttpStatus.OK.value())
                    .payload(authenticationResult)
                    .build();
            
            return ResponseEntity.ok(body);
            
        } else {
            
            ApiError error = ApiError.builder()
                    .errorMessage("unauthorized request")
                    .build();
            
            ApiResponse<AuthenticationResult> body = ApiResponse.<AuthenticationResult>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .errors(Arrays.asList(error))
                    .build();
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }
}
