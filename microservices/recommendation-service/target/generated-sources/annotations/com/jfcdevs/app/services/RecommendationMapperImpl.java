package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.persistence.RecommendationEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-12T14:13:26-0500",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.15 (Private Build)"
)
@Component
public class RecommendationMapperImpl implements RecommendationMapper {

    @Override
    public Recommendation entityToApi(RecommendationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Recommendation recommendation = new Recommendation();

        recommendation.setRate( entity.getRating() );
        recommendation.setProductId( entity.getProductId() );
        recommendation.setRecommendationId( entity.getRecommendationId() );
        recommendation.setAuthor( entity.getAuthor() );
        recommendation.setContent( entity.getContent() );

        return recommendation;
    }

    @Override
    public RecommendationEntity apiToEntity(Recommendation api) {
        if ( api == null ) {
            return null;
        }

        RecommendationEntity recommendationEntity = new RecommendationEntity();

        recommendationEntity.setRating( api.getRate() );
        recommendationEntity.setProductId( api.getProductId() );
        recommendationEntity.setRecommendationId( api.getRecommendationId() );
        recommendationEntity.setAuthor( api.getAuthor() );
        recommendationEntity.setContent( api.getContent() );

        return recommendationEntity;
    }

    @Override
    public List<Recommendation> entityListToApiList(List<RecommendationEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Recommendation> list = new ArrayList<Recommendation>( entities.size() );
        for ( RecommendationEntity recommendationEntity : entities ) {
            list.add( entityToApi( recommendationEntity ) );
        }

        return list;
    }

    @Override
    public List<RecommendationEntity> apiListToEntityList(List<Recommendation> apis) {
        if ( apis == null ) {
            return null;
        }

        List<RecommendationEntity> list = new ArrayList<RecommendationEntity>( apis.size() );
        for ( Recommendation recommendation : apis ) {
            list.add( apiToEntity( recommendation ) );
        }

        return list;
    }
}
