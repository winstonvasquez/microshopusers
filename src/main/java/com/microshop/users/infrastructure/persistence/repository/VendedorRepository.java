package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.VendedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<VendedorEntity, Long> {
    Optional<VendedorEntity> findByUsuarioId(Long usuarioId);

    // Variantes acotadas por tenant (V37). Las no acotadas quedan SOLO para el bypass explícito
    // de SUPERADMIN; cualquier otro uso reintroduce la fuga cross-tenant que corrigió B07.
    java.util.List<VendedorEntity> findByCompanyId(Long companyId);

    Optional<VendedorEntity> findByIdAndCompanyId(Long id, Long companyId);

    Optional<VendedorEntity> findByUsuarioIdAndCompanyId(Long usuarioId, Long companyId);
}
