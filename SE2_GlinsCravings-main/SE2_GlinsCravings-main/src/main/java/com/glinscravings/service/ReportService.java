package com.glinscravings.service;

import com.glinscravings.Order;
import com.glinscravings.OrderItem;
import com.glinscravings.OrderRepository;
import com.glinscravings.dto.SalesReportResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start;
        List<Order> rangeOrders = orderRepository.findByCreatedAtDateBetweenOrderByCreatedAtDesc(start, end);
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalSales = BigDecimal.ZERO;
        Map<String, Integer> productCount = new HashMap<>();

        for (Order order : rangeOrders) {
            totalSales = totalSales.add(order.getTotalAmount());
            for (OrderItem item : order.getItems()) {
                productCount.merge(item.getProductName(), item.getQuantity(), Integer::sum);
            }
        }

        String topSeller = productCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        SalesReportResponse report = new SalesReportResponse();
        report.setTotalSalesToday(totalSales);
        report.setOrdersProcessed(rangeOrders.size());
        report.setTopSeller(topSeller);
        report.setTotalOrders(allOrders.size());

        return report;
    }
}
