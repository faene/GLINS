package com.glinscravings.controller;

import com.glinscravings.Order;
import com.glinscravings.dto.ApiResponse;
import com.glinscravings.dto.OrderRequest;
import com.glinscravings.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return orderService.getOrder(id)
                .map(order -> ResponseEntity.ok((Object) order))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody OrderRequest request) {
        ApiResponse response = orderService.createOrder(request);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @GetMapping("/queue")
    public List<Order> getQueue() {
        return orderService.getQueue();
    }

    @GetMapping("/queue/preparing")
    public List<Order> getPreparing() {
        return orderService.getOrdersByStatus(Order.OrderStatus.PENDING);
    }

    @GetMapping("/queue/ready")
    public List<Order> getReady() {
        return orderService.getOrdersByStatus(Order.OrderStatus.PROCESSING);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Status is required"));
        }
        try {
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            ApiResponse response = orderService.updateOrderStatus(id, status);
            return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                    .body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid status"));
        }
    }
}
