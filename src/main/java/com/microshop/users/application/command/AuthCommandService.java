package com.microshop.users.application.command;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.dto.LoginRequest;
import com.microshop.users.application.dto.LoginResponse;
import com.microshop.users.application.dto.SocialLoginRequest;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.config.SecurityProperties;

import com.microshop.users.config.security.JwtService;
import com.microshop.users.infrastructure.persistence.entity.SesionEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.SesionRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.domain.service.EmailService;
import com.microshop.users.domain.service.OtpService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        boolean belongsToCompany = userCompanies.stream()
                .anyMatch(uc -> uc.getCompany().getId().equals(companyId) && uc.isActive());

        if (!belongsToCompany) {
            throw new IllegalArgumentException(
                    msg.get("auth.user.company.mismatch"));
        }
    }

    private String generateJwtToken(UsuarioEntity user, Long companyId) {
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), Collections.emptyList());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        // Authorities Spring Security: prefijo ROLE_ obligatorio para que los
        // @PreAuthorize("hasAuthority('ROLE_ADMIN')") o hasRole('ADMIN') de los
        // microservicios reconozcan el rol del usuario.
        if (user.getRol() != null && user.getRol().getNombre() != null) {
            extraClaims.put("roles", List.of("ROLE_" + user.getRol().getNombre().toUpperCase()));
        }
        if (companyId != null) {
            extraClaims.put("companyId", companyId);
            List<String> modules = saasQueryService.getEnabledModuleCodes(companyId);
            if (!modules.isEmpty()) {
                extraClaims.put("modules", String.join(",", modules));
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
        // Hash the PIN and find user
        var allUsers = usuarioRepository.findAll();
        UsuarioEntity user = null;
        for (UsuarioEntity u : allUsers) {
            if (u.getPinHash() != null && passwordEncoder.matches(pin, u.getPinHash())) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new IllegalArgumentException(msg.get("auth.credentials.invalid"));
        }

        var userCompanies = getUserCompanies(user.getId());
        var resolvedCompanyId = determineCompanyId(companyId, userCompanies);
        validateCompanyMembership(resolvedCompanyId, userCompanies);

        var jwtToken = generateJwtToken(user, resolvedCompanyId);
        createSession(user, jwtToken, resolvedCompanyId);
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(resolvedCompanyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                resolvedCompanyId, getAvailableCompanyIds(userCompanies), enabledModules);
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
            @SuppressWarnings("unchecked")
            Map<String, Object> response = WebClient.create()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
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
