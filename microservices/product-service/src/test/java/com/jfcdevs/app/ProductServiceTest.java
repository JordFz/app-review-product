package com.jfcdevs.app;

import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.persistence.ProductRepository;
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
public class ProductServiceTest extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setupDb(){
        repository.deleteAll();
    }

    @Test
    void getProductId(){
        int productId = 1;
        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).isPresent());
        getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
    }
//    @Test
//    void duplicateError(){
//        int productId = 1;
//        postAndVerifyProduct(productId, OK);
//        assertTrue(repository.findByProductId(productId).isPresent());
//
//        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
//                .jsonPath("$.path").isEqualTo("/product")
//                .jsonPath("$.message").isEqualTo("Duplicate key, product id: "+productId);
//    }

    @Test
    void deleteProduct(){
        int productId =1;
        postAndVerifyProduct(productId,OK);
        assertTrue(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId,OK);
        assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
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
    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus){
        Product product = new Product(productId, "Name" + productId, productId,"SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
        return client.delete()
                .uri("/product/" +  productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
