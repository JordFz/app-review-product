package com.jfcdevs.app.core.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ReviewSummary {
    private final int reviewId;
    private final String author;
    private final String subject;
    //private final String content;
}
