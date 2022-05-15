package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.core.review.ReviewService;
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
    private final ReviewService reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor(){
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()){
                case CREATE:
                    Review review = event.getData();
                    log.info("Delete reviews with ProductId: {}/{}", review.getProductId(), review.getReviewId());
                    reviewService.createReview(review).block();
                    break;
                case DELETE:
                    int productId = event.getKey();
                    log.info("Delete reviews with product Id {}: ", productId);
                    reviewService.deleteReviews(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type: "+ event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EvenProcessingException(errorMessage);
            }
            log.info("Message process done!");
        };
    }
}
