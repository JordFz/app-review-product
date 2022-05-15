package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.recommendation.RecommendationService;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.exceptions.EvenProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageProcessorConfig {
    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()){
                case CREATE:
                    Recommendation recommendation = event.getData();
                    log.info("Create recommendation with Id: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                    recommendationService.createRecommendation(recommendation).block();
                    break;
                case DELETE:
                    int productId = event.getKey();
                    log.info("Delete recommendation with ProductId: {}", productId);
                    recommendationService.deleteRecommendation(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EvenProcessingException(errorMessage);
            }
        };
    }
}
