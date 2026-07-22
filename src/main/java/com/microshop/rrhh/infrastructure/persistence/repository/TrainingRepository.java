package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByTenantId(Long tenantId);

    Optional<Training> findByIdAndTenantId(Long id, Long tenantId);

    List<Training> findByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);

    long countByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);

    // Analytics dashboard (2026-07-22): SUM agregado en SQL, COALESCE evita null si no hay filas.
    @Query("SELECT COALESCE(SUM(t.duracionHoras), 0) FROM Training t WHERE t.tenantId = :tenantId AND t.estado = :estado")
    long sumDuracionHorasByTenantIdAndEstado(@Param("tenantId") Long tenantId, @Param("estado") Training.TrainingStatus estado);
}
