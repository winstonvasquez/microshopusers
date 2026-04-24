package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.infrastructure.persistence.entity.CreditAccountEntity;
import com.microshop.users.infrastructure.persistence.repository.CreditAccountRepository;
import com.microshop.users.infrastructure.persistence.repository.CreditTransactionRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Saldo de crédito del cliente autenticado.
 */
@RestController
@RequestMapping("/api/users/me/credit")
@RequiredArgsConstructor
@Tag(name = "Crédito Cliente", description = "Consulta del saldo de crédito del cliente autenticado")
public class ClienteCreditController {

    private final CreditAccountRepository creditRepo;
    private final CreditTransactionRepository txRepo;
    private final UsuarioRepository usuarioRepo;

    // ── DTOs ─────────────────────────────────────────────────────────────

    public record TransactionResponse(
            Long id,
            String type,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description,
            Instant createdAt
    ) {}

    public record CreditBalanceResponse(
            BigDecimal balance,
            String currency,
            List<TransactionResponse> recentTransactions
    ) {}

    // ── Helper ────────────────────────────────────────────────────────────

    private Long resolveUserId(String username) {
        return usuarioRepo.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new NotFoundException("usuario", username));
    }

    private CreditAccountEntity getOrCreateAccount(Long userId) {
        return creditRepo.findByClienteId(userId).orElseGet(() -> {
            CreditAccountEntity account = CreditAccountEntity.builder()
                    .clienteId(userId)
                    .build();
            return creditRepo.save(account);
        });
    }

    // ── Endpoints ─────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Consultar saldo de crédito con últimos 5 movimientos")
    public ResponseEntity<CreditBalanceResponse> getCredit(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolveUserId(userDetails.getUsername());
        CreditAccountEntity account = getOrCreateAccount(userId);

        Page<TransactionResponse> txPage = txRepo
                .findByCreditAccount_ClienteIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .map(t -> new TransactionResponse(
                        t.getId(), t.getType(), t.getAmount(),
                        t.getBalanceBefore(), t.getBalanceAfter(),
                        t.getDescription(), t.getCreatedAt()));

        return ResponseEntity.ok(new CreditBalanceResponse(
                account.getBalance(), account.getCurrency(), txPage.getContent()));
    }

    @GetMapping("/history")
    @Operation(summary = "Historial paginado de movimientos de crédito")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = resolveUserId(userDetails.getUsername());
        Page<TransactionResponse> result = txRepo
                .findByCreditAccount_ClienteIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(t -> new TransactionResponse(
                        t.getId(), t.getType(), t.getAmount(),
                        t.getBalanceBefore(), t.getBalanceAfter(),
                        t.getDescription(), t.getCreatedAt()));
        return ResponseEntity.ok(result);
    }
}
