package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasPlanModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaasPlanModuleRepository extends JpaRepository<SaasPlanModuleEntity, Long> {
    List<SaasPlanModuleEntity> findByPlanId(Long planId);

    /**
     * Borra los módulos del plan como DML inmediato, NO como delete derivado.
     *
     * <p>Con la versión derivada (<code>void deleteByPlanId(Long)</code>) Spring Data cargaba las
     * filas y las marcaba para borrado en el contexto de persistencia; Hibernate ordena las
     * acciones por tipo y ejecuta los INSERT ANTES de los DELETE, así que al reemplazar el set de
     * módulos de un plan los nuevos choraban con los viejos todavía presentes y el PUT del plan
     * respondía 500 con <code>uk_plan_module</code> violado (falla en TODA edición que conserve
     * algún módulo). Como DML explícito el DELETE se ejecuta al invocarlo, antes de los INSERT.</p>
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SaasPlanModuleEntity pm where pm.plan.id = :planId")
    void deleteByPlanId(@Param("planId") Long planId);
}
