package com.glinscravings.dto;

import java.math.BigDecimal;

public class SalesReportResponse {
    private BigDecimal totalSalesToday;
    private long ordersProcessed;
    private String topSeller;
    private long totalOrders;

    public SalesReportResponse() {}

    public SalesReportResponse(BigDecimal totalSalesToday, long ordersProcessed, String topSeller, long totalOrders) {
        this.totalSalesToday = totalSalesToday;
        this.ordersProcessed = ordersProcessed;
        this.topSeller = topSeller;
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSalesToday() { return totalSalesToday; }
    public void setTotalSalesToday(BigDecimal totalSalesToday) { this.totalSalesToday = totalSalesToday; }

    public long getOrdersProcessed() { return ordersProcessed; }
    public void setOrdersProcessed(long ordersProcessed) { this.ordersProcessed = ordersProcessed; }

    public String getTopSeller() { return topSeller; }
    public void setTopSeller(String topSeller) { this.topSeller = topSeller; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
}
