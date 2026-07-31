package com.microshop.users.application.command;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microshop.users.application.MessageHelper;
import com.microshop.users.config.security.JwtClaims;
import com.microshop.users.application.dto.LoginRequest;
import com.microshop.users.application.dto.LoginResponse;
import com.microshop.users.application.dto.SocialLoginRequest;
import com.microshop.users.application.dto.SupervisorAuthResponse;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.config.SecurityProperties;
import com.microshop.users.shared.constants.AppConstants;

import com.microshop.users.config.security.JwtService;
import com.microshop.users.infrastructure.persistence.entity.SesionEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.SesionRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.domain.service.EmailService;
import com.microshop.users.domain.service.OtpService;
import com.microshop.users.shared.exception.BusinessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper msg;
    private final EmailService emailService;
    private final OtpService otpService;
    private final SecurityProperties securityProps;
    private final SaasQueryService saasQueryService;

    public void register(LoginRequest request) {
        var user = new UsuarioEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        // Incomplete logic from original implementation
    }

    public LoginResponse login(LoginRequest request) {
        authenticate(request);
        var user = getUser(request.username());
        var userCompanies = getUserCompanies(user.getId());
        var companyId = determineCompanyId(request.companyId(), userCompanies);

        validateCompanyMembership(companyId, userCompanies);

        var jwtToken = generateJwtToken(user, companyId);
        createSession(user, jwtToken, companyId);

        var availableCompanyIds = getAvailableCompanyIds(userCompanies);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(companyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(), companyId, availableCompanyIds, enabledModules);
    }

    private void authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    }

    private UsuarioEntity getUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("auth.credentials.invalid")));
    }

    private List<UserCompanyEntity> getUserCompanies(Long userId) {
        return new ArrayList<>(userCompanyRepository.findByUsuarioId(userId));
    }

    private Long determineCompanyId(Long requestedCompanyId, List<UserCompanyEntity> userCompanies) {
        if (requestedCompanyId != null && requestedCompanyId != 0L) {
            return requestedCompanyId;
        }
        return userCompanies.isEmpty() ? null : userCompanies.get(0).getCompany().getId();
    }

    private void validateCompanyMembership(Long companyId, List<UserCompanyEntity> userCompanies) {
        if (companyId == null)
            return;

        var membresia = userCompanies.stream()
                .filter(uc -> uc.getCompany().getId().equals(companyId) && uc.isActive())
                .findFirst();

        if (membresia.isEmpty()) {
            // BusinessException mapea a 400 igual que la IllegalArgumentException que había antes:
            // el código HTTP no cambia, solo se usa la excepción del proyecto.
            throw new BusinessException(msg.get("auth.user.company.mismatch"));
        }

        // B08 (2026-07-28): hasta ahora aquí solo se comprobaba `uc.isActive()`, que es el estado de
        // la MEMBRESÍA, nunca `company.isActive()`. Consecuencia: "suspender" una empresa con
        // DELETE /users/api/companies/{id} (que hace soft-delete poniendo is_active=false) no
        // impedía que sus usuarios siguieran entrando y operando con total normalidad — la
        // suspensión de tenant no existía funcionalmente. Se valida en el login Y en el cambio de
        // empresa, porque switchCompany reutiliza este mismo método.
        if (!membresia.get().getCompany().isActive()) {
            throw new AccessDeniedException(msg.get("auth.company.suspended"));
        }
    }

    private String generateJwtToken(UsuarioEntity user, Long companyId) {
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), Collections.emptyList());
        // Los nombres de claim salen de JwtClaims, que es el contrato que leen los seis servicios.
        // No poner literales aquí: esta ruta y la de SaasOnboardingCommandService ya habían divergido
        // una vez (esta emitía `roles` y la otra no).
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(JwtClaims.USER_ID, user.getId());
        // Authorities Spring Security: prefijo ROLE_ obligatorio para que los
        // @PreAuthorize("hasAuthority('ROLE_ADMIN')") o hasRole('ADMIN') de los
        // microservicios reconozcan el rol del usuario.
        if (user.getRol() != null && user.getRol().getNombre() != null) {
            extraClaims.put(JwtClaims.ROLES, List.of("ROLE_" + user.getRol().getNombre().toUpperCase()));
        }
        if (companyId != null) {
            extraClaims.put(JwtClaims.COMPANY_ID, companyId);
            List<String> modules = saasQueryService.getEnabledModuleCodes(companyId);
            if (!modules.isEmpty()) {
                extraClaims.put(JwtClaims.MODULES, String.join(JwtClaims.MODULES_SEPARATOR, modules));
            }
        }
        return jwtService.generateToken(extraClaims, userDetails);
    }

    private void createSession(UsuarioEntity user, String jwtToken, Long companyId) {
        SesionEntity session = new SesionEntity();
        session.setUsuario(user);
        session.setToken(jwtToken);
        session.setFechaInicio(Instant.now());
        session.setFechaExpiracion(Instant.now().plus(1, ChronoUnit.DAYS));
        session.setValido(true);
        session.setCompanyId(companyId);
        // jti: identificador de revocacion (M27). Sin el, invalidar una sesion concreta obligaba a
        // comparar por igualdad el JWT completo (~800 caracteres) en cada consulta.
        session.setJti(jwtService.extractJti(jwtToken));
        sesionRepository.save(session);
    }

    private List<Long> getAvailableCompanyIds(List<UserCompanyEntity> userCompanies) {
        return userCompanies.stream()
                .map(uc -> uc.getCompany().getId())
                .collect(Collectors.toList());
    }

    /**
     * Cambia la empresa activa del usuario autenticado y genera un nuevo JWT.
     * Valida que el usuario pertenezca a la empresa destino.
     */
    public LoginResponse switchCompany(String username, Long targetCompanyId) {
        // Guarda propia del servicio, además de la validación del DTO en el controller. No es
        // redundancia decorativa: validateCompanyMembership() empieza con
        // `if (companyId == null) return;` —correcto para el LOGIN, donde determineCompanyId() ya
        // eligió la primera empresa del usuario, y donde un usuario sin ninguna empresa debe poder
        // obtener token—, pero en el cambio de empresa el valor llega crudo del cliente y ese
        // early-return lo dejaba pasar hasta generateJwtToken(user, null), emitiendo un JWT sin
        // claim companyId ni modules. Cualquier futuro llamador de este método queda cubierto aquí.
        if (targetCompanyId == null || targetCompanyId <= 0L) {
            throw new BusinessException(msg.get("auth.user.company.mismatch"));
        }
        var user = getUser(username);
        var userCompanies = getUserCompanies(user.getId());
        validateCompanyMembership(targetCompanyId, userCompanies);

        var jwtToken = generateJwtToken(user, targetCompanyId);
        createSession(user, jwtToken, targetCompanyId);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(targetCompanyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                targetCompanyId, getAvailableCompanyIds(userCompanies), enabledModules);
    }

    /**
     * Devuelve las empresas del usuario.
     * El flag isActive indica si el usuario está habilitado en esa empresa.
     */
    public List<Map<String, Object>> getMyCompanies(String username) {
        var user = getUser(username);
        var userCompanies = getUserCompanies(user.getId());
        return userCompanies.stream()
                .map(uc -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("companyId", uc.getCompany().getId());
                    m.put("companyName", uc.getCompany().getName());
                    m.put("ruc", uc.getCompany().getRuc());
                    m.put("isActive", uc.isActive());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * Login por PIN numérico — para POS (cambio de turno rápido).
     * Compara el PIN hasheado con BCrypt.
     */
    public LoginResponse pinLogin(String pin, Long companyId) {
        // Candidatos con PIN configurado. Si se especifica empresa, acotamos a los
        // miembros ACTIVOS de ESA empresa (query companyId != null) — evita escanear
        // (y BCrypt-comparar) usuarios con PIN de TODOS los tenants. Sin empresa
        // (caso raro/legacy) mantenemos el escaneo global previo.
        List<UsuarioEntity> candidatos = companyId != null
                ? usuarioRepository.findByPinHashIsNotNullAndCompanyId(companyId)
                : usuarioRepository.findByPinHashIsNotNull();

        UsuarioEntity user = null;
        for (UsuarioEntity u : candidatos) {
            if (passwordEncoder.matches(pin, u.getPinHash())) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new IllegalArgumentException(msg.get("auth.credentials.invalid"));
        }

        var userCompanies = getUserCompanies(user.getId());
        var resolvedCompanyId = determineCompanyId(companyId, userCompanies);
        if (companyId == null) {
            // Sin empresa solicitada: se resolvió un default de la lista del usuario,
            // igual que antes. Con empresa solicitada, la query ya garantizó membresía
            // activa — revalidar aquí sería redundante.
            validateCompanyMembership(resolvedCompanyId, userCompanies);
        }

        var jwtToken = generateJwtToken(user, resolvedCompanyId);
        createSession(user, jwtToken, resolvedCompanyId);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(resolvedCompanyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                resolvedCompanyId, getAvailableCompanyIds(userCompanies), enabledModules);
    }

    /** Roles habilitados para autorizar operaciones sensibles del POS (descuentos, etc.). */
    private static final java.util.Set<String> ROLES_AUTORIZADORES =
            java.util.Set.of(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.GERENTE,
                    AppConstants.Seguridad.SUPERADMIN);

    /**
     * Verifica el PIN de un supervisor para autorizar una operación sensible en el POS.
     * NO emite JWT ni crea sesión: solo confirma que el PIN pertenece a un usuario con
     * rol autorizador y (si se indica) miembro de la empresa.
     *
     * <p>Reemplaza el "acepta cualquier PIN" del frontend. Ante PIN inválido, rol no
     * autorizador o empresa distinta devuelve {@code authorized=false} sin detalle.</p>
     */
    @Transactional(readOnly = true)
    public SupervisorAuthResponse verifySupervisorPin(String pin, Long companyId) {
        if (pin == null || pin.isBlank()) {
            return new SupervisorAuthResponse(false, null, null, null);
        }

        // Solo candidatos con PIN y rol autorizador: el filtro lo hace la BD, no un findAll().
        UsuarioEntity supervisor = null;
        for (UsuarioEntity u : usuarioRepository.findByPinHashIsNotNullAndRol_NombreIn(ROLES_AUTORIZADORES)) {
            if (passwordEncoder.matches(pin, u.getPinHash())) {
                supervisor = u;
                break;
            }
        }

        if (supervisor == null) {
            return new SupervisorAuthResponse(false, null, null, null);
        }

        // El rol ya está garantizado por la query; se lee solo para devolverlo.
        String rol = supervisor.getRol().getNombre().toUpperCase();

        if (companyId != null) {
            boolean perteneceEmpresa = getUserCompanies(supervisor.getId()).stream()
                    .anyMatch(uc -> uc.getCompany().getId().equals(companyId) && uc.isActive());
            if (!perteneceEmpresa) {
                log.warn("Supervisor {} no pertenece a la empresa {} — autorización denegada",
                        supervisor.getId(), companyId);
                return new SupervisorAuthResponse(false, null, null, null);
            }
        }

        return new SupervisorAuthResponse(true, supervisor.getId(), supervisor.getUsername(), rol);
    }

    /**
     * Establece un PIN numérico para login rápido en POS.
     */
    public void setPin(Long userId, String pin) {
        UsuarioEntity user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        user.setPinHash(passwordEncoder.encode(pin));
        usuarioRepository.save(user);
        log.info("PIN establecido para usuario {}", userId);
    }

    public void sendOtp(String email) {
        String otp = otpService.generateAndStore(email);
        emailService.sendOtp(email, otp);
        log.info("OTP sent to {}", email);
    }

    public LoginResponse verifyOtpAndLogin(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new IllegalArgumentException(msg.get("auth.verification.code.invalid"));
        }
        otpService.invalidate(email);

        UsuarioEntity user = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException(msg.get("user.not.found", email)));

        var userCompanies = getUserCompanies(user.getId());
        var companyId = determineCompanyId(null, userCompanies);
        var jwtToken = generateJwtToken(user, companyId);
        createSession(user, jwtToken, companyId);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(companyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                companyId, getAvailableCompanyIds(userCompanies), enabledModules);
    }

    public LoginResponse socialLogin(SocialLoginRequest request) {
        if ("google".equalsIgnoreCase(request.provider())) {
            return verifyGoogleToken(request.token());
        } else if ("facebook".equalsIgnoreCase(request.provider())) {
            return verifyFacebookToken(request.token());
        }
        throw new IllegalArgumentException(msg.get("auth.provider.unsupported", request.provider()));
    }

    private LoginResponse verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(securityProps.getOauth2().getGoogle().getClientId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                return autoRegisterOrLoginSocialUser(email, name);
            } else {
                throw new IllegalArgumentException(msg.get("auth.google.token.invalid"));
            }
        } catch (Exception e) {
            log.error("Error validando token de Google", e);
            throw new IllegalArgumentException(msg.get("auth.google.token.validation.failed"));
        }
    }

    private LoginResponse verifyFacebookToken(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null && response.containsKey("email")) {
                String email = (String) response.get("email");
                String name = (String) response.get("name");
                return autoRegisterOrLoginSocialUser(email, name);
            } else {
                throw new IllegalArgumentException(
                        msg.get("auth.facebook.token.invalid"));
            }
        } catch (Exception e) {
            log.error("Error validando token de Facebook", e);
            throw new IllegalArgumentException(msg.get("auth.facebook.token.validation.failed"));
        }
    }

    private LoginResponse autoRegisterOrLoginSocialUser(String email, String name) {
        UsuarioEntity user = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    UsuarioEntity newUser = new UsuarioEntity();
                    newUser.setEmail(email.toLowerCase());
                    newUser.setUsername(email.toLowerCase());
                    newUser.setPassword(passwordEncoder.encode("SOCIAL_" + System.currentTimeMillis()));
                    return usuarioRepository.save(newUser);
                });

        var userCompanies = getUserCompanies(user.getId());
        var companyId = determineCompanyId(null, userCompanies);
        var jwtToken = generateJwtToken(user, companyId);
        createSession(user, jwtToken, companyId);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(companyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                companyId, getAvailableCompanyIds(userCompanies), enabledModules);
    }
}
