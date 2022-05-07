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

import java.util.ArrayList;
import java.util.List;

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
    public List<Recommendation> getRecommendations(int productId) {

        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
        log.debug("getRecommendation: response size: {}", list.size());
        return list;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try{
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, productId: "+body.getProductId()+" recommendationId: "+body.getRecommendationId());
        }
    }

    @Override
    public void deleteRecommendation(int productId) {
        log.debug("deleteRecommendation: tries to delete recommendatioons for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
