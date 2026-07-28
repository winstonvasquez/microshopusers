package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaasPlanRepository extends JpaRepository<SaasPlanEntity, Long> {
    Optional<SaasPlanEntity> findByCode(String code);

    @Query("SELECT m.code FROM SaasPlanModuleEntity pm JOIN pm.module m " +
           "WHERE pm.plan.id = :planId AND m.isActive = true ORDER BY m.sortOrder")
    List<String> findModuleCodesByPlanId(@Param("planId") Long planId);

    /**
     * Listado admin con filtros opcionales: búsqueda por texto (código/nombre/descripción),
     * estado activo/inactivo y módulo incluido. Catálogo maestro de bajo volumen (~4 planes)
     * -> no justifica paginación server-side, solo List (ver ficha de auditoría de filtros).
     */
    @Query("SELECT DISTINCT p FROM SaasPlanEntity p " +
           "LEFT JOIN SaasPlanModuleEntity pm ON pm.plan = p " +
           "LEFT JOIN pm.module m " +
           "WHERE (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' " +
           "       OR LOWER(p.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "       OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "       OR LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:isActive AS Boolean) IS NULL OR p.isActive = :isActive) " +
           "AND (CAST(:moduleCode AS String) IS NULL OR CAST(:moduleCode AS String) = '' OR m.code = :moduleCode) " +
           "ORDER BY p.id")
    List<SaasPlanEntity> searchPlans(@Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("moduleCode") String moduleCode);
}
