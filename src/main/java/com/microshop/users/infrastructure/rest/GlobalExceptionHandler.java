package com.microshop.users.infrastructure.rest;

import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

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

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
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

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de bloqueo optimista: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El registro fue modificado por otra operación, reintenta");
        detail.setType(URI.create("urn:users:optimistic-lock-conflict"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return detail;
    }

    /**
     * Verbo HTTP incorrecto sobre una ruta existente: 405, no 500.
     *
     * <p>Sin este handler, el {@code @ExceptionHandler(Exception.class)} de mas abajo ENSOMBRECE el
     * manejo por defecto de Spring MVC y un POST a una ruta que solo tiene GET sale como
     * "Error interno del servidor". Quien integra contra la API se equivoca de verbo y se va a
     * buscar el fallo en el servidor en vez de en su llamada. Es la misma familia del incidente ya
     * documentado con {@code AccessDeniedException}, que reportaba un bloqueo cross-tenant como 500
     * en vez de 403: un handler generico comiendose excepciones que traen su propia semantica HTTP.</p>
     *
     * <p>Se propaga la cabecera {@code Allow} con los verbos que la ruta si admite, que es lo que
     * pide la especificacion para un 405 y lo que necesita un cliente para corregirse solo.</p>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMetodoNoSoportado(HttpRequestMethodNotSupportedException ex) {
        log.warn("Metodo HTTP no soportado: {} (admitidos: {})", ex.getMethod(), ex.getSupportedHttpMethods());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en esta ruta");
        detail.setType(URI.create("urn:users:method-not-allowed"));
        detail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        if (ex.getSupportedHttpMethods() != null) {
            detail.setProperty("metodosPermitidos", ex.getSupportedHttpMethods().toString());
        }
        HttpHeaders headers = new HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(new java.util.LinkedHashSet<>(ex.getSupportedHttpMethods()));
        }
        return new ResponseEntity<>(detail, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Content-Type no soportado: 415, no 500. Mismo motivo que el handler de arriba.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNoSoportado(HttpMediaTypeNotSupportedException ex) {
        log.warn("Content-Type no soportado: {}", ex.getContentType());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido enviado no esta soportado en esta ruta");
        detail.setType(URI.create("urn:users:unsupported-media-type"));
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
