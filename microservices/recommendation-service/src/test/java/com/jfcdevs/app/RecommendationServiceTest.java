package com.jfcdevs.app;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.persistence.RecommendationRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceTest extends MongoDBTestBase{

    @Autowired
    private WebTestClient client;
    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setupDb(){
        repository.deleteAll();
    }

    @Test
    void getRecommendationsByProductId(){
        int productId = 1;
        postAndVerifyRecommendation(productId, 1, OK);
        postAndVerifyRecommendation(productId, 2, OK);
        postAndVerifyRecommendation(productId, 3, OK);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$.[2].recommendationId").isEqualTo(3);
    }
//    @Test
//    void duplicateError(){
//        int productId = 1;
//        int recommendationId = 1;
//        postAndVerifyRecommendation(productId,recommendationId,OK)
//                .jsonPath("$.productId").isEqualTo(productId)
//                .jsonPath("$.recommendationId").isEqualTo(recommendationId);
//        assertEquals(1, repository.count());
//        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
//                .jsonPath("$.path").isEqualTo("/recommendation")
//                .jsonPath("$.message").isEqualTo("Duplicate key, ProductId: 1, recommendationId: 1");
//        assertEquals(1, repository.count());
//    }

    @Test
    void deleteRecommendations(){
        int productId = 1;
        int recommendationId = 1;
        postAndVerifyRecommendation(productId, recommendationId,OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
    }

    @Test
    void getRecommendationMissingParameter(){
        getAndVerifyRecommendationsByProductId("",BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation");
                //.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter(){
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation");
                //.jsonPath("$.message").isEqualTo("Type mismatch.")
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
    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus){
        Recommendation recommendation = new Recommendation(productId,recommendationId, "Author"+recommendationId,
                recommendationId,"Content "+ recommendationId, "SA");
        return client.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus){
        return client.delete()
                .uri("/recommendation?productId=" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
