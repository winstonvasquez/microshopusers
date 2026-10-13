package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.dto.UserRequestDto;
import com.microshop.users.application.mapper.UserMapper;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.config.SecurityProperties;
import com.microshop.users.config.security.JwtService;
import com.microshop.users.domain.service.EmailService;
import com.microshop.users.domain.service.OtpService;
import com.microshop.users.infrastructure.persistence.entity.RolEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** R2P-5 (supervisor inactivo) y R2P-7 (username case-insensitive en alta/edición). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsernameYSupervisorTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private RolRepository rolRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageHelper msg;
    @Mock private UserCompanyCommandService userCompanyCommandService;
    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private SesionRevocacionService sesionRevocacionService;
    @InjectMocks private UserCommandService userService;

    @Mock private SesionRepository sesionRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private OtpService otpService;
    @Mock private SecurityProperties securityProps;
    @Mock private SaasQueryService saasQueryService;

    private static UserRequestDto dto(String username) {
        return new UserRequestDto(username, "a@b.pe", "secret1", 1L, null, "N", "A", "DNI", "12345678",
                LocalDate.of(1990, 1, 1));
    }

    @Test
    void altaRechazaUsernameQueSoloDifiereEnMayusculas() {
        when(usuarioRepository.existsByUsernameIgnoreCase("Juan")).thenReturn(true);
        when(msg.get(any(), any())).thenReturn("dup");
        assertThatThrownBy(() -> userService.createUser(dto("Juan"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void edicionRechazaUsernameDeOtroUsuarioIgnorandoMayusculas() {
        var actual = UsuarioEntity.builder().username("maria").email("a@b.pe").build();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(actual));
        when(usuarioRepository.existsByUsernameIgnoreCase("JUAN")).thenReturn(true);
        when(msg.get(any(), any())).thenReturn("dup");
        assertThatThrownBy(() -> userService.updateUser(2L, dto("JUAN"))).isInstanceOf(IllegalArgumentException.class);
    }

    private AuthCommandService auth() {
        return new AuthCommandService(usuarioRepository, sesionRepository, userCompanyRepository,
                authenticationManager, jwtService, passwordEncoder, msg, emailService, otpService,
                securityProps, saasQueryService);
    }

    private UsuarioEntity supervisor(boolean activo) {
        var u = UsuarioEntity.builder().id(9L).username("sup").pinHash("hash")
                .rol(RolEntity.builder().nombre("ADMIN").build()).build();
        u.setActivo(activo);
        return u;
    }

    @Test
    void supervisorDesactivadoNoAutoriza() {
        when(usuarioRepository.findByPinHashIsNotNullAndRol_NombreIn(any())).thenReturn(List.of(supervisor(false)));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThat(auth().verifySupervisorPin("1234", null).authorized()).isFalse();
    }

    @Test
    void supervisorActivoAutoriza() {
        when(usuarioRepository.findByPinHashIsNotNullAndRol_NombreIn(any())).thenReturn(List.of(supervisor(true)));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThat(auth().verifySupervisorPin("1234", null).authorized()).isTrue();
    }
}
