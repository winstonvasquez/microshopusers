package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de {@code PATCH /users/api/companies/{id}/estado}.
 *
 * <p>Un único campo a propósito. La alternativa era reutilizar {@code CompanyRequestDto}, y eso es
 * exactamente lo que había que evitar: {@code CompanyCommandService.updateCompany} sobrescribe ocho
 * campos de la empresa, así que suspenderla con el DTO completo borraba cualquier campo que el
 * llamador no reenviara.</p>
 *
 * @param activa {@code false} suspende la empresa, {@code true} la reactiva. Es {@code Boolean} y no
 *               {@code boolean} para que un cuerpo sin el campo falle con un 400 explícito en vez de
 *               tomar {@code false} por omisión y suspender la empresa por accidente.
 */
public record CambiarEstadoEmpresaRequest(
        @NotNull(message = "Hay que indicar si la empresa queda activa o suspendida")
        Boolean activa) {
}
