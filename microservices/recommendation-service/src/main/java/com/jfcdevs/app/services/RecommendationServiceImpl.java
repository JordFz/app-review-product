package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.recommendation.RecommendationService;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.util.ServiceUtil;
import com.jfcdevs.app.persistence.RecommendationEntity;
import com.jfcdevs.app.persistence.RecommendationRepository;
import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static java.util.logging.Level.*;

@RestController
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper){
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.debug("getRecommendation: response for productId {}", productId);
        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if(body.getProductId() < 1){
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: "+  body.getProductId()+ ", Recommendation Id: "+body.getRecommendationId())
                )
                .map(e -> mapper.entityToApi(e));
        return newEntity;
    }

    @Override
    public Mono<Void> deleteRecommendation(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteRecommendation: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e){
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
