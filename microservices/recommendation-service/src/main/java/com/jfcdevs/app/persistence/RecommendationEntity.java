package com.jfcdevs.app.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendation")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId':1, 'recommendationId':1}")
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecommendationEntity {

    @Id
    private String id;
    @Version
    private Integer version;

    @NonNull
    private int productId;
    @NonNull
    private int recommendationId;
    @NonNull
    private String author;
    @NonNull
    private int rating;
    @NonNull
    private String content;
}
