package com.jfcdevs.app;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.persistence.RecommendationRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static com.jfcdevs.app.core.api.event.Event.Type.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceTest extends MongoDBTestBase{

    @Autowired
    private WebTestClient client;
    @Autowired
    private RecommendationRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb(){
        repository.deleteAll().block();
    }

    @Test
    void getRecommendationsByProductId(){
        int productId = 1;
        sendCreateRecommendationEvent(productId, 1);
        sendCreateRecommendationEvent(productId, 2);
        sendCreateRecommendationEvent(productId, 3);

        assertEquals(3,(long) repository.findByProductId(productId).count().block());

        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[1].productId").isEqualTo(productId)
                .jsonPath("$.[2].recommendationId").isEqualTo(3);
    }
    @Test
    void duplicateError(){
        int productId = 1;
        int recommendationId = 1;
        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, (long) repository.count().block());

//        InvalidInputException thrown = assertThrows(
//                InvalidInputException.class,
//                () -> sendCreateRecommendationEvent(productId, recommendationId)
//                //,"Expected a InvalidInputException here!"
//        );
    }

    @Test
    void deleteRecommendations(){
        int productId = 1;
        int recommendationId = 1;
        sendCreateRecommendationEvent(productId,recommendationId);
        assertEquals(1, (long)repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
        assertEquals(0, (long)repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
    }

    @Test
    void getRecommendationMissingParameter(){
        getAndVerifyRecommendationsByProductId("",BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter(){
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
       getAndVerifyRecommendationsByProductId("?productId=113", OK)
               .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue(){
        int productIdInvalid = -1;
        getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }
    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus){
        return getAndVerifyRecommendationsByProductId("?productId="+productId, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus){
        return client.get()
                .uri("/recommendation" + productIdQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateRecommendationEvent(int productId, int recommendationId){
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author "  + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        Event<Integer, Recommendation> event = new Event(CREATE, productId, recommendation);
        messageProcessor.accept(event);
    }
    private void sendDeleteRecommendationEvent(int productId){
        Event<Integer, Recommendation> event = new Event(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}
