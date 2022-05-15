package com.jfcdevs.app.core.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {

    @GetMapping(
            value = "/recommendation",
            produces = "application/json"
    )
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);


    Mono<Recommendation> createRecommendation(Recommendation body);


    Mono<Void> deleteRecommendation( int productId);
}
