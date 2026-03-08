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
}
