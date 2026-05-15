package com.glinscravings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Product entity - represents an inventory item.
 * Maps to the "products" table in MySQL.
 */
@Entity
@Table(name = "products")
public class Product {

    public static final int LOW_STOCK_THRESHOLD = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category categoryEntity;

    @Transient
    private String category;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Column(name = "product_name", nullable = false, length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        AVAILABLE,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED
    }

    // Default Constructor
    public Product() {}

    // Parameterized Constructor
    public Product(String productCode, String name, String category, BigDecimal price, int stock, LocalDate expirationDate) {
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.stockQuantity = stock;
        this.expirationDate = expirationDate;
        this.category = category;
        initializeStatus();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() {
        if (category != null) {
            return category;
        }
        return categoryEntity != null ? categoryEntity.getCategoryName() : null;
    }
    public void setCategory(String category) { this.category = category; }

    public Category getCategoryEntity() { return categoryEntity; }
    public void setCategoryEntity(Category categoryEntity) { this.categoryEntity = categoryEntity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public int getStock() { return stockQuantity; }
    public void setStock(int stock) { 
        this.stockQuantity = stock; 
        recomputeStatus(); // Automatically update status when stock changes
    }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { 
        this.expirationDate = expirationDate; 
        recomputeStatus(); // Automatically update status when date changes
    }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Logic Methods
    private void initializeStatus() {
        if (this.stockQuantity <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void recomputeStatus() {
        if (this.status == ProductStatus.DISCONTINUED) {
            return;
        }
        if (this.stockQuantity <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
            return;
        }
        if (this.stockQuantity <= LOW_STOCK_THRESHOLD) {
            this.status = ProductStatus.LOW_STOCK;
        } else {
            this.status = ProductStatus.AVAILABLE;
        }
    }

    @PostLoad
    @PrePersist
    @PreUpdate
    private void syncStatus() {
        recomputeStatus();
    }
}