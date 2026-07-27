package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SegmentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SegmentoRepository extends JpaRepository<SegmentoEntity, Long> {

    boolean existsByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrueAndIdNot(String nombre, Long id);

    // Filtros avanzados (activo/tipoCliente/rango fechaCreacion) + búsqueda por texto server-side.
    // 'activo' ya NO se hardcodea a true: si el filtro viene null se listan activos e inactivos.
    @Query("""
            SELECT s FROM SegmentoEntity s
            WHERE (:activo IS NULL OR s.activo = :activo)
              AND (:tipoCliente = '' OR s.tipoCliente = :tipoCliente)
              AND (:search = ''
                   OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.tipoCliente) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:fechaDesde IS NULL OR s.fechaCreacion >= :fechaDesde)
              AND (:fechaHasta IS NULL OR s.fechaCreacion <= :fechaHasta)
            """)
    Page<SegmentoEntity> findAllActiveWithSearch(
            @Param("search") String search,
            @Param("activo") Boolean activo,
            @Param("tipoCliente") String tipoCliente,
            @Param("fechaDesde") Instant fechaDesde,
            @Param("fechaHasta") Instant fechaHasta,
            Pageable pageable);
}
