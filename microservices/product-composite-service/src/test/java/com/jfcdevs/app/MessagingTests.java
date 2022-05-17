package com.jfcdevs.app;

import com.jfcdevs.app.core.api.composite.product.ProductAggregate;
import com.jfcdevs.app.core.api.composite.product.RecommendationSummary;
import com.jfcdevs.app.core.api.composite.product.ReviewSummary;
import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.event.Event.Type;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jfcdevs.app.IsSameEvent.*;
import static com.jfcdevs.app.core.api.event.Event.Type.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition=overriding=true",
        "eureka.client.enabled=false"}
)
@Import({TestChannelBinderConfiguration.class})
@Slf4j
public class MessagingTests {
    @Autowired
    private WebTestClient client;
    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp(){
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProduct(){
        ProductAggregate composite = new ProductAggregate(1, "Name",1,null,null,null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewsMessages = getMessages("reviews");

        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedEvent = new Event(
                CREATE, composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null)
        );
        log.info("Is {}", productMessages.get(0));
        //assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));
        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewsMessages.size());
    }

    @Test
    void createCompositeProduct2(){
        ProductAggregate composite = new ProductAggregate(1, "name", 1,
                Collections.singletonList(new RecommendationSummary(1,"a",1,"c")),
                Collections.singletonList(new ReviewSummary(1,"a","s","c")), null
        );
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewsMessages = getMessages("reviews");
        assertEquals(1, productMessages.size());

        Event<Integer, Product> expectedProductEvent = new Event(
                CREATE, composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null)
        );
        //assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));
        assertEquals(1, recommendationMessages.size());
        RecommendationSummary rec = composite.getRecommendations().get(0);
        Event<Integer, Product> expectedRecommendationEvent = new Event(CREATE, composite.getProductId(),
                new Recommendation(composite.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(), rec.getContent(), null));
        //assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, reviewsMessages.size());
        ReviewSummary rev = composite.getReviews().get(0);
        Event<Integer, Product> expectedReviewEvent = new Event(CREATE, composite.getProductId(),
                new Review(composite.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null));
        //assertThat(reviewsMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProduct(){
        deleteAndVerifyProduct(1, ACCEPTED);

        final List<String> productMessage = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewsMessages = getMessages("reviews");
        assertEquals(1, productMessage.size());

        Event<Integer, Product> expectedProductEvent = new Event(DELETE, 1, null);
        //assertThat(productMessage.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        assertEquals(1, recommendationMessages.size());
        Event<Integer, Product> expectedRecommendationEvent = new Event(DELETE, 1 ,null);
        //assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        assertEquals(1, reviewsMessages.size());
        Event<Integer, Product> expectedReviewEvent = new Event(DELETE, 1 ,null);
        //assertThat(reviewsMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void purgeMessages(String bindingName){
        getMessages(bindingName);
    }
    private List<String> getMessages(String bindingName){
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;
        while(anyMoreMessages){
            Message<byte[]> message = getMessage(bindingName);
            if(message == null){
                anyMoreMessages  = false;
            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }
    private Message<byte[]> getMessage(String bindingName){
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe){
            log.error("getMessage() received a NPE with binding =  {}", bindingName);
            return null;
        }
    }
    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus){
        client.post()
                .uri("/product-composite")
                .body(Mono.just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}
