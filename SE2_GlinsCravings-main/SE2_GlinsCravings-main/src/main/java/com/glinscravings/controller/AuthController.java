package com.glinscravings.controller;

import com.glinscravings.dto.ApiResponse;
import com.glinscravings.dto.LoginRequest;
import com.glinscravings.dto.RegisterRequest;
import com.glinscravings.service.AuthService;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        ApiResponse response = authService.register(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        ApiResponse response = authService.login(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(response, status);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is required"));
        }
        boolean exists = authService.userExists(username);
        if (exists) {
            return ResponseEntity.ok(new ApiResponse(true, "User found. Proceed to reset."));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username not found"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("password");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is required"));
        }
        if (newPassword == null || newPassword.length() < 4) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Password must be at least 4 characters"));
        }
        ApiResponse response = authService.resetPassword(username, newPassword);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }
}
