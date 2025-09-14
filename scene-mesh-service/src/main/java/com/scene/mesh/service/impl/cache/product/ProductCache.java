package com.scene.mesh.service.impl.cache.product;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.product.Product;
import com.scene.mesh.service.spec.cache.IDisposed;

import java.util.List;

public class ProductCache implements IDisposed {

    private final ICache<String, Product> cache;

    public static String KEY_PREFIX = "product:";

    public ProductCache(ICache<String, Product> cache) {
        this.cache = cache;
    }

    public Product getProduct(String productId) {
        return cache.get(KEY_PREFIX + productId);
    }

    @Override
    public void dispose() {
        this.cache.deleteByKeyPrefix(KEY_PREFIX + "*");
    }

    public List<Product> getAllProducts() {
        return this.cache.getAll(KEY_PREFIX + "*");
    }
}
