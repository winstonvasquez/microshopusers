package com.microshop.users.infrastructure.rest;

import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String PROPERTY_TIMESTAMP = "timestamp";

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        log.warn("Credenciales inválidas: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        detail.setType(URI.create("urn:users:unauthorized"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setType(URI.create("urn:users:not-found"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        detail.setType(URI.create("urn:users:forbidden"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setType(URI.create("urn:users:business-error"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        log.warn("Conflicto de recurso: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        detail.setType(URI.create("urn:users:conflict"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setType(URI.create("urn:users:invalid-argument"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Estado inválido: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setType(URI.create("urn:users:invalid-state"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validación fallida: {}", errors);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Errores de validación: " + errors);
        detail.setType(URI.create("urn:users:validation-error"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "Recurso no encontrado: " + ex.getResourcePath());
        detail.setType(URI.create("urn:users:not-found"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Parámetro requerido ausente: {}", ex.getParameterName());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parámetro requerido ausente: " + ex.getParameterName());
        detail.setType(URI.create("urn:users:missing-parameter"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        log.warn("Tipo de parámetro inválido: {}={} (esperado {})", ex.getName(), ex.getValue(), required);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parámetro '" + ex.getName() + "' con valor '" + ex.getValue() + "' no es del tipo esperado (" + required + ")");
        detail.setType(URI.create("urn:users:type-mismatch"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor");
        detail.setType(URI.create("urn:users:internal-error"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }
}
