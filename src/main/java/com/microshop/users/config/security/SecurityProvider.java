package com.microshop.users.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityProvider {

    /**
     * Obtiene el nombre/ID del usuario actual de forma segura.
     * 
     * @return Usuario autenticado o "ANONYMOUS"
     */
    public String getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("ANONYMOUS");
    }
}
