package com.microshop.users.application.query;

import com.microshop.users.application.dto.UbigeoOptionDto;
import com.microshop.users.infrastructure.persistence.repository.UbigeoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consultas del ubigeo INEI para los selects encadenados departamento → provincia → distrito.
 *
 * <p>Todo va cacheado porque la tabla es inmutable: la siembra la migración V42 y no hay ningún
 * comando que la escriba. Sin caché, cada apertura de un formulario de dirección dispararía tres
 * consultas con {@code DISTINCT} sobre las 1874 filas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UbigeoQueryService {

    private final UbigeoRepository ubigeoRepository;

    /** Los 25 departamentos. */
    @Cacheable(value = "ubigeo", key = "'departamentos'")
    public List<UbigeoOptionDto> getDepartamentos() {
        return ubigeoRepository.findDepartamentos();
    }

    /**
     * Provincias de un departamento. Devuelve lista vacía si el código no existe: un select sin
     * opciones es la respuesta correcta a un departamento inválido, no un error.
     */
    @Cacheable(value = "ubigeo", key = "'provincias:' + #departamentoCodigo")
    public List<UbigeoOptionDto> getProvincias(String departamentoCodigo) {
        return ubigeoRepository.findProvincias(departamentoCodigo);
    }

    /** Distritos de una provincia. El código devuelto es el ubigeo de 6 dígitos. */
    @Cacheable(value = "ubigeo", key = "'distritos:' + #provinciaCodigo")
    public List<UbigeoOptionDto> getDistritos(String provinciaCodigo) {
        return ubigeoRepository.findDistritos(provinciaCodigo);
    }

}
