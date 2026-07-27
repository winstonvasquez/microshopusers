package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.ChatCommandService;
import com.microshop.users.application.dto.ChatConversacionResponseDto;
import com.microshop.users.application.dto.ChatMensajeResponseDto;
import com.microshop.users.application.query.ChatQueryService;
import com.microshop.users.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador REST para el chat de soporte cliente ↔ MicroShop.
 * Implementa HTTP polling (GET mensajes?since=ISO_TIMESTAMP cada 5s en el frontend).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Soporte", description = "Mensajería cliente-soporte con polling HTTP")
public class ChatController {

    private final ChatCommandService commandService;
    private final ChatQueryService queryService;

    // ─────────────────────────────────────────────────────────────────────────
    // Endpoints CLIENTE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inicia una nueva conversación de soporte para el cliente autenticado.
     * POST /api/chat/conversaciones
     */
    @PostMapping("/api/chat/conversaciones")
    @Operation(summary = "Iniciar conversación de soporte")
    public ResponseEntity<ChatConversacionResponseDto> crearConversacion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) Map<String, String> body) {

        String asunto = body != null ? body.get("asunto") : null;
        ChatConversacionResponseDto conv = commandService.crearConversacion(principal.getUsername(), asunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(conv);
    }

    /**
     * Obtiene la conversación activa (ABIERTA) del cliente autenticado.
     * GET /api/chat/conversaciones/activa
     */
    @GetMapping("/api/chat/conversaciones/activa")
    @Operation(summary = "Obtener conversación activa del cliente")
    public ResponseEntity<ChatConversacionResponseDto> getConversacionActiva(
            @AuthenticationPrincipal UserDetails principal) {

        return queryService.getConversacionActiva(principal.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Envía un mensaje en una conversación.
     * POST /api/chat/conversaciones/{id}/mensajes
     */
    @PostMapping("/api/chat/conversaciones/{id}/mensajes")
    @Operation(summary = "Enviar mensaje en la conversación")
    public ResponseEntity<ChatMensajeResponseDto> enviarMensaje(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {

        String contenido = body.get("contenido");
        if (contenido == null || contenido.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        ChatMensajeResponseDto msg = commandService.enviarMensaje(id, principal.getUsername(), authorities, contenido);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    /**
     * Obtiene mensajes de una conversación, opcionalmente desde una fecha para polling.
     * GET /api/chat/conversaciones/{id}/mensajes?since=ISO_TIMESTAMP
     */
    @GetMapping("/api/chat/conversaciones/{id}/mensajes")
    @Operation(summary = "Obtener mensajes (polling) — since es ISO-8601 opcional")
    public ResponseEntity<List<ChatMensajeResponseDto>> getMensajes(
            @PathVariable Long id,
            @RequestParam(required = false) String since) {

        return ResponseEntity.ok(queryService.getMensajes(id, since));
    }

    /**
     * Marca como leídos los mensajes del SOPORTE en una conversación (el cliente los leyó).
     * PUT /api/chat/conversaciones/{id}/leer
     */
    @PutMapping("/api/chat/conversaciones/{id}/leer")
    @Operation(summary = "Marcar mensajes de soporte como leídos")
    public ResponseEntity<Void> marcarLeido(@PathVariable Long id) {
        commandService.marcarLeido(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve el contador de mensajes no leídos del SOPORTE para el badge del cliente.
     * GET /api/chat/conversaciones/{id}/unread-count
     */
    @GetMapping("/api/chat/conversaciones/{id}/unread-count")
    @Operation(summary = "Contador de mensajes no leídos del soporte")
    public ResponseEntity<Map<String, Long>> unreadCount(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("count", queryService.unreadCount(id)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Endpoints ADMIN / SOPORTE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lista conversaciones para el panel de soporte, paginado y con filtros avanzados
     * (2026-07-27: antes devolvía SIEMPRE solo las ABIERTA, sin paginar ni filtrar).
     * GET /api/admin/chat/conversaciones
     */
    @GetMapping("/api/admin/chat/conversaciones")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_SOPORTE)
    @Operation(summary = "Listar conversaciones (admin) con búsqueda por asunto, estado, cliente y rango de fechas")
    public ResponseEntity<Page<ChatConversacionResponseDto>> listarConversacionesAdmin(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastMessageAtDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastMessageAtHasta) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
        // LocalDate (yyyy-MM-dd, lo que envía el date-range del frontend) -> Instant día completo.
        Instant createdDesde = toInstantInicioDia(createdAtDesde);
        Instant createdHasta = toInstantFinDia(createdAtHasta);
        Instant lastMsgDesde = toInstantInicioDia(lastMessageAtDesde);
        Instant lastMsgHasta = toInstantFinDia(lastMessageAtHasta);
        return ResponseEntity.ok(queryService.listarConversacionesAdmin(
                search, estado, clienteId, createdDesde, createdHasta, lastMsgDesde, lastMsgHasta, pageable));
    }

    private static Instant toInstantInicioDia(LocalDate fecha) {
        return fecha != null ? fecha.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
    }

    private static Instant toInstantFinDia(LocalDate fecha) {
        return fecha != null ? fecha.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    /**
     * El equipo de soporte envía un mensaje en nombre de "SOPORTE".
     * POST /api/admin/chat/conversaciones/{id}/mensajes
     */
    @PostMapping("/api/admin/chat/conversaciones/{id}/mensajes")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_SOPORTE)
    @Operation(summary = "Soporte responde en la conversación")
    public ResponseEntity<ChatMensajeResponseDto> responderComoSoporte(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {

        String contenido = body.get("contenido");
        if (contenido == null || contenido.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChatMensajeResponseDto msg = commandService.responderComoSoporte(id, principal.getUsername(), contenido);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }
}
