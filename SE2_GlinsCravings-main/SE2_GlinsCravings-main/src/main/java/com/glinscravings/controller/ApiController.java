package com.glinscravings.controller;

import com.glinscravings.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping
    public ApiResponse root() {
        return new ApiResponse(
                true,
                "Glins Cravings backend is running",
                Map.of(
                        "products", "/api/products",
                        "orders", "/api/orders",
                        "reports", "/api/reports",
                        "auth", "/api/login and /api/register"
                )
        );
    }
}
