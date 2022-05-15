package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.product.ProductService;
import com.jfcdevs.app.core.api.event.Event;
import com.jfcdevs.app.core.api.exceptions.EvenProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageProcessorConfig {
    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor(){
        return event -> {
            log.info("Process message created at {}... ", event.getEventCreatedAt());
            switch (event.getEventType()){
                case CREATE:
                    Product product = event.getData();
                    log.info("Create product with ID: {}", product.getProductId());
                    productService.createProduct(product).block();
                    break;
                case DELETE:
                    int productId = event.getKey();
                    log.info("Delete recommendations with ProductId: {}", productId);
                    productService.deleteProduct(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EvenProcessingException(errorMessage);
            }
            log.info("Message processing done:");
        };
    }
}
