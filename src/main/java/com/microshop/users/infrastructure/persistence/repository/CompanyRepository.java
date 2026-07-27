package com.microshop.users.infrastructure.persistence.repository;


import com.microshop.users.application.dto.CompanyResponseDto;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByRuc(String ruc);

    Optional<CompanyEntity> findByRuc(String ruc);

    /** Bloquea la fila de la empresa durante la transacción — serializa altas concurrentes de usuarios contra el cupo del plan. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompanyEntity c WHERE c.id = :id")
    Optional<CompanyEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c")
    List<CompanyResponseDto> findAllProjected();

    /**
     * Fragmento JPQL de filtros avanzados reutilizado por {@link #searchPaged} y {@link #searchPagedScoped}
     * (búsqueda ampliada a razón social/email/dominio, plan SaaS, estado de suscripción, rubro y
     * "con dominio propio"). El plan/suscripción y el rubro se resuelven con EXISTS (no JOIN) para no
     * duplicar filas de la proyección constructor-expression ni romper el countQuery.
     */
    String FILTROS_AVANZADOS =
            "(:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR c.ruc LIKE CONCAT('%', :search, '%') " +
            "     OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.domain) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:active IS NULL OR c.isActive = :active) " +
            "AND (:fechaDesde IS NULL OR c.fechaCreacion >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR c.fechaCreacion <= :fechaHasta) " +
            "AND (:planCode IS NULL OR EXISTS (SELECT 1 FROM SaasSubscriptionEntity sub " +
            "     WHERE sub.company = c AND sub.plan.code = :planCode)) " +
            "AND (:subscriptionStatus IS NULL OR EXISTS (SELECT 1 FROM SaasSubscriptionEntity sub2 " +
            "     WHERE sub2.company = c AND sub2.status = :subscriptionStatus)) " +
            "AND (:rubroId IS NULL OR EXISTS (SELECT 1 FROM CompanyRubroEntity cr " +
            "     WHERE cr.company = c AND cr.rubro.id = :rubroId)) " +
            "AND (:conDominio IS NULL OR (:conDominio = TRUE AND c.domain IS NOT NULL AND c.domain <> '') " +
            "     OR (:conDominio = FALSE AND (c.domain IS NULL OR c.domain = '')))";

    @Query(value = "SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c " +
            "WHERE " + FILTROS_AVANZADOS,
           countQuery = "SELECT COUNT(c) FROM CompanyEntity c " +
            "WHERE " + FILTROS_AVANZADOS)
    Page<CompanyResponseDto> searchPaged(@Param("search") String search, @Param("active") Boolean active,
            @Param("fechaDesde") Instant fechaDesde, @Param("fechaHasta") Instant fechaHasta,
            @Param("planCode") String planCode, @Param("subscriptionStatus") String subscriptionStatus,
            @Param("rubroId") Long rubroId, @Param("conDominio") Boolean conDominio, Pageable pageable);

    /**
     * Ver {@link #searchPaged}, acotado ademas a UNA empresa cuando {@code companyId} es no-nulo.
     * Defensa cross-tenant: un ADMIN normal solo debe ver su propia empresa en el listado paginado
     * (el mismo que consume la pagina "Empresas" del admin), a diferencia de un SUPERADMIN.
     */
    @Query(value = "SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c " +
            "WHERE " + FILTROS_AVANZADOS + " " +
            "AND (:companyId IS NULL OR c.id = :companyId)",
           countQuery = "SELECT COUNT(c) FROM CompanyEntity c " +
            "WHERE " + FILTROS_AVANZADOS + " " +
            "AND (:companyId IS NULL OR c.id = :companyId)")
    Page<CompanyResponseDto> searchPagedScoped(@Param("search") String search, @Param("active") Boolean active,
            @Param("fechaDesde") Instant fechaDesde, @Param("fechaHasta") Instant fechaHasta,
            @Param("planCode") String planCode, @Param("subscriptionStatus") String subscriptionStatus,
            @Param("rubroId") Long rubroId, @Param("conDominio") Boolean conDominio,
            @Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c WHERE c.id = :id")
    Optional<CompanyResponseDto> findProjectedById(@Param("id") Long id);
}
