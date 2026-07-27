package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para editar UNA sección de contenido de /portal/landing (PROBLEM_POINTS, HOW_IT_WORKS,
 * TRUST_POINTS, FAQ). {@code content} es JSON arbitrario (array de strings u objetos, según la
 * sección) — se re-serializa tal cual para guardar en landing_content_section.content_json.
 */
public record LandingSectionUpdateRequest(
        @NotBlank(message = "sectionKey es obligatorio")
        String sectionKey,

        @NotNull(message = "content es obligatorio")
        Object content
) {}
