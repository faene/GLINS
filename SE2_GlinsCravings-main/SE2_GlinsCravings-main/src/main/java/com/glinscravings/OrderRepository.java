package com.glinscravings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = :orderDate ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtDateOrderByCreatedAtDesc(@Param("orderDate") LocalDate orderDate);

    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtDateBetweenOrderByCreatedAtDesc(@Param("start") LocalDate start,
                                                              @Param("end") LocalDate end);

    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusOrderByIdAsc(Order.OrderStatus status);
    List<Order> findByStatusNotOrderByIdAsc(Order.OrderStatus status);
    List<Order> findByStatusNotAndStatusNotOrderByIdAsc(Order.OrderStatus first, Order.OrderStatus second);

    @Query("SELECT COALESCE(MAX(o.queueNumber), 0) FROM Order o")
    long findMaxQueueNumber();
}
