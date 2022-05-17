package com.jfcdevs.app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.product.ProductService;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.recommendation.RecommendationService;
import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.core.review.ReviewService;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.api.exceptions.NotFoundException;
import com.jfcdevs.app.core.util.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;


import java.io.IOException;

import static com.jfcdevs.app.core.api.event.Event.Type.*;
import static java.util.logging.Level.*;
import static reactor.core.publisher.Flux.*;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final Scheduler publishEvenScheduler;
    private final StreamBridge streamBridge;
    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";


    @Autowired
    public ProductCompositeIntegration(
            WebClient.Builder webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            @Qualifier("publishEventScheduler") Scheduler publishEvenScheduler) {

        this.publishEvenScheduler = publishEvenScheduler;
        this.webClient = webClient.build();
        this.streamBridge = streamBridge;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = PRODUCT_SERVICE_URL + "/product/" + productId;
        log.debug("Will call the getProduct API on URL: {}", url);
        return webClient.get()
                .uri(url).retrieve().bodyToMono(Product.class)
                .log(log.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEvenScheduler);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEvenScheduler).then();
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;
        log.debug("Will call the getRecommendation API om URL: {}", url);

        return webClient.get()
                .uri(url).retrieve()
                .bodyToFlux(Recommendation.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(() -> {
           sendMessage("recommendations-out-0", new Event(CREATE, body.getProductId(), body));
           return body;
        }).subscribeOn(publishEvenScheduler);
    }

    @Override
    public Mono<Void> deleteRecommendation(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEvenScheduler).then();
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;
        log.debug("Will call the getReviews API on URL: {}", url);
        return webClient.get()
                .uri(url).retrieve()
                .bodyToFlux(Review.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEvenScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEvenScheduler).then();
    }
    public Mono<Health> getProductHealth() {
        return getHealth(PRODUCT_SERVICE_URL);
    }
    public Mono<Health> getRecommendationHealth(){
        return getHealth(RECOMMENDATION_SERVICE_URL);
    }
    public Mono<Health> getReviewHealth(){
        return getHealth(REVIEW_SERVICE_URL);
    }
    private Mono<Health> getHealth(String url){
        url += "/actuator/health";
        log.debug("Will call the health API on URL: {}", url);
        return webClient.get()
                .uri(url).retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(log.getName(), FINE);
    }
    private Throwable handleException(Throwable ex){
        if(!(ex instanceof WebClientResponseException)){
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
        }
        WebClientResponseException wcre = (WebClientResponseException) ex;
        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }
    private String getErrorMessage(WebClientResponseException ex){
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex){
            return ex.getMessage();
        }
    }

    private void sendMessage(String bindingName, Event event){
        log.debug("Send a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}
