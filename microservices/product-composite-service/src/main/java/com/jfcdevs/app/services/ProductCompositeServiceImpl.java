package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.composite.product.*;
import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

    @Autowired
    public  ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration){
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
    @Override
    public Mono<ProductAggregate> getProduct(int productId) {
        log.info("Will get composite product info for product id = {} ", productId);

        return Mono.zip(
                values -> createProductAggregate((Product) values[0],
                (List<Recommendation>) values[1],
                (List<Review>) values[2],
                serviceUtil.getServiceAddress()
        ),
                        integration.getProduct(productId),
                        integration.getRecommendations(productId).collectList(),
                        integration.getReviews(productId).collectList())
                .doOnError(ex -> log.warn("Get CompositeProduct failed: {}", ex.toString()))
                .log(log.getName(),  Level.FINE);
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate body) {
        try {
            List<Mono> monoList = new ArrayList<>();
            log.debug("createCompositeProduct: create a new composite entity for productId: {}", body.getProductId());
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monoList.add(integration.createProduct(product));

            if(body.getRecommendations() != null){
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }
            if(body.getReviews() != null){
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    monoList.add(integration.createReview(review));
                });

            }
            log.debug("createCompositeProduct: composites entities created for a productId {}", body.getProductId());
            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();
        } catch (RuntimeException ex){
            log.warn("createCompositeProduct failed", ex);
            throw  ex;
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        try {
            log.debug("deleteCompositeProduct: Delete a product aggregate for productId: {}", productId);
            return Mono.zip(
                    r -> "",
                    integration.deleteProduct(productId),
                    integration.deleteRecommendation(productId),
                    integration.deleteReviews(productId)
            ).doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                    .log(log.getName(), Level.FINE).then();
        } catch (RuntimeException ex){
            log.warn("createCompositeProduct failed", ex);
            throw  ex;
        }

    }


    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
                                                    List<Review> reviews, String serviceAddress){
        //Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        //Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null: recommendations.stream()
                        .map( r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());
        //Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null)? null: reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // Create info regarding the involved microservices address
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress(): "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";

        ServiceAddress serviceAddresses = new ServiceAddress(serviceAddress, productAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
