package com.glinscravings.service;

import com.glinscravings.User;
import com.glinscravings.UserRepository;
import com.glinscravings.dto.ApiResponse;
import com.glinscravings.dto.LoginRequest;
import com.glinscravings.dto.RegisterRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────
    public ApiResponse register(RegisterRequest request) {

        // Validate username
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new ApiResponse(false, "Username is required");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            return new ApiResponse(false, "Password must be at least 4 characters");
        }

        // Validate full name
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return new ApiResponse(false, "Full name is required");
        }

        // Validate role
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return new ApiResponse(false, "Role is required");
        }

        // Check duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ApiResponse(false, "Username already exists");
        }

        // Convert role string to enum
        User.Role role;
        String roleStr = request.getRole().toUpperCase();
        if ("USER".equals(roleStr)) roleStr = "EMPLOYEE";
        try {
            role = User.Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return new ApiResponse(false, "Invalid role. Must be ADMIN or EMPLOYEE");
        }

        // Encrypt password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(hashedPassword);
        user.setRole(role);

        // Save user
        userRepository.save(user);

        return new ApiResponse(true, "Registration successful");
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────
    public ApiResponse login(LoginRequest request) {

        // Validate username
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new ApiResponse(false, "Username is required");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new ApiResponse(false, "Password is required");
        }

        // Optional hardcoded admin
        if ("admin".equals(request.getUsername())) {
            if ("1234".equals(request.getPassword())) {
                return new ApiResponse(true, "Login successful", "ADMIN");
            }
            return new ApiResponse(false, "Invalid username or password");
        }

        // Find user
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "Invalid username or password");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse(false, "Invalid username or password");
        }

        return new ApiResponse(true, "Login successful", user.getRole().name());
    }

    public boolean userExists(String username) {
        if ("admin".equals(username)) return true;
        return userRepository.existsByUsername(username);
    }

    public ApiResponse resetPassword(String username, String newPassword) {
        if ("admin".equals(username)) {
            return new ApiResponse(true, "Password reset for admin");
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ApiResponse(true, "Password reset successful");
    }
}