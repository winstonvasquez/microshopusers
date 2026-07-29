package com.microshop.users.application.query;

import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserCompanyQueryService {

    private final UserCompanyRepository userCompanyRepository;

    public List<UserCompanyEntity> getUserCompanies(@NonNull Long userId) {
        return userCompanyRepository.findByUsuarioId(userId);
    }

    /**
     * ¿El usuario indicado pertenece al tenant indicado? Es la pregunta que los endpoints por
     * {@code {id}} de usuario tienen que hacerse antes de responder: como {@code UsuarioEntity} no
     * lleva {@code company_id}, sin esta comprobación un ADMIN de cualquier empresa podía leer,
     * modificar o borrar usuarios de otra con solo cambiar el id de la URL.
     */
    public boolean perteneceAlTenant(@NonNull Long usuarioId, @NonNull Long companyId) {
        return userCompanyRepository.existsByUsuarioIdAndCompanyId(usuarioId, companyId);
    }
}
