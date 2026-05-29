package com.microshop.users.application.mapper;

import com.microshop.users.application.dto.CompanyRequestDto;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del mapeo CompanyRequestDto → CompanyEntity.
 *
 * Cubre el fix 2026-05-28: `active` pasó de `boolean` primitivo a `Boolean` para que omitirlo
 * en el JSON no causara HTTP 500; el mapper debe defaultearlo a true.
 */
class CompanyMapperTest {

    private final CompanyMapper mapper = new CompanyMapper();

    private CompanyRequestDto dto(Boolean active) {
        return new CompanyRequestDto("Empresa X", "20600000099", active,
                "Empresa X SAC", "Av Test", "999", "x@test.pe", "", "x");
    }

    @Test
    @DisplayName("active=null → entidad activa por defecto (fix onboarding 500)")
    void activeNullDefaultsToTrue() {
        CompanyEntity entity = mapper.toEntity(dto(null));
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("active=false se respeta")
    void activeFalseIsRespected() {
        CompanyEntity entity = mapper.toEntity(dto(false));
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    @DisplayName("active=true se respeta")
    void activeTrueIsRespected() {
        CompanyEntity entity = mapper.toEntity(dto(true));
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("campos string se mapean")
    void mapsStringFields() {
        CompanyEntity entity = mapper.toEntity(dto(true));
        assertThat(entity.getName()).isEqualTo("Empresa X");
        assertThat(entity.getRuc()).isEqualTo("20600000099");
        assertThat(entity.getEmail()).isEqualTo("x@test.pe");
    }
}
