package com.jfcdevs.app.core.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Product {
    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
}
