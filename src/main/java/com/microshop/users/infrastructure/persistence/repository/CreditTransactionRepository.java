package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.CreditTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransactionEntity, Long> {

    Page<CreditTransactionEntity> findByCreditAccount_ClienteIdOrderByCreatedAtDesc(
            Long clienteId, Pageable pageable);

    /**
     * Historial paginado con filtros avanzados: tipo de movimiento y rango de fecha del
     * movimiento — reemplaza al historial sin filtros de /me/credit/history.
     */
    @Query("SELECT t FROM CreditTransactionEntity t WHERE t.creditAccount.clienteId = :clienteId " +
           "AND (CAST(:type AS String) IS NULL OR CAST(:type AS String) = '' OR t.type = :type) " +
           "AND (CAST(:desde AS Instant) IS NULL OR t.createdAt >= :desde) " +
           "AND (CAST(:hasta AS Instant) IS NULL OR t.createdAt <= :hasta)")
    Page<CreditTransactionEntity> searchPaged(@Param("clienteId") Long clienteId,
                                              @Param("type") String type,
                                              @Param("desde") Instant desde,
                                              @Param("hasta") Instant hasta,
                                              Pageable pageable);
}
