package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.mapper.UserMapper;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ADM-5: eliminar usuario es baja lógica de la membresía (y del usuario si no le queda ninguna). */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceBajaLogicaTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private RolRepository rolRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageHelper msg;
    @Mock private UserCompanyCommandService userCompanyCommandService;
    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private SesionRevocacionService sesionRevocacionService;
    @InjectMocks private UserCommandService service;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteDesactivaMembresiaYUsuarioSiNoQuedaNingunaActiva() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of());
        auth.setDetails(Map.of("companyId", 7L));
        SecurityContextHolder.getContext().setAuthentication(auth);

        var usuario = new UsuarioEntity();
        var membresia = UserCompanyEntity.builder().build();
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(userCompanyRepository.findByUsuarioIdAndCompanyId(5L, 7L)).thenReturn(Optional.of(membresia));
        when(userCompanyRepository.findByUsuarioId(5L)).thenReturn(List.of(membresia));

        service.deleteUser(5L);

        assertThat(membresia.isActive()).isFalse();
        assertThat(usuario.isActivo()).isFalse();
        verify(sesionRevocacionService).revocarPorUsuario(5L);
    }
}
