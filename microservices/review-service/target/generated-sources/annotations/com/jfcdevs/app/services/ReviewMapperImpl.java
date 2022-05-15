package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.persistence.ReviewEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-12T14:14:05-0500",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.15 (Private Build)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review entityToApi(ReviewEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Review review = new Review();

        review.setProductId( entity.getProductId() );
        review.setReviewId( entity.getReviewId() );
        review.setAuthor( entity.getAuthor() );
        review.setSubject( entity.getSubject() );
        review.setContent( entity.getContent() );

        return review;
    }

    @Override
    public ReviewEntity apiToEntity(Review api) {
        if ( api == null ) {
            return null;
        }

        ReviewEntity reviewEntity = new ReviewEntity();

        reviewEntity.setProductId( api.getProductId() );
        reviewEntity.setReviewId( api.getReviewId() );
        reviewEntity.setAuthor( api.getAuthor() );
        reviewEntity.setSubject( api.getSubject() );
        reviewEntity.setContent( api.getContent() );

        return reviewEntity;
    }

    @Override
    public List<Review> entityListToApiList(List<ReviewEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Review> list = new ArrayList<Review>( entities.size() );
        for ( ReviewEntity reviewEntity : entities ) {
            list.add( entityToApi( reviewEntity ) );
        }

        return list;
    }

    @Override
    public List<ReviewEntity> apiListToEntityList(List<Review> apis) {
        if ( apis == null ) {
            return null;
        }

        List<ReviewEntity> list = new ArrayList<ReviewEntity>( apis.size() );
        for ( Review review : apis ) {
            list.add( apiToEntity( review ) );
        }

        return list;
    }
}
