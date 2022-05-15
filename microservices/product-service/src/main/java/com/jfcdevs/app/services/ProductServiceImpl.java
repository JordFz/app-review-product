package com.jfcdevs.app.services;

import com.jfcdevs.app.core.api.core.product.Product;
import com.jfcdevs.app.core.api.core.product.ProductService;
import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.api.exceptions.NotFoundException;
import com.jfcdevs.app.core.util.ServiceUtil;
import com.jfcdevs.app.persistence.ProductEntity;
import com.jfcdevs.app.persistence.ProductRepository;
import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

import static java.util.logging.Level.*;

@RestController
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public Mono<Product> getProduct(int productId) {
        log.info("/Product return the found product for productId = {}", productId);
        if(productId< 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get product info for id={}", productId);
        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if(body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId:" +  body.getProductId());
        }
        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId())
                )
                .map(e -> mapper.entityToApi(e));
        return newEntity;
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if(productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteProduct: tries to delete an entity with productId {}", productId);
        return repository.findByProductId(productId).log(log.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
    }
    private Product setServiceAddress(Product e){
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
