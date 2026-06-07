package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    /** Projection para listados — evita cargar contraseña y datos sensibles. */
    interface UsuarioSummary {
        Long getId();
        String getUsername();
        String getEmail();
        boolean isActivo();
    }

    Optional<UsuarioEntity> findByUsername(String username);

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPersonaNumeroDocumento(String numeroDocumento);

    java.util.List<UsuarioEntity> findByRolId(Long rolId);

    Page<UsuarioSummary> findProjectedBy(Pageable pageable);

    Optional<UsuarioEntity> findByPinHash(String pinHash);

    /**
     * Candidatos para login por PIN: solo usuarios que tienen un PIN configurado.
     * Evita iterar (y BCrypt-comparar) toda la tabla de usuarios.
     */
    java.util.List<UsuarioEntity> findByPinHashIsNotNull();

    /**
     * Candidatos para autorización de supervisor: usuarios con PIN configurado
     * cuyo rol está entre los autorizadores. Reduce el set a un puñado antes del
     * BCrypt-match (los roles se siembran en mayúsculas: ADMIN/GERENTE/SUPERADMIN).
     */
    java.util.List<UsuarioEntity> findByPinHashIsNotNullAndRol_NombreIn(java.util.Collection<String> rolNombres);
}
