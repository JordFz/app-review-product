package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.recommendation.Recommendation;
import com.jfcdevs.app.persistence.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    @Mappings({
            @Mapping(target = "rate", source = "entity.rating"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation entityToApi(RecommendationEntity entity);
    @Mappings({
            @Mapping(target = "rating", source = "api.rate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(Recommendation api);
    List<Recommendation> entityListToApiList(List<RecommendationEntity> entities);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> apis);
}
