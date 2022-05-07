package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.review.Review;
import com.jfcdevs.app.persistence.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Review entityToApi(ReviewEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ReviewEntity apiToEntity(Review api);
    List<Review> entityListToApiList(List<ReviewEntity> entities);
    List<ReviewEntity> apiListToEntityList(List<Review> apis);
}
