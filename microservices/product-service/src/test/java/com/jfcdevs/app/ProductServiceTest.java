package com.jfcdevs.app;

import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.event.Event.Type;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.persistence.ProductRepository;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class ProductServiceTest extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Product>> messageProcessor;

    @BeforeEach
    void setupDb(){
        repository.deleteAll().block();
    }

    @Test
    void getProductId(){
        int productId = 1;
        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long) repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long)repository.count().block());

        getAndVerifyProduct(productId, OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }
    @Test
    void duplicateError(){
        int productId = 1;
        assertNull(repository.findByProductId(productId).block());
        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
//        InvalidInputException thrown = assertThrows(
//                InvalidInputException.class,
//                () -> sendCreateProductEvent(productId),
//                "Expected a InvalidInputException here!");
    }

    @Test
    void deleteProduct(){
        int productId =1;
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);

    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound(){
        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: "+productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue(){
        int productIdInvalid = -1;
        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/"+productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: "+productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus){
        return client.get()
                .uri("/product" + productIdPath)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
    private void sendCreateProductEvent (int productId){
        Product product = new Product(productId, "Name " + productId, productId,  "SA");
        Event<Integer, Product> event = new Event(CREATE, productId, product);
        messageProcessor.accept(event);
    }
    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}
