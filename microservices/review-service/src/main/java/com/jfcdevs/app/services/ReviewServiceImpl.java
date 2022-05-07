package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.core.api.core.review.ReviewService;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.util.ServiceUtil;
import com.jfcdevs.app.persistence.ReviewEntity;
import com.jfcdevs.app.persistence.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper){
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public List<Review> getReviews(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: "+ productId);
        }
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));
        log.debug("getReviews: response size: {}", list.size());
        return list;
    }

    @Override
    public Review createReview(Review body) {
        try{
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);
            log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, product id: "+body.getProductId()+". Review id: "+body.getReviewId());
        }
    }

    @Override
    public void deleteReviews(int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
