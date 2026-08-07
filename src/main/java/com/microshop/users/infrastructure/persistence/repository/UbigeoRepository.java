package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.application.dto.UbigeoOptionDto;
import com.microshop.users.infrastructure.persistence.entity.UbigeoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso al ubigeo INEI. Las tres consultas de nivel devuelven directamente
 * {@link UbigeoOptionDto} por proyección: de las 1874 filas solo interesan el código y el nombre,
 * y traer entidades completas para llenar un dropdown sería malgastar el viaje.
 *
 * <p>Sin acotación por tenant, y es intencional: la división política del Perú es la misma para
 * todas las empresas (ver {@link UbigeoEntity}).
 *
 * <p>Ordenado por nombre y no por código porque es lo que el usuario busca con la vista en un
 * select. La única excepción visible es el Callao, que el INEI nombra «Prov. Const. del Callao».
 */
@Repository
public interface UbigeoRepository extends JpaRepository<UbigeoEntity, String> {

    @Query("""
            SELECT DISTINCT new com.microshop.users.application.dto.UbigeoOptionDto(
                       u.departamentoCodigo, u.departamento)
            FROM UbigeoEntity u
            ORDER BY u.departamento
            """)
    List<UbigeoOptionDto> findDepartamentos();

    @Query("""
            SELECT DISTINCT new com.microshop.users.application.dto.UbigeoOptionDto(
                       u.provinciaCodigo, u.provincia)
            FROM UbigeoEntity u
            WHERE u.departamentoCodigo = :departamento
            ORDER BY u.provincia
            """)
    List<UbigeoOptionDto> findProvincias(@Param("departamento") String departamentoCodigo);

    @Query("""
            SELECT new com.microshop.users.application.dto.UbigeoOptionDto(u.codigo, u.distrito)
            FROM UbigeoEntity u
            WHERE u.provinciaCodigo = :provincia
            ORDER BY u.distrito
            """)
    List<UbigeoOptionDto> findDistritos(@Param("provincia") String provinciaCodigo);
}
