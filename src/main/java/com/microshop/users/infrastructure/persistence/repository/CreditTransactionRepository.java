package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.CreditTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransactionEntity, Long> {

    Page<CreditTransactionEntity> findByCreditAccount_ClienteIdOrderByCreatedAtDesc(
            Long clienteId, Pageable pageable);
}
