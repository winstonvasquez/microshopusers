package com.microshop.users.application.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microshop.users.infrastructure.persistence.repository.LandingContentSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/** Lee el contenido editable de /portal/landing (endpoint público, consumido por landing-page.component.ts). */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LandingContentQueryService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final LandingContentSectionRepository repository;

    public Map<String, Object> getAllSections() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (var section : repository.findAllByActivoTrue()) {
            try {
                result.put(section.getSectionKey(), objectMapper.readValue(section.getContentJson(), Object.class));
            } catch (Exception e) {
                log.warn("No se pudo parsear content_json de la sección {}: {}", section.getSectionKey(), e.getMessage());
            }
        }
        return result;
    }
}
