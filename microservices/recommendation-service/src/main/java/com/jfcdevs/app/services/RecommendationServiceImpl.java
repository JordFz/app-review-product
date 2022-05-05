package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.core.api.core.recommendation.RecommendationService;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
    }
    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if(productId < 1){
            throw new InvalidInputException("Invalid productIs: " + productId);
        }
        if(productId == 113){
            log.debug("No recommendations found for productIs: {}", productId);
            return new ArrayList<>();
        }
        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

        log.debug("/Recommendation reponse size: {}", list.size() );
        return list;
    }
}
