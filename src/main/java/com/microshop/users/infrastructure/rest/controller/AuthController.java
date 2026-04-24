package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.AuthCommandService;
import com.microshop.users.application.command.PasswordResetCommandService;
import com.microshop.users.application.query.AuthQueryService;
import com.microshop.users.application.dto.CheckEmailRequest;
import com.microshop.users.application.dto.ForgotPasswordRequest;
import com.microshop.users.application.dto.LoginRequest;
import com.microshop.users.application.dto.ResetPasswordRequest;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microshop.users.application.dto.CheckEmailResponse;
import com.microshop.users.application.dto.LoginResponse;
import com.microshop.users.application.dto.SocialLoginRequest;
import com.microshop.users.application.dto.VerifyOtpRequest;


@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;
    private final PasswordResetCommandService passwordResetCommandService;

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

    @PostMapping("/pin-login")
    @Operation(summary = "Login with numeric PIN (POS quick login)")
    public ResponseEntity<LoginResponse> pinLogin(
            @RequestParam String pin,
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(authCommandService.pinLogin(pin, companyId));
    }

    @PostMapping("/users/{userId}/set-pin")
    @Operation(summary = "Set numeric PIN for POS quick login")
    public ResponseEntity<Void> setPin(@PathVariable Long userId, @RequestParam String pin) {
        authCommandService.setPin(userId, pin);
        return ResponseEntity.ok().build();
    }

    /**
     * Inicia el flujo de recuperación de contraseña.
     * Siempre responde 200 para no revelar si el email existe en el sistema.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Solicita el envío de un enlace para recuperar la contraseña")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetCommandService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    /**
     * Restablece la contraseña usando el token recibido por correo.
     * Responde 400 si el token es inválido, ya fue usado o expiró.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Restablece la contraseña usando un token de recuperación")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetCommandService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
