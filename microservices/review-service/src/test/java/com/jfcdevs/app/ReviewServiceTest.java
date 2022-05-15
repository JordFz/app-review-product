package com.jfcdevs.app;

import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.persistence.ReviewRepository;
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

import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewServiceTest extends MySqlTestBase {
    @Autowired
    private WebTestClient client;
    @Autowired
    private ReviewRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Review>> messageProcessor;

    @BeforeEach
    void setupDb(){
        repository.deleteAll();
    }

    @Test
    void getReviewsByProductId(){
        int productId = 1;

        assertEquals(0, repository.findByProductId(productId).size());

        sendCreateReviewEvent(productId, 1);
        sendCreateReviewEvent(productId, 2);
        sendCreateReviewEvent(productId, 3);
        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyReviewsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    @Test
    void duplicateError(){
        int productId = 1;
        int reviewId = 1;

        assertEquals(0, repository.count());
        sendCreateReviewEvent(productId, reviewId);

        assertEquals(1, repository.count());

    }
    @Test
    void deleteReviews(){
        int productId =1;
        int reviewId = 1;

        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.findByProductId(productId).size());

        sendDeleteReviewEvent(productId);
        assertEquals(0, repository.findByProductId(productId).size());
        sendDeleteReviewEvent(productId);
    }

    @Test
    void getReviewsMissingParameter(){
        getAndVerifyReviewsByProductId("",BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getReviewsInvalidParameter(){
        getAndVerifyReviewsByProductId("?productId=no-integer",BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound(){
        getAndVerifyReviewsByProductId("?productId=213",OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue(){
        int productIdInvalid = -1;
        getAndVerifyReviewsByProductId("?productId="+productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus){
        return getAndVerifyReviewsByProductId("?productId="+productId, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus){
        return client.get()
                .uri("/review"+productIdQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateReviewEvent(int productId, int reviewId) {
        Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
        Event<Integer, Review> event = new Event(Event.Type.CREATE, productId, review);
        messageProcessor.accept(event);
    }
    private void sendDeleteReviewEvent(int productId) {
        Event<Integer, Review> event = new Event(Event.Type.DELETE, productId, null);
        messageProcessor.accept(event);
    }

}
