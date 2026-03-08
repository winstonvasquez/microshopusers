package com.microshop.users.infrastructure.web.controller;

import com.microshop.users.application.command.AuthCommandService;
import com.microshop.users.application.query.AuthQueryService;
import com.microshop.users.infrastructure.web.dto.CheckEmailRequest;
import com.microshop.users.infrastructure.web.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microshop.users.infrastructure.web.dto.CheckEmailResponse;
import com.microshop.users.infrastructure.web.dto.LoginResponse;
import com.microshop.users.infrastructure.web.dto.SocialLoginRequest;
import com.microshop.users.infrastructure.web.dto.VerifyOtpRequest;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/password")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authCommandService.login(request));
    }

    @PostMapping("/check-email")
    @Operation(summary = "Check if an email is registered")
    public ResponseEntity<CheckEmailResponse> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        return ResponseEntity.ok(authQueryService.checkEmail(request.email()));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send an OTP code to the given email")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody CheckEmailRequest request) {
        authCommandService.sendOtp(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and return a JWT token")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authCommandService.verifyOtpAndLogin(request.email(), request.otp()));
    }

    @PostMapping("/social-login")
    @Operation(summary = "Login with a social provider token")
    public ResponseEntity<LoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authCommandService.socialLogin(request));
    }
}
