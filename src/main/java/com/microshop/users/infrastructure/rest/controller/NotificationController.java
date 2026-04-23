package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.infrastructure.persistence.entity.NotificationEntity;
import com.microshop.users.infrastructure.persistence.repository.NotificationRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Notificaciones in-app del usuario autenticado.
 * Todos los endpoints requieren autenticación.
 */
@RestController
@RequestMapping(ApiPaths.USERS + "/me/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notificaciones", description = "Notificaciones in-app del usuario autenticado")
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final UsuarioRepository usuarioRepo;

    /** DTO de respuesta — record para mantener el patrón del proyecto. */
    public record NotificationResponse(
            Long id,
            String type,
            String title,
            String body,
            Long referenceId,
            String referenceType,
            boolean read,
            Instant createdAt
    ) {}

    private Long resolveUserId(String username) {
        return usuarioRepo.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new NotFoundException("usuario", username));
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones paginadas")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = resolveUserId(userDetails.getUsername());
        Page<NotificationResponse> result = notificationRepo
                .findByUsuarioIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(n -> new NotificationResponse(
                        n.getId(), n.getType(), n.getTitle(), n.getBody(),
                        n.getReferenceId(), n.getReferenceType(), n.isRead(), n.getCreatedAt()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Número de notificaciones no leídas")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails.getUsername());
        long count = notificationRepo.countByUsuarioIdAndReadFalse(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @Transactional
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = resolveUserId(userDetails.getUsername());
        notificationRepo.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Transactional
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails.getUsername());
        notificationRepo.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Eliminar notificación")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = resolveUserId(userDetails.getUsername());
        notificationRepo.findById(id).ifPresent(n -> {
            if (n.getUsuario().getId().equals(userId)) {
                notificationRepo.delete(n);
            }
        });
        return ResponseEntity.noContent().build();
    }

}
