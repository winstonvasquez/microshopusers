package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<Document> findByIdAndTenantId(Long id, Long tenantId);

    List<Document> findByTenantIdAndEmployeeIdAndTipoDocumento(Long tenantId, Long employeeId, Document.DocumentType tipoDocumento);

    long countByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
}
