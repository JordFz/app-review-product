package com.jfcdevs.app;

import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

import com.jfcdevs.app.core.api.composite.product.ProductAggregate;
import com.jfcdevs.app.core.api.composite.product.RecommendationSummary;
import com.jfcdevs.app.core.api.composite.product.ReviewSummary;
import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.api.exceptions.NotFoundException;
import com.jfcdevs.app.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class ProductCompositeServiceTest {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp(){
        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name",1, "mock-address")));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(singletonList(
                        new Recommendation(PRODUCT_ID_OK,1,"Author",1,"Content","mock address"))));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1,"Author","Subject","Content","Mock-address"))));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));

    }

//    @Test
//    void createCompositeProduct1(){
//        ProductAggregate compositeProduct = new ProductAggregate(1,"name",1,null,null,null);
//        postAndVerifyProduct(compositeProduct, OK);
//    }
//    @Test
//    void createCompositeProduct2(){
//        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
//            singletonList(new RecommendationSummary(1,"a",1,"c")),
//                singletonList(new ReviewSummary(1,"a","s","c")), null
//        );
//        postAndVerifyProduct(compositeProduct, OK);
//    }
//    @Test
//    void deleteCompositeProduct(){
//        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
//                singletonList(new RecommendationSummary(1,"a",1,"c")),
//                singletonList(new ReviewSummary(1,"a","s","c")), null
//        );
//        postAndVerifyProduct(compositeProduct, OK);
//        deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
//        deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
//    }
    @Test
    void getProductById(){
        getAndVerifyProduct(PRODUCT_ID_OK, OK)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound(){
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput(){
        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
//    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus){
//        client.post()
//                .uri("/product-composite")
//                .body(Mono.just(compositeProduct), ProductAggregate.class)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus);
//    }
//    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
//        client.delete()
//                .uri("/product-composite/"+productId)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus);
//    }

}
