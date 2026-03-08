package com.microshop.users.config;

import com.microshop.users.infrastructure.persistence.entity.*;
import com.microshop.users.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InitialUserConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserCompanyRoleRepository userCompanyRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAdminUser();
    }

    private void seedAdminUser() {
        log.info("Seeding admin user and default company...");

        var adminRole = getOrCreateAdminRole();
        var adminPersona = getOrCreateAdminPersona();
        var defaultCompany = getOrCreateDefaultCompany();
        var adminUser = getOrCreateAdminUser(adminRole, adminPersona);
        var userCompany = linkUserToCompany(adminUser, defaultCompany);
        assignRoleToUserCompany(userCompany, adminRole);

        log.info("Admin user, default company, and associations created successfully.");
    }

    private RolEntity getOrCreateAdminRole() {
        return rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(RolEntity.builder()
                        .nombre("ADMIN")
                        .descripcion("Administrator with full access")
                        .build()));
    }

    private PersonaEntity getOrCreateAdminPersona() {
        return personaRepository.findByNumeroDocumento("00000000")
                .orElseGet(() -> personaRepository.save(PersonaEntity.builder()
                        .nombres("Admin")
                        .apellidos("System")
                        .tipoDocumento("DNI")
                        .numeroDocumento("00000000")
                        .fechaNacimiento(LocalDate.of(2000, 1, 1))
                        .build()));
    }

    private CompanyEntity getOrCreateDefaultCompany() {
        return companyRepository.findByRuc("20000000001")
                .map(this::activateCompanyIfInactive)
                .orElseGet(() -> companyRepository.save(CompanyEntity.builder()
                        .name("Microshop Default Company")
                        .ruc("20000000001")
                        .isActive(true)
                        .build()));
    }

    private CompanyEntity activateCompanyIfInactive(CompanyEntity company) {
        if (!company.isActive()) {
            company.setActive(true);
            return companyRepository.save(company);
        }
        return company;
    }

    private UsuarioEntity getOrCreateAdminUser(RolEntity role, PersonaEntity persona) {
        return usuarioRepository.findByUsername("admin")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("12345678"))
                        .email("admin@microshop.com")
                        .rol(role)
                        .persona(persona)
                        .build()));
    }

    private UserCompanyEntity linkUserToCompany(UsuarioEntity user, CompanyEntity company) {
        return userCompanyRepository
                .findByUsuarioIdAndCompanyId(user.getId(), company.getId())
                .orElseGet(() -> userCompanyRepository.save(UserCompanyEntity.builder()
                        .usuario(user)
                        .company(company)
                        .isActive(true)
                        .build()));
    }

    private void assignRoleToUserCompany(UserCompanyEntity userCompany, RolEntity role) {
        if (userCompanyRoleRepository.findByUserCompanyId(userCompany.getId()).isEmpty()) {
            userCompanyRoleRepository.save(UserCompanyRoleEntity.builder()
                    .userCompany(userCompany)
                    .rol(role)
                    .build());
        }
    }
}
