package com.glinscravings;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DataSeeder(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Korean Noodles"));
            categoryRepository.save(new Category("Filipino Food"));
            categoryRepository.save(new Category("Beverages"));
            categoryRepository.save(new Category("Add Ons"));
        }

        if (productRepository.count() > 0) {
            productRepository.findAll().forEach(product -> {
                product.recomputeStatus();
                productRepository.save(product);
            });
            return;
        }

        Product p1 = new Product();
        p1.setProductCode("K001");
        p1.setName("Potato Corndog");
        p1.setCategoryEntity(categoryRepository.findByCategoryName("Korean Noodles").orElse(null));
        p1.setReorderLevel(10);
        p1.setPrice(new BigDecimal("75.00"));
        p1.setStock(30);
        p1.setExpirationDate(LocalDate.now().plusMonths(6));
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setProductCode("K002");
        p2.setName("Tteokbokki");
        p2.setCategoryEntity(categoryRepository.findByCategoryName("Korean Noodles").orElse(null));
        p2.setReorderLevel(10);
        p2.setPrice(new BigDecimal("120.00"));
        p2.setStock(15);
        p2.setExpirationDate(LocalDate.now().plusMonths(3));
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setProductCode("P001");
        p3.setName("Sisig Fries");
        p3.setCategoryEntity(categoryRepository.findByCategoryName("Filipino Food").orElse(null));
        p3.setReorderLevel(5);
        p3.setPrice(new BigDecimal("150.00"));
        p3.setStock(20);
        p3.setExpirationDate(LocalDate.now().plusMonths(1));
        productRepository.save(p3);

        Product p4 = new Product();
        p4.setProductCode("F001");
        p4.setName("Matcha Shake");
        p4.setCategoryEntity(categoryRepository.findByCategoryName("Beverages").orElse(null));
        p4.setReorderLevel(10);
        p4.setPrice(new BigDecimal("95.00"));
        p4.setStock(40);
        productRepository.save(p4);

        Product p5 = new Product();
        p5.setProductCode("F002");
        p5.setName("Ube Cheese Pandesal");
        p5.setCategoryEntity(categoryRepository.findByCategoryName("Add Ons").orElse(null));
        p5.setReorderLevel(5);
        p5.setPrice(new BigDecimal("60.00"));
        p5.setStock(3);
        p5.setExpirationDate(LocalDate.now().plusDays(5));
        productRepository.save(p5);

        System.out.println("Seeded " + productRepository.count() + " sample products.");
    }
}
