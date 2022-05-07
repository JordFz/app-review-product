package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.composite.product.*;
import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.exceptions.NotFoundException;
import com.jfcdevs.app.core.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if(product == null){
            throw new NotFoundException("Not product found for productId: " + productId);
        }
        log.debug("start to generate list");
        List<Recommendation> recommendations = integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);
        log.debug("End to create list of recommendation and reviews");
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate body) {
        try {
            log.debug("createCompositeProduct: create a new composite entity for productId: {}", body.getProductId());
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if(body.getRecommendations() != null){
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }
            if(body.getReviews() != null){
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });

            }
            log.debug("createCompositeProduct: composites entities created for a productId {}", body.getProductId());
        } catch (RuntimeException ex){
            log.warn("createCompositeProduct failed", ex);
            throw  ex;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("deleteCompositeProduct: Delete a product aggregate for productId: {}", productId);
        integration.deleteProduct(productId);
        integration.deleteRecommendation(productId);
        integration.deleteReviews(productId);
        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);

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
