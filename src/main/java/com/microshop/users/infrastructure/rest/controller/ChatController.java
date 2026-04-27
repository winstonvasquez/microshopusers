package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.infrastructure.persistence.entity.ChatConversacionEntity;
import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import com.microshop.users.infrastructure.persistence.repository.ChatConversacionRepository;
import com.microshop.users.infrastructure.persistence.repository.ChatMensajeRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el chat de soporte cliente ↔ MicroShop.
 * Implementa HTTP polling (GET mensajes?since=ISO_TIMESTAMP cada 5s en el frontend).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Soporte", description = "Mensajería cliente-soporte con polling HTTP")
public class ChatController {

    private final ChatConversacionRepository conversacionRepo;
    private final ChatMensajeRepository mensajeRepo;
    private final UsuarioRepository usuarioRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Endpoints CLIENTE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inicia una nueva conversación de soporte para el cliente autenticado.
     * POST /api/chat/conversaciones
     */
    @PostMapping("/api/chat/conversaciones")
    @Operation(summary = "Iniciar conversación de soporte")
    public ResponseEntity<ChatConversacionEntity> crearConversacion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) Map<String, String> body) {

        Long clienteId = resolverClienteId(principal.getUsername());
        String asunto = body != null ? body.get("asunto") : null;

        ChatConversacionEntity conv = ChatConversacionEntity.builder()
                .clienteId(clienteId)
                .asunto(asunto)
                .estado("ABIERTA")
                .build();
        conversacionRepo.save(conv);
        log.info("Nueva conversación de chat creada: id={}, clienteId={}", conv.getId(), clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(conv);
    }

    /**
     * Obtiene la conversación activa (ABIERTA) del cliente autenticado.
     * GET /api/chat/conversaciones/activa
     */
    @GetMapping("/api/chat/conversaciones/activa")
    @Operation(summary = "Obtener conversación activa del cliente")
    public ResponseEntity<ChatConversacionEntity> getConversacionActiva(
            @AuthenticationPrincipal UserDetails principal) {

        Long clienteId = resolverClienteId(principal.getUsername());
        return conversacionRepo
                .findFirstByClienteIdAndEstadoOrderByCreatedAtDesc(clienteId, "ABIERTA")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Envía un mensaje en una conversación.
     * POST /api/chat/conversaciones/{id}/mensajes
     */
    @PostMapping("/api/chat/conversaciones/{id}/mensajes")
    @Operation(summary = "Enviar mensaje en la conversación")
    public ResponseEntity<ChatMensajeEntity> enviarMensaje(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {

        ChatConversacionEntity conv = conversacionRepo.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Conversación no encontrada: " + id));

        Long emisorId = resolverClienteId(principal.getUsername());
        // Determinar tipo de emisor según si el usuario es admin/soporte
        boolean esAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("SOPORTE"));
        String emisorTipo = esAdmin ? "SOPORTE" : "CLIENTE";

        String contenido = body.get("contenido");
        if (contenido == null || contenido.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChatMensajeEntity msg = ChatMensajeEntity.builder()
                .conversacionId(id)
                .emisorId(emisorId)
                .emisorTipo(emisorTipo)
                .contenido(contenido.trim())
                .build();
        mensajeRepo.save(msg);

        // Actualizar lastMessageAt de la conversación
        conv.setLastMessageAt(msg.getTimestamp());
        conversacionRepo.save(conv);

        log.info("Mensaje enviado: convId={}, emisor={}, tipo={}", id, emisorId, emisorTipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    /**
     * Obtiene mensajes de una conversación, opcionalmente desde una fecha para polling.
     * GET /api/chat/conversaciones/{id}/mensajes?since=ISO_TIMESTAMP
     */
    @GetMapping("/api/chat/conversaciones/{id}/mensajes")
    @Operation(summary = "Obtener mensajes (polling) — since es ISO-8601 opcional")
    public ResponseEntity<List<ChatMensajeEntity>> getMensajes(
            @PathVariable Long id,
            @RequestParam(required = false) String since) {

        List<ChatMensajeEntity> mensajes;
        if (since != null && !since.isBlank()) {
            Instant sinceInstant = Instant.parse(since);
            mensajes = mensajeRepo.findByConversacionIdAndTimestampAfterOrderByTimestampAsc(id, sinceInstant);
        } else {
            mensajes = mensajeRepo.findByConversacionIdOrderByTimestampAsc(id);
        }
        return ResponseEntity.ok(mensajes);
    }

    /**
     * Marca como leídos los mensajes del SOPORTE en una conversación (el cliente los leyó).
     * PUT /api/chat/conversaciones/{id}/leer
     */
    @PutMapping("/api/chat/conversaciones/{id}/leer")
    @Operation(summary = "Marcar mensajes de soporte como leídos")
    public ResponseEntity<Void> marcarLeido(@PathVariable Long id) {
        List<ChatMensajeEntity> noLeidos = mensajeRepo
                .findByConversacionIdOrderByTimestampAsc(id)
                .stream()
                .filter(m -> !m.isLeido() && "SOPORTE".equals(m.getEmisorTipo()))
                .toList();

        noLeidos.forEach(m -> m.setLeido(true));
        mensajeRepo.saveAll(noLeidos);
        log.info("Marcados {} mensajes como leídos en convId={}", noLeidos.size(), id);
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve el contador de mensajes no leídos del SOPORTE para el badge del cliente.
     * GET /api/chat/conversaciones/{id}/unread-count
     */
    @GetMapping("/api/chat/conversaciones/{id}/unread-count")
    @Operation(summary = "Contador de mensajes no leídos del soporte")
    public ResponseEntity<Map<String, Long>> unreadCount(@PathVariable Long id) {
        long count = mensajeRepo.countByConversacionIdAndLeidoFalseAndEmisorTipo(id, "SOPORTE");
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Endpoints ADMIN / SOPORTE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lista todas las conversaciones activas para el panel de soporte.
     * GET /api/admin/chat/conversaciones
     */
    @GetMapping("/api/admin/chat/conversaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    @Operation(summary = "Listar conversaciones activas (admin)")
    public ResponseEntity<List<ChatConversacionEntity>> listarConversacionesAdmin() {
        List<ChatConversacionEntity> activas = conversacionRepo.findByEstado("ABIERTA");
        return ResponseEntity.ok(activas);
    }

    /**
     * El equipo de soporte envía un mensaje en nombre de "SOPORTE".
     * POST /api/admin/chat/conversaciones/{id}/mensajes
     */
    @PostMapping("/api/admin/chat/conversaciones/{id}/mensajes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    @Operation(summary = "Soporte responde en la conversación")
    public ResponseEntity<ChatMensajeEntity> responderComoSoporte(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {

        ChatConversacionEntity conv = conversacionRepo.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Conversación no encontrada: " + id));

        Long emisorId = resolverClienteId(principal.getUsername());
        String contenido = body.get("contenido");
        if (contenido == null || contenido.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChatMensajeEntity msg = ChatMensajeEntity.builder()
                .conversacionId(id)
                .emisorId(emisorId)
                .emisorTipo("SOPORTE")
                .contenido(contenido.trim())
                .build();
        mensajeRepo.save(msg);

        conv.setLastMessageAt(msg.getTimestamp());
        conversacionRepo.save(conv);

        log.info("Soporte respondió: convId={}, adminId={}", id, emisorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resuelve el ID de usuario a partir del username (email) extraído del JWT.
     */
    private Long resolverClienteId(String username) {
        return usuarioRepo.findByUsername(username)
                .map(u -> u.getId())
                .orElseGet(() -> usuarioRepo.findByEmail(username)
                        .map(u -> u.getId())
                        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                "Usuario no encontrado: " + username)));
    }
}
