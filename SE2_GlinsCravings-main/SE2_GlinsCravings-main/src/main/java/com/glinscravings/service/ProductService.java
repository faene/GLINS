package com.glinscravings.service;

import com.glinscravings.Category;
import com.glinscravings.CategoryRepository;
import com.glinscravings.Product;
import com.glinscravings.ProductRepository;
import com.glinscravings.dto.ApiResponse;
import com.glinscravings.dto.ProductRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findByStatusNot(Product.ProductStatus.DISCONTINUED);
    }

    public Optional<Product> getProduct(Long id) {
        return productRepository.findById(id);
    }

    public ApiResponse createProduct(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return new ApiResponse(false, "Product name is required");
        }
        if (request.getPrice() == null || request.getPrice().doubleValue() <= 0) {
            return new ApiResponse(false, "Valid price is required");
        }

        String code = request.getProductCode();
        if (code == null || code.isBlank()) {
            long count = productRepository.count();
            code = "P" + (count + 1);
        }
        if (productRepository.existsByProductCode(code)) {
            return new ApiResponse(false, "Product code already exists");
        }
        if (request.getName() != null && productRepository.existsByNameIgnoreCase(request.getName().trim())) {
            return new ApiResponse(false, "Product name already exists");
        }

        Product product = new Product();
        product.setProductCode(code);
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setCategoryEntity(resolveCategory(request.getCategory()));
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : Product.LOW_STOCK_THRESHOLD);
        product.setExpirationDate(request.getExpirationDate());
        product.recomputeStatus();

        productRepository.save(product);
        return new ApiResponse(true, "Product created", product);
    }

    public ApiResponse updateProduct(Long id, ProductRequest request) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            return new ApiResponse(false, "Product not found");
        }

        Product product = opt.get();
        if (request.getName() != null && !request.getName().isBlank()) {
            Optional<Product> duplicate = productRepository.findByNameIgnoreCase(request.getName().trim());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(product.getId())) {
                return new ApiResponse(false, "Product name already exists");
            }
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
            product.setCategoryEntity(resolveCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getReorderLevel() != null) {
            product.setReorderLevel(request.getReorderLevel());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getExpirationDate() != null) {
            product.setExpirationDate(request.getExpirationDate());
        }
        product.recomputeStatus();

        productRepository.save(product);
        return new ApiResponse(true, "Product updated", product);
    }

    public ApiResponse deleteProduct(Long id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            return new ApiResponse(false, "Product not found");
        }
        Product product = opt.get();
        product.setStatus(Product.ProductStatus.DISCONTINUED);
        productRepository.save(product);
        return new ApiResponse(true, "Product deleted");
    }

    private Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        return categoryRepository.findByCategoryName(categoryName.trim()).orElse(null);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.searchByNameOrCode(query);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getNearExpiryProducts() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(7);
        return productRepository.findNearExpiryProducts(today, limit);
    }
}
