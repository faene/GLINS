package com.glinscravings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository - all database operations for Products.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

       // Get all non-discontinued products
       List<Product> findByStatusNot(Product.ProductStatus status);

       // Get products filtered by category name
       List<Product> findByStatusNotAndCategoryEntity_CategoryName(Product.ProductStatus status, String categoryName);

    // Search by name or product code (case-insensitive)
    @Query("SELECT p FROM Product p WHERE p.status <> com.glinscravings.Product.ProductStatus.DISCONTINUED AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchByNameOrCode(@Param("query") String query);

    // Find by name
    Optional<Product> findByName(String name);

       // Find by name ignoring case (used to prevent duplicate items)
       Optional<Product> findByNameIgnoreCase(String name);

       // Check whether a product name already exists ignoring case
       boolean existsByNameIgnoreCase(String name);

    // Find by product code
    Optional<Product> findByProductCode(String productCode);

    // Check if product code exists
    boolean existsByProductCode(String productCode);

        // Products with low stock (used for notifications)
              @Query("SELECT p FROM Product p WHERE p.status <> com.glinscravings.Product.ProductStatus.DISCONTINUED AND " +
                            "p.stockQuantity <= 5")
       List<Product> findLowStockProducts();

    // Products expiring within N days (used for notifications)
    @Query("SELECT p FROM Product p WHERE p.status <> com.glinscravings.Product.ProductStatus.DISCONTINUED AND " +
           "p.expirationDate IS NOT NULL AND " +
           "p.expirationDate BETWEEN :today AND :limitDate")
    List<Product> findNearExpiryProducts(@Param("today") LocalDate today,
                                          @Param("limitDate") LocalDate limitDate);
}