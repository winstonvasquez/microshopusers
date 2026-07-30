package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SesionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<SesionEntity, Long> {

    Optional<SesionEntity> findByToken(String token);

    /**
     * Lookup por el {@code jti} del JWT (M27). Es el identificador de revocación: 36 caracteres, único
     * por token y con índice propio — frente a {@link #findByToken(String)}, que compara por igualdad
     * una cadena de ~800 caracteres en cada consulta.
     */
    Optional<SesionEntity> findByJti(String jti);

    /**
     * Invalida de golpe todas las sesiones vivas de una empresa.
     *
     * <p>{@code @Modifying @Query} y no un {@code findBy...} más un bucle de {@code save}: son
     * potencialmente cientos de filas y no hace falta traerlas a memoria para girar un booleano. Ojo
     * también con la trampa inversa que este proyecto ya documentó — un {@code deleteByXxx} derivado
     * NO es un DELETE inmediato, carga y marca entidades—; aquí se escribe el UPDATE explícito.</p>
     *
     * @return número de filas afectadas
     */
    @Modifying
    @Query("""
           update SesionEntity s
              set s.valido = false
            where s.companyId = :companyId
              and s.valido = true
           """)
    int invalidarPorCompany(@Param("companyId") Long companyId);

    /** Ver {@link #invalidarPorCompany(Long)}, por usuario. Logout global y reseteo de contraseña. */
    @Modifying
    @Query("""
           update SesionEntity s
              set s.valido = false
            where s.usuario.id = :usuarioId
              and s.valido = true
           """)
    int invalidarPorUsuario(@Param("usuarioId") Long usuarioId);
}
