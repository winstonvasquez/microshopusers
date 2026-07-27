package com.microshop.users.application.query;

import com.microshop.users.application.dto.ChatConversacionResponseDto;
import com.microshop.users.application.dto.ChatMensajeResponseDto;
import com.microshop.users.application.mapper.ChatMapper;
import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import com.microshop.users.infrastructure.persistence.repository.ChatConversacionRepository;
import com.microshop.users.infrastructure.persistence.repository.ChatMensajeRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.util.AppUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Lista conversaciones para el panel de soporte, paginado y con filtros avanzados opcionales
     * (2026-07-27): antes hardcodeaba estado="ABIERTA" y devolvía la lista completa sin paginar.
     * Nota: ChatConversacionEntity no tiene companyId (chat cliente↔MicroShop es transversal a
     * tenants) — sin scope de tenant a propósito, igual que antes de este cambio.
     */
    @Transactional(readOnly = true)
    public Page<ChatConversacionResponseDto> listarConversacionesAdmin(String search, String estado, Long clienteId,
                                                                        Instant createdAtDesde, Instant createdAtHasta,
                                                                        Instant lastMessageAtDesde, Instant lastMessageAtHasta,
                                                                        Pageable pageable) {
        String term = AppUtils.searchTermOrNull(search);
        String estadoFiltro = AppUtils.searchTermOrNull(estado);
        return conversacionRepo.searchAdminPaged(term, estadoFiltro, clienteId,
                        createdAtDesde, createdAtHasta, lastMessageAtDesde, lastMessageAtHasta, pageable)
                .map(chatMapper::toDto);
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
