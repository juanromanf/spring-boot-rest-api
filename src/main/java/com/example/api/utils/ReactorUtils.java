package com.example.api.utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

public class ReactorUtils {
    
    private ReactorUtils() {
    
    }
    
    public static <T> Mono<T> toMono(Callable<T> callable) {
        
        return Mono.fromCallable(callable)
                .subscribeOn(Schedulers.boundedElastic());
    }
    
}
