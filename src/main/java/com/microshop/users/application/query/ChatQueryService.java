package com.microshop.users.application.query;

import com.microshop.users.application.dto.ChatConversacionResponseDto;
import com.microshop.users.application.dto.ChatMensajeResponseDto;
import com.microshop.users.application.mapper.ChatMapper;
import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import com.microshop.users.infrastructure.persistence.repository.ChatConversacionRepository;
import com.microshop.users.infrastructure.persistence.repository.ChatMensajeRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Consultas (lecturas) del chat de soporte cliente ↔ MicroShop.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatQueryService {

    private final ChatConversacionRepository conversacionRepo;
    private final ChatMensajeRepository mensajeRepo;
    private final UsuarioRepository usuarioRepo;
    private final ChatMapper chatMapper;

    /**
     * Obtiene la conversación activa (ABIERTA) del cliente autenticado.
     */
    @Transactional(readOnly = true)
    public Optional<ChatConversacionResponseDto> getConversacionActiva(String username) {
        Long clienteId = resolverClienteId(username);
        return conversacionRepo
                .findFirstByClienteIdAndEstadoOrderByCreatedAtDesc(clienteId, "ABIERTA")
                .map(chatMapper::toDto);
    }

    /**
     * Obtiene mensajes de una conversación, opcionalmente desde una fecha para polling.
     */
    @Transactional(readOnly = true)
    public List<ChatMensajeResponseDto> getMensajes(Long conversacionId, String since) {
        List<ChatMensajeEntity> mensajes;
        if (since != null && !since.isBlank()) {
            Instant sinceInstant = Instant.parse(since);
            mensajes = mensajeRepo.findByConversacionIdAndTimestampAfterOrderByTimestampAsc(conversacionId, sinceInstant);
        } else {
            mensajes = mensajeRepo.findByConversacionIdOrderByTimestampAsc(conversacionId);
        }
        return mensajes.stream().map(chatMapper::toDto).toList();
    }

    /**
     * Cuenta los mensajes no leídos del SOPORTE para el badge del cliente.
     */
    @Transactional(readOnly = true)
    public long unreadCount(Long conversacionId) {
        return mensajeRepo.countByConversacionIdAndLeidoFalseAndEmisorTipo(conversacionId, "SOPORTE");
    }

    /**
     * Lista todas las conversaciones activas para el panel de soporte.
     */
    @Transactional(readOnly = true)
    public List<ChatConversacionResponseDto> listarConversacionesAdmin() {
        return conversacionRepo.findByEstado("ABIERTA").stream().map(chatMapper::toDto).toList();
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
