package com.scene.mesh.service.impl.product;

import com.scene.mesh.model.product.Product;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.service.spec.product.IProductService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultProductService implements IProductService {

    private final MutableCacheService mutableCacheService;

    public DefaultProductService(MutableCacheService mutableCacheService) {
        this.mutableCacheService = mutableCacheService;
    }

    @Override
    public Product getProduct(String productId) {
        return mutableCacheService.getProductById(productId);
    }

    @Override
    public List<Product> getProducts() {
        return mutableCacheService.getAllProducts();
    }

    @Override
    public boolean verifyProductSecret(String productId, String secretKey) {
        Product product = this.getProduct(productId);
        if (product == null) {
            log.error("product not found for productId :{}", productId);
            return false;
        }
        if (product.getSettings() == null || product.getSettings().getSecretKey() == null) {
            log.error("product settings not found or secretKey is null");
            return false;
        }

        String[] secretKeys = product.getSettings().getSecretKey();
        boolean isMatched = false;
        for (String sk : secretKeys) {
            if (sk.equals(secretKey)) {
                isMatched = true;
                break;
            }
        }
        return isMatched;
    }

}
