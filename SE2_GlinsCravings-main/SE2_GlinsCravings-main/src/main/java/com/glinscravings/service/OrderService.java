package com.glinscravings.service;

import com.glinscravings.Order;
import com.glinscravings.OrderItem;
import com.glinscravings.OrderRepository;
import com.glinscravings.Product;
import com.glinscravings.ProductRepository;
import com.glinscravings.dto.ApiResponse;
import com.glinscravings.dto.OrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ApiResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return new ApiResponse(false, "Order must have at least one item");
        }

        Order order = new Order();
        order.setQueueNumber((int) orderRepository.findMaxQueueNumber() + 1);
        order.setOrderType(Order.OrderType.IN_STORE);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCompletedAt(null);
        order.setCustomerName(
            request.getCustomerName() != null && !request.getCustomerName().isBlank()
                ? request.getCustomerName() : "Guest"
        );

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            BigDecimal unitPrice = BigDecimal.valueOf(itemReq.getUnitPrice());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProductName(itemReq.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            item.setOrder(order);
            order.getItems().add(item);

            Optional<Product> productOpt = productRepository.findByName(itemReq.getProductName());
            productOpt.ifPresent(product -> {
                item.setProduct(product);
                product.setStock(Math.max(0, product.getStock() - itemReq.getQuantity()));
                product.recomputeStatus();
                productRepository.save(product);
            });
        }

        order.setTotalAmount(total);
        orderRepository.save(order);
        return new ApiResponse(true, "Order created", order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByDate(LocalDate date) {
        return orderRepository.findByCreatedAtDateOrderByCreatedAtDesc(date);
    }

    public List<Order> getQueue() {
        return orderRepository.findByStatusNotAndStatusNotOrderByIdAsc(Order.OrderStatus.COMPLETED, Order.OrderStatus.CANCELLED);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatusOrderByIdAsc(status);
    }

    public ApiResponse updateOrderStatus(Long id, Order.OrderStatus status) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) {
            return new ApiResponse(false, "Order not found");
        }
        Order order = opt.get();
        order.setStatus(status);
        if (status == Order.OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
        return new ApiResponse(true, "Status updated to " + status, order);
    }
}
