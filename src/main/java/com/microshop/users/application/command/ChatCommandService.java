package com.microshop.users.application.command;

import com.microshop.users.application.dto.ChatConversacionResponseDto;
import com.microshop.users.application.dto.ChatMensajeResponseDto;
import com.microshop.users.application.mapper.ChatMapper;
import com.microshop.users.infrastructure.persistence.entity.ChatConversacionEntity;
import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import com.microshop.users.infrastructure.persistence.repository.ChatConversacionRepository;
import com.microshop.users.infrastructure.persistence.repository.ChatMensajeRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.constants.AppConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Comandos (mutaciones) del chat de soporte cliente ↔ MicroShop.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCommandService {

    private static final Set<String> ROLES_STAFF = Set.of(
            AppConstants.Seguridad.ROLE_ADMIN,
            AppConstants.Seguridad.ROLE_SUPERADMIN,
            AppConstants.Seguridad.ROLE_SOPORTE);

    private final ChatConversacionRepository conversacionRepo;
    private final ChatMensajeRepository mensajeRepo;
    private final UsuarioRepository usuarioRepo;
    private final ChatMapper chatMapper;

    /**
     * Inicia una nueva conversación de soporte para el cliente autenticado.
     */
    @Transactional
    public ChatConversacionResponseDto crearConversacion(String username, String asunto) {
        Long clienteId = resolverClienteId(username);

        ChatConversacionEntity conv = ChatConversacionEntity.builder()
                .clienteId(clienteId)
                .asunto(asunto)
                .estado("ABIERTA")
                .build();
        conversacionRepo.save(conv);
        log.info("Nueva conversación de chat creada: id={}, clienteId={}", conv.getId(), clienteId);
        return chatMapper.toDto(conv);
    }

    /**
     * Envía un mensaje en una conversación. Determina el tipo de emisor (CLIENTE/SOPORTE)
     * según las authorities del usuario autenticado.
     */
    @Transactional
    public ChatMensajeResponseDto enviarMensaje(Long conversacionId, String username, Set<String> authorities, String contenido) {
        ChatConversacionEntity conv = conversacionRepo.findById(conversacionId)
                .orElseThrow(() -> new EntityNotFoundException("Conversación no encontrada: " + conversacionId));

        Long emisorId = resolverClienteId(username);
        // Determinar tipo de emisor: el personal de staff (admin/superadmin/soporte)
        // envía como "SOPORTE"; el resto como "CLIENTE". Comparación EXACTA contra la
        // authority ROLE_* — antes usaba .contains() (matching débil que matchearía
        // cualquier authority conteniendo el literal). Fix auditoría 2026-07-20.
        boolean esAdmin = authorities.stream().anyMatch(ROLES_STAFF::contains);
        String emisorTipo = esAdmin ? "SOPORTE" : "CLIENTE";

        ChatMensajeEntity msg = ChatMensajeEntity.builder()
                .conversacionId(conversacionId)
                .emisorId(emisorId)
                .emisorTipo(emisorTipo)
                .contenido(contenido.trim())
                .build();
        mensajeRepo.save(msg);

        // Actualizar lastMessageAt de la conversación
        conv.setLastMessageAt(msg.getTimestamp());
        conversacionRepo.save(conv);

        log.info("Mensaje enviado: convId={}, emisor={}, tipo={}", conversacionId, emisorId, emisorTipo);
        return chatMapper.toDto(msg);
    }

    /**
     * Marca como leídos los mensajes del SOPORTE en una conversación (el cliente los leyó).
     */
    @Transactional
    public void marcarLeido(Long conversacionId) {
        var noLeidos = mensajeRepo
                .findByConversacionIdOrderByTimestampAsc(conversacionId)
                .stream()
                .filter(m -> !m.isLeido() && "SOPORTE".equals(m.getEmisorTipo()))
                .toList();

        noLeidos.forEach(m -> m.setLeido(true));
        mensajeRepo.saveAll(noLeidos);
        log.info("Marcados {} mensajes como leídos en convId={}", noLeidos.size(), conversacionId);
    }

    /**
     * El equipo de soporte envía un mensaje en nombre de "SOPORTE".
     */
    @Transactional
    public ChatMensajeResponseDto responderComoSoporte(Long conversacionId, String username, String contenido) {
        ChatConversacionEntity conv = conversacionRepo.findById(conversacionId)
                .orElseThrow(() -> new EntityNotFoundException("Conversación no encontrada: " + conversacionId));

        Long emisorId = resolverClienteId(username);

        ChatMensajeEntity msg = ChatMensajeEntity.builder()
                .conversacionId(conversacionId)
                .emisorId(emisorId)
                .emisorTipo("SOPORTE")
                .contenido(contenido.trim())
                .build();
        mensajeRepo.save(msg);

        conv.setLastMessageAt(msg.getTimestamp());
        conversacionRepo.save(conv);

        log.info("Soporte respondió: convId={}, adminId={}", conversacionId, emisorId);
        return chatMapper.toDto(msg);
    }

    /**
     * Resuelve el ID de usuario a partir del username (email) extraído del JWT.
     */
    private Long resolverClienteId(String username) {
        return usuarioRepo.findByUsername(username)
                .map(u -> u.getId())
                .orElseGet(() -> usuarioRepo.findByEmail(username)
                        .map(u -> u.getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Usuario no encontrado: " + username)));
    }
}
