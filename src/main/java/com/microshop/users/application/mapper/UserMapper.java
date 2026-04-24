package com.microshop.users.application.mapper;

import com.microshop.users.application.dto.UserRequestDto;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.infrastructure.persistence.entity.PersonaEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class UserMapper {

    public UserResponseDto toDto(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        var persona = usuario.getPersona();
        var nombreCompleto = persona != null
                ? persona.getNombres() + " " + persona.getApellidos()
                : "";

        return new UserResponseDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                new UserResponseDto.RolDto(
                        usuario.getRol().getId(),
                        usuario.getRol().getNombre(),
                        usuario.getRol().getDescripcion()),
                new UserResponseDto.PersonaDto(
                        persona.getId(),
                        persona.getNombres(),
                        persona.getApellidos(),
                        nombreCompleto,
                        persona.getTipoDocumento(),
                        persona.getNumeroDocumento(),
                        persona.getFechaNacimiento()),
                mapToLocalDateTime(usuario.getFechaCreacion()),
                mapToLocalDateTime(usuario.getFechaModificacion()));
    }

    public PersonaEntity toPersonaEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return PersonaEntity.builder()
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .tipoDocumento(dto.tipoDocumento())
                .numeroDocumento(dto.numeroDocumento())
                .fechaNacimiento(dto.fechaNacimiento())
                .build();
    }

    public void updatePersonaFromDto(UserRequestDto dto, PersonaEntity persona) {
        if (dto == null || persona == null) {
            return;
        }

        persona.setNombres(dto.nombres());
        persona.setApellidos(dto.apellidos());
        persona.setTipoDocumento(dto.tipoDocumento());
        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setFechaNacimiento(dto.fechaNacimiento());
    }

    private LocalDateTime mapToLocalDateTime(Instant instant) {
        return instant != null
                ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                : null;
    }
}
