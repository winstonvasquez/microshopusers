package com.microshop.users.application.query;


import com.microshop.users.application.dto.CheckEmailResponse;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthQueryService {

    private final UsuarioRepository usuarioRepository;

    public CheckEmailResponse checkEmail(String email) {
        boolean exists = usuarioRepository.existsByEmail(email.toLowerCase());
        String masked = maskEmail(email);
        return new CheckEmailResponse(exists, masked);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2)
            return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String masked = local.charAt(0) + "***" + local.charAt(local.length() - 1);
        return masked + domain;
    }
}
