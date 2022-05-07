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
    public Product getProduct(int productId) {
        log.info("/Product return the found product for productId = {}", productId);
        if(productId< 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        log.debug("getProduct: found productId: {}", response.getProductId());
        return response;
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);
            log.debug("CreateProduct: entity create for productId: {}", body.getProductId());
            return mapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, product Id"+ body.getProductId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("deleteProduct: tries to delete an entity with productId {}", productId);
        repository.findByProductId((productId)).ifPresent(e -> repository.delete(e));
    }
}
