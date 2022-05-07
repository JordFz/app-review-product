package com.jfcdevs.app.core.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {

    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json"
    )
    Product getProduct(@PathVariable int productId);

    @PostMapping(
            value = "/product",
            produces = "application/json",
            consumes = "application/json"
    )
    Product createProduct(@RequestBody Product body);

    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);

}
