package com.example.api.web.rest;

import com.example.api.web.rest.reqres.ApiError;
import com.example.api.web.rest.reqres.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Arrays;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    
    private ObjectMapper mapper;
    
    public GlobalErrorWebExceptionHandler(ObjectMapper mapper) {
        
        this.mapper = mapper;
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        
        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        
        Throwable e = ExceptionUtils.getRootCause(throwable);
        ApiResponse response;
        
        if (e instanceof ExpiredJwtException) {
            
            ApiError error = translateToApiError((ExpiredJwtException) e);
            response = toApiResponse(HttpStatus.UNAUTHORIZED, error);
            
        } else if (e instanceof AccessDeniedException) {
            
            ApiError error = translateToApiError((AccessDeniedException) e);
            response = toApiResponse(HttpStatus.FORBIDDEN, error);
            
        } else if (e instanceof ResponseStatusException) {
            
            ApiError error = translateToApiError((ResponseStatusException) e);
            response = toApiResponse(((ResponseStatusException) e).getStatus(), error);
            
        } else {
            
            log.error("unexpected error !", e);
            
            ApiError error = translateToApiError(e);
            response = toApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, error);
        }
        
        DataBuffer dataBuffer = wrapResponse(bufferFactory, response);
        
        serverWebExchange.getResponse().setStatusCode(HttpStatus.resolve(response.getStatus()));
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
    
    private DataBuffer wrapResponse(DataBufferFactory bufferFactory, ApiResponse response) {
        
        DataBuffer dataBuffer;
        try {
            dataBuffer = bufferFactory.wrap(mapper.writeValueAsBytes(response));
            
        } catch (JsonProcessingException jpe) {
            log.warn("json processing error!", jpe);
            
            dataBuffer = bufferFactory.wrap("".getBytes(Charset.defaultCharset()));
        }
        
        return dataBuffer;
    }
    
    private ApiResponse toApiResponse(HttpStatus status, ApiError error) {
        
        return ApiResponse.builder()
                .status(status.value())
                .errors(Arrays.asList(error))
                .build();
    }
    
    private ApiError translateToApiError(ExpiredJwtException e) {
        
        return ApiError.builder()
                .errorMessage("Token expired")
                .errorCause(e.getMessage())
                .build();
    }
    
    private ApiError translateToApiError(AccessDeniedException e) {
        
        return ApiError.builder()
                .errorMessage("Access denied")
                .build();
    }
    
    private ApiError translateToApiError(ResponseStatusException e) {
        
        return ApiError.builder()
                .errorMessage(e.getStatus().getReasonPhrase())
                .build();
    }
    
    private ApiError translateToApiError(Throwable e) {
        
        return ApiError.builder()
                .errorMessage("Unexpected error :(")
                .errorCause(e.getMessage())
                .build();
    }
}
