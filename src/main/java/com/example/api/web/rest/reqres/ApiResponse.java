package com.example.api.web.rest.reqres;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonPropertyOrder({"status", "timestamp", "errors", "payload"})
public class ApiResponse<T> {
    
    @Builder.Default
    private int status = 200;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private List<ApiError> errors;
    
    private T payload;
}