package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.infrastructure.persistence.entity.NotificationEntity;
import com.microshop.users.infrastructure.persistence.repository.NotificationRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint interno para que otros microservicios creen notificaciones in-app.
 * No requiere JWT — restringido a red interna por configuración de red/firewall.
 */
@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notificaciones Internas", description = "Crear notificaciones desde otros microservicios")
public class InternalNotificationController {

    private final NotificationRepository notificationRepo;
    private final UsuarioRepository usuarioRepo;

    public record InternalNotificationRequest(
            Long userId,
            String type,
            String title,
            String body,
            Long referenceId,
            String referenceType
    ) {}

    @PostMapping
    @Transactional
    @Operation(summary = "Crear notificación in-app desde microservicio interno")
    public ResponseEntity<Void> create(@RequestBody InternalNotificationRequest req) {
        usuarioRepo.findById(req.userId()).ifPresent(usuario -> {
            NotificationEntity notification = NotificationEntity.builder()
                    .usuario(usuario)
                    .type(req.type())
                    .title(req.title())
                    .body(req.body())
                    .referenceId(req.referenceId())
                    .referenceType(req.referenceType())
                    .build();
            notificationRepo.save(notification);
            log.info("Notificación interna creada: tipo={}, userId={}", req.type(), req.userId());
        });
        return ResponseEntity.ok().build();
    }
}
