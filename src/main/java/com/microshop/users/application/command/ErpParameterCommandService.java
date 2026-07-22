package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comandos de escritura sobre parámetros ERP editables por empresa.
 * Las lecturas están en ErpParameterQueryService.
 */
@Service
@RequiredArgsConstructor
public class ErpParameterCommandService {

    private final ErpParameterJpaRepository repository;

    /** Resultado de la actualización, para que el controller mapee el código HTTP correspondiente. */
    public enum UpdateResult { NOT_FOUND, FORBIDDEN, OK }

    @Transactional
    public UpdateResult updateParameter(String key, String value) {
        var paramOpt = repository.findByParamKeyAndTenantIdIsNull(key);
        if (paramOpt.isEmpty()) {
            return UpdateResult.NOT_FOUND;
        }
        var param = paramOpt.get();
        if (!Boolean.TRUE.equals(param.getEditable())) {
            return UpdateResult.FORBIDDEN;
        }
        param.setParamValue(value.trim().replace("\"", ""));
        repository.save(param);
        return UpdateResult.OK;
    }
}
