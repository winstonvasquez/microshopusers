package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.DireccionClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para las direcciones de envío del cliente.
 */
@Repository
public interface DireccionClienteRepository extends JpaRepository<DireccionClienteEntity, Long> {

    /**
     * Lista todas las direcciones activas de un usuario.
     *
     * @param userId ID del usuario
     * @return lista de direcciones activas
     */
    List<DireccionClienteEntity> findByUserIdAndActivoTrue(Long userId);

    /**
     * Obtiene la dirección principal activa del usuario.
     *
     * @param userId ID del usuario
     * @return dirección principal, si existe
     */
    Optional<DireccionClienteEntity> findByUserIdAndEsPrincipalTrueAndActivoTrue(Long userId);
}
