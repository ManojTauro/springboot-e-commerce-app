package com.manoj.ecommerce.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manoj.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
    //    private final ProductMapper mapper;
    private final ModelMapper mapper = new ModelMapper();

    public Integer saveProduct(ProductRequest request) {
        var product = this.mapper.map(request, Product.class);
        product.getCategory().setId(request.categoryId());

        return repository.save(product).getId();
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        var productIdList = request.stream()
                .map(ProductPurchaseRequest::productId)
                .toList();

        var productsFromDB = repository.findAllByIdInOrderById(productIdList);

        if (productsFromDB.size() != productIdList.size())
            throw new ProductPurchaseException("One or more products does not exists");

        var requestedProducts = request.stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::productId))
                .toList();

        return IntStream.range(0, productsFromDB.size())
                .mapToObj(i -> {
                    Product productFromDB = productsFromDB.get(i);
                    ProductPurchaseRequest requestedProduct = requestedProducts.get(i);

                    if (productFromDB.getAvailableQuantity() < requestedProduct.quantity())
                        throw new ProductPurchaseException("Insufficient stock for product with ID: " + productFromDB.getId());

                    var newAvailableQuantity = productFromDB.getAvailableQuantity() - requestedProduct.quantity();
                    productFromDB.setAvailableQuantity(newAvailableQuantity);

                    repository.save(productFromDB);

                    return new ProductPurchaseResponse(
                            productFromDB.getId(),
                            productFromDB.getName(),
                            productFromDB.getDescription(),
                            productFromDB.getPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    public ProductResponse getProduct(Integer id) {
        return repository.findById(id)
                .map(product ->
                        mapper.map(product, ProductResponse.class)
                )
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
    }

    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream()
                .map(product -> mapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }
}
