package com.jfcdevs.app.core.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class RecommendationSummary {
    private final int recommendationId;
    private final String author;
    private final int rate;
    //private final String content;
}
