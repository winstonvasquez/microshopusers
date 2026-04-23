package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SegmentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentoRepository extends JpaRepository<SegmentoEntity, Long> {

    boolean existsByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrueAndIdNot(String nombre, Long id);

    @Query("""
            SELECT s FROM SegmentoEntity s
            WHERE s.activo = true
              AND (:search = ''
                   OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.tipoCliente) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<SegmentoEntity> findAllActiveWithSearch(
            @Param("search") String search,
            Pageable pageable);
}
