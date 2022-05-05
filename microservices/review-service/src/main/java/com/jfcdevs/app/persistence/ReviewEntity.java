package com.jfcdevs.app.persistence;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "reviews", indexes = { @Index(name = "reviews_unique_idx", unique = true, columnList = "productId, reviewId")})
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class ReviewEntity {

    @Id
    @GeneratedValue
    private int id;
    @Version
    private int version;

    @NonNull
    private int productId;
    @NonNull
    private int reviewId;
    @NonNull
    private String author;
    @NonNull
    private String subject;
    @NonNull
    private String content;
}
