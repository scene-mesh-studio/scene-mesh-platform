package com.scene.mesh.service.spec.product;

import com.scene.mesh.model.event.IMetaEvent;
import com.scene.mesh.model.product.OriginalProduct;
import com.scene.mesh.model.product.Product;
import com.scene.mesh.model.scene.Scene;

import java.util.List;

/**
 * 产品服务
 */
public interface IProductService {

    Product getProduct(String productId);

    List<Product> getProducts();

    boolean verifyProductSecret(String productId, String secretKey);

}
