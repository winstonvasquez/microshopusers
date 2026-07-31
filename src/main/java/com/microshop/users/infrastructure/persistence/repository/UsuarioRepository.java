package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    // findByRolId(Long) se eliminó: derivado de Spring Data, traducía a "WHERE rol_id = ?" sin
    // acotar por empresa y era el origen de una fuga de PII cross-tenant en /api/users/by-rol/{id}.
    // Para filtrar por rol usar findByCompanyIdFiltered(companyId, ..., rolId, ...), que respeta
    // la tabla de membresías user_company.

    Page<UsuarioSummary> findProjectedBy(Pageable pageable);

    Optional<UsuarioEntity> findByPinHash(String pinHash);

    /**
     * Candidatos para login por PIN: solo usuarios que tienen un PIN configurado.
     * Evita iterar (y BCrypt-comparar) toda la tabla de usuarios.
     */
    java.util.List<UsuarioEntity> findByPinHashIsNotNull();

    /**
     * Candidatos para login por PIN acotados a UNA empresa: usuarios con PIN configurado
     * que son miembros ACTIVOS de esa empresa (via user_company). Evita escanear (y
     * BCrypt-comparar) usuarios con PIN de TODOS los tenants — solo los de la empresa
     * solicitada.
     */
    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN UserCompanyEntity uc ON uc.usuario.id = u.id " +
           "WHERE u.pinHash IS NOT NULL AND uc.company.id = :companyId AND uc.isActive = true")
    java.util.List<UsuarioEntity> findByPinHashIsNotNullAndCompanyId(@Param("companyId") Long companyId);

    /**
     * Candidatos para autorización de supervisor: usuarios con PIN configurado
     * cuyo rol está entre los autorizadores. Reduce el set a un puñado antes del
     * BCrypt-match (los roles se siembran en mayúsculas: ADMIN/GERENTE/SUPERADMIN).
     */
    java.util.List<UsuarioEntity> findByPinHashIsNotNullAndRol_NombreIn(java.util.Collection<String> rolNombres);

    /**
     * Listado de usuarios acotado a UNA empresa (via user_company), paginado.
     * Defensa cross-tenant: un ADMIN normal solo debe ver usuarios de su propia empresa,
     * a diferencia de un SUPERADMIN que usa {@code findAll(Pageable)} sin acotar.
     */
    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN UserCompanyEntity uc ON uc.usuario.id = u.id " +
           "WHERE uc.company.id = :companyId")
    Page<UsuarioEntity> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /** Misma acotacion que {@link #findByCompanyId(Long, Pageable)} pero sin paginar. */
    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN UserCompanyEntity uc ON uc.usuario.id = u.id " +
           "WHERE uc.company.id = :companyId")
    java.util.List<UsuarioEntity> findAllByCompanyId(@Param("companyId") Long companyId);

    /**
     * Ver {@link #findByCompanyId(Long, Pageable)}, con filtros adicionales opcionales de
     * búsqueda por texto (username/email/nombres/apellidos/documento), rol, estado activo,
     * tipo de documento y rango de fecha de creación (para el listado avanzado de usuarios,
     * usado tanto por admin/users como por admin/reportes-clientes).
     * {@code search} y {@code tipoDocumento} usan el patrón "(:x IS NULL OR :x = '' OR ...)"
     * (vara de medir del módulo, ver EmployeeRepository.searchPaged) — Hibernate no evalúa
     * bien el bind de String null en algunos drivers, así que se cubren ambos casos.
     */
    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN UserCompanyEntity uc ON uc.usuario.id = u.id " +
           "WHERE uc.company.id = :companyId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' " +
           "     OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.numeroDocumento) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:rolId AS Long) IS NULL OR u.rol.id = :rolId) " +
           "AND (CAST(:activo AS Boolean) IS NULL OR u.activo = :activo) " +
           "AND (CAST(:tipoDocumento AS String) IS NULL OR CAST(:tipoDocumento AS String) = '' OR u.persona.tipoDocumento = :tipoDocumento) " +
           "AND (CAST(:fechaDesde AS Instant) IS NULL OR u.fechaCreacion >= :fechaDesde) " +
           "AND (CAST(:fechaHasta AS Instant) IS NULL OR u.fechaCreacion <= :fechaHasta)")
    Page<UsuarioEntity> findByCompanyIdFiltered(@Param("companyId") Long companyId,
            @Param("search") String search,
            @Param("rolId") Long rolId,
            @Param("activo") Boolean activo,
            @Param("tipoDocumento") String tipoDocumento,
            @Param("fechaDesde") Instant fechaDesde,
            @Param("fechaHasta") Instant fechaHasta,
            Pageable pageable);

    /** Ver {@link #findByCompanyIdFiltered} pero sin acotar por empresa (uso exclusivo SUPERADMIN). */
    @Query("SELECT u FROM UsuarioEntity u WHERE " +
           "(CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' " +
           "     OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(u.persona.numeroDocumento) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:rolId AS Long) IS NULL OR u.rol.id = :rolId) " +
           "AND (CAST(:activo AS Boolean) IS NULL OR u.activo = :activo) " +
           "AND (CAST(:tipoDocumento AS String) IS NULL OR CAST(:tipoDocumento AS String) = '' OR u.persona.tipoDocumento = :tipoDocumento) " +
           "AND (CAST(:fechaDesde AS Instant) IS NULL OR u.fechaCreacion >= :fechaDesde) " +
           "AND (CAST(:fechaHasta AS Instant) IS NULL OR u.fechaCreacion <= :fechaHasta)")
    Page<UsuarioEntity> findAllFiltered(@Param("search") String search,
            @Param("rolId") Long rolId,
            @Param("activo") Boolean activo,
            @Param("tipoDocumento") String tipoDocumento,
            @Param("fechaDesde") Instant fechaDesde,
            @Param("fechaHasta") Instant fechaHasta,
            Pageable pageable);
}
