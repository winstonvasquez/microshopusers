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
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        when(usuarioRepository.findByPinHashIsNotNullAndCompanyId(1L)).thenReturn(List.of(supervisor(false)));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThat(auth().verifySupervisorPin("1234", 1L).authorized()).isFalse();
    }

    @Test
    void supervisorActivoAutoriza() {
        when(usuarioRepository.findByPinHashIsNotNullAndCompanyId(1L)).thenReturn(List.of(supervisor(true)));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThat(auth().verifySupervisorPin("1234", 1L).authorized()).isTrue();
    }

    @Test
    void supervisorSinEmpresaOConRolDeCajeroNoAutoriza() {
        var cajero = supervisor(true);
        cajero.setRol(RolEntity.builder().nombre("VENDEDOR").build());
        when(usuarioRepository.findByPinHashIsNotNullAndCompanyId(1L)).thenReturn(List.of(cajero));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThat(auth().verifySupervisorPin("1234", 1L).authorized()).isFalse();
        assertThat(auth().verifySupervisorPin("1234", null).authorized()).isFalse();
    }

    private static void sesionDeEmpresa(Long companyId) {
        var a = new UsernamePasswordAuthenticationToken("caja", null, List.of());
        a.setDetails(companyId == null ? Map.of() : Map.of("companyId", companyId, "userId", 9L));
        SecurityContextHolder.getContext().setAuthentication(a);
    }

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pinLoginExigeSesionYNoEmiteToken() {
        sesionDeEmpresa(null);
        assertThatThrownBy(() -> auth().pinLogin("1234")).isInstanceOf(AccessDeniedException.class);

        sesionDeEmpresa(1L);
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(supervisor(true)));
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        var r = auth().pinLogin("1234");
        assertThat(r.username()).isEqualTo("sup");
        assertThat(r.activeCompanyId()).isEqualTo(1L);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void pinLoginCortaTrasDiezFallosAunqueLuegoAcierte() {
        sesionDeEmpresa(1L);
        // Sólo se compara con el PIN del usuario de la sesión (9): nunca se busca entre los compañeros.
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(supervisor(true)));
        when(msg.get(any())).thenReturn("credenciales");
        var service = auth();
        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> service.pinLogin("0000")).isInstanceOf(IllegalArgumentException.class);
        }
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        assertThatThrownBy(() -> service.pinLogin("1234"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(429));
    }

    @Test
    void generarPinExigeContrasenaYEvitaElDeUnCompanero() {
        var yo = UsuarioEntity.builder().id(5L).username("yo").password("pwd-hash").build();
        when(usuarioRepository.findByUsername("yo")).thenReturn(Optional.of(yo));
        assertThatThrownBy(() -> userService.generarPinPos("yo", "mala")).isInstanceOf(BusinessException.class);

        when(passwordEncoder.matches("buena", "pwd-hash")).thenReturn(true);
        var membresia = UserCompanyEntity.builder().company(CompanyEntity.builder().id(1L).build()).isActive(true).build();
        when(userCompanyRepository.findByUsuarioId(5L)).thenReturn(List.of(membresia));
        when(usuarioRepository.findByPinHashIsNotNullAndCompanyId(1L)).thenReturn(List.of(supervisor(true)));
        // El primer PIN candidato choca con el del compañero; el segundo queda libre.
        when(passwordEncoder.matches(anyString(), eq("hash"))).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("nuevo-hash");

        String pin = userService.generarPinPos("yo", "buena");
        assertThat(pin).matches("[0-9]{6}");
        assertThat(yo.getPinHash()).isEqualTo("nuevo-hash");
    }
}
