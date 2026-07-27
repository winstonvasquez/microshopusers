package com.microshop.users.application.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microshop.users.infrastructure.persistence.entity.LandingContentSectionEntity;
import com.microshop.users.infrastructure.persistence.repository.LandingContentSectionRepository;
import com.microshop.users.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Edita UNA sección de contenido de /portal/landing — restringido a ADMIN/SUPERADMIN (ver SecurityConfig). */
@Service
@RequiredArgsConstructor
@Slf4j
public class LandingContentCommandService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final LandingContentSectionRepository repository;

    @Transactional
    public void updateSection(String sectionKey, Object content) {
        log.info("Updating landing content section: {}", sectionKey);

        String json;
        try {
            json = objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new BusinessException("Contenido inválido para la sección " + sectionKey + ": " + e.getMessage());
        }

        var section = repository.findBySectionKey(sectionKey)
                .orElseGet(() -> LandingContentSectionEntity.builder().sectionKey(sectionKey).build());
        section.setContentJson(json);
        repository.save(section);

        log.info("Landing content section updated: {}", sectionKey);
    }
}
