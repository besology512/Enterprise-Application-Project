package com.workhub.controller;

import com.workhub.dto.JwtResponse;
import com.workhub.dto.LoginRequest;
import com.workhub.dto.UserResponse;
import com.workhub.model.User;
import com.workhub.repository.UserRepository;
import com.workhub.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication — login and current user info")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider tokenProvider;

    @Operation(summary = "Login and receive a JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — copy the accessToken value and use it as Bearer token"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid fields"),
        @ApiResponse(responseCode = "401", description = "Wrong email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        String jwt = tokenProvider.generateToken(authentication, user.getTenantId());

        return ResponseEntity.ok(new JwtResponse(jwt, user.getTenantId()));
    }

    @Operation(summary = "Get the currently authenticated user's profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(new UserResponse(user));
    }
}
