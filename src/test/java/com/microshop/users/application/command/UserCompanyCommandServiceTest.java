package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.*;
import com.microshop.users.infrastructure.persistence.repository.*;
import com.microshop.users.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cubre el cupo de usuarios por plan SaaS agregado en addUserToCompany: antes de este fix,
 * SaasPlanEntity.maxUsers no se validaba en ningún lado (gap detectado en auditoría 2026-07-26
 * del landing/pricing — el copy prometía "hasta N usuarios" sin enforcement real).
 */
@ExtendWith(MockitoExtension.class)
class UserCompanyCommandServiceTest {

    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private UserCompanyRoleRepository userCompanyRoleRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private RolRepository rolRepository;
    @Mock private SaasSubscriptionRepository subscriptionRepository;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private UserCompanyCommandService service;

    private static final Long USER_ID = 1L;
    private static final Long COMPANY_ID = 10L;
    private static final Long ROLE_ID = 100L;

    private UsuarioEntity user;
    private CompanyEntity company;
    private RolEntity role;

    @BeforeEach
    void setUp() {
        user = UsuarioEntity.builder().id(USER_ID).build();
        company = CompanyEntity.builder().id(COMPANY_ID).build();
        role = RolEntity.builder().id(ROLE_ID).build();

        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(companyRepository.findByIdForUpdate(COMPANY_ID)).thenReturn(Optional.of(company));
        when(rolRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
    }

    private SaasSubscriptionEntity subscriptionWithMaxUsers(int maxUsers) {
        SaasPlanEntity plan = SaasPlanEntity.builder().code("PROFESSIONAL").name("Professional").maxUsers(maxUsers).build();
        return SaasSubscriptionEntity.builder().plan(plan).build();
    }

    @Test
    @DisplayName("Alta de usuario nuevo bajo el cupo del plan: se crea la membresía")
    void addsNewUserWhenUnderQuota() {
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(USER_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.of(subscriptionWithMaxUsers(5)));
        when(userCompanyRepository.countByCompanyIdAndIsActiveTrue(COMPANY_ID)).thenReturn(2L);
        when(userCompanyRepository.save(any(UserCompanyEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addUserToCompany(USER_ID, COMPANY_ID, ROLE_ID);

        verify(userCompanyRepository).save(any(UserCompanyEntity.class));
        verify(userCompanyRoleRepository).save(any(UserCompanyRoleEntity.class));
    }

    @Test
    @DisplayName("Alta de usuario nuevo en el límite del plan: rechaza con ConflictException")
    void blocksNewUserAtQuota() {
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(USER_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.of(subscriptionWithMaxUsers(5)));
        when(userCompanyRepository.countByCompanyIdAndIsActiveTrue(COMPANY_ID)).thenReturn(5L);

        assertThatThrownBy(() -> service.addUserToCompany(USER_ID, COMPANY_ID, ROLE_ID))
                .isInstanceOf(ConflictException.class);

        verify(userCompanyRepository, never()).save(any(UserCompanyEntity.class));
    }

    @Test
    @DisplayName("Sin suscripción activa: no se restringe el alta (fallback permisivo)")
    void allowsWhenNoSubscription() {
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(USER_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.empty());
        when(userCompanyRepository.save(any(UserCompanyEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addUserToCompany(USER_ID, COMPANY_ID, ROLE_ID);

        verify(userCompanyRepository).save(any(UserCompanyEntity.class));
        verify(userCompanyRepository, never()).countByCompanyIdAndIsActiveTrue(any());
    }

    @Test
    @DisplayName("Reactivar membresía inactiva también valida el cupo")
    void reactivatingInactiveMembershipChecksQuota() {
        UserCompanyEntity inactive = UserCompanyEntity.builder()
                .id(50L).usuario(user).company(company).isActive(false).build();
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(USER_ID, COMPANY_ID)).thenReturn(Optional.of(inactive));
        when(subscriptionRepository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.of(subscriptionWithMaxUsers(5)));
        when(userCompanyRepository.countByCompanyIdAndIsActiveTrue(COMPANY_ID)).thenReturn(5L);

        assertThatThrownBy(() -> service.addUserToCompany(USER_ID, COMPANY_ID, ROLE_ID))
                .isInstanceOf(ConflictException.class);

        assertThat(inactive.isActive()).isFalse();
        verify(userCompanyRepository, never()).save(any(UserCompanyEntity.class));
    }

    @Test
    @DisplayName("Agregar un rol a una membresía ya activa no consume cupo")
    void addingRoleToActiveMembershipDoesNotCheckQuota() {
        UserCompanyEntity active = UserCompanyEntity.builder()
                .id(51L).usuario(user).company(company).isActive(true).build();
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(USER_ID, COMPANY_ID)).thenReturn(Optional.of(active));

        service.addUserToCompany(USER_ID, COMPANY_ID, ROLE_ID);

        verify(subscriptionRepository, never()).findByCompanyId(any());
        verify(userCompanyRoleRepository).save(any(UserCompanyRoleEntity.class));
    }
}
