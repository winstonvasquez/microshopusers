package com.microshop.users.application.command;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microshop.users.infrastructure.web.dto.LoginRequest;
import com.microshop.users.infrastructure.web.dto.LoginResponse;
import com.microshop.users.infrastructure.web.dto.SocialLoginRequest;
import org.springframework.beans.factory.annotation.Value;

import com.microshop.users.security.JwtService;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.client.RestTemplate;

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
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final OtpService otpService;

    @Value("${application.security.oauth2.client.registration.google.client-id:TU_CLIENT_ID_DE_GOOGLE}")
    private String googleClientId;

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

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(), companyId, availableCompanyIds);
    }

    private void authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    }

    private UsuarioEntity getUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
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
                    messageSource.getMessage("auth.user.company.mismatch", null, LocaleContextHolder.getLocale()));
        }
    }

    private String generateJwtToken(UsuarioEntity user, Long companyId) {
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), Collections.emptyList());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        if (companyId != null) {
            extraClaims.put("companyId", companyId);
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

    public void sendOtp(String email) {
        String otp = otpService.generateAndStore(email);
        emailService.sendOtp(email, otp);
        log.info("OTP sent to {}", email);
    }

    public LoginResponse verifyOtpAndLogin(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new IllegalArgumentException("Código de verificación incorrecto o expirado");
        }
        otpService.invalidate(email);

        UsuarioEntity user = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        var userCompanies = getUserCompanies(user.getId());
        var companyId = determineCompanyId(null, userCompanies);
        var jwtToken = generateJwtToken(user, companyId);
        createSession(user, jwtToken, companyId);

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                companyId, getAvailableCompanyIds(userCompanies));
    }

    public LoginResponse socialLogin(SocialLoginRequest request) {
        if ("google".equalsIgnoreCase(request.provider())) {
            return verifyGoogleToken(request.token());
        } else if ("facebook".equalsIgnoreCase(request.provider())) {
            return verifyFacebookToken(request.token());
        }
        throw new IllegalArgumentException("Provider no soportado aún: " + request.provider());
    }

    private LoginResponse verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                return autoRegisterOrLoginSocialUser(email, name);
            } else {
                throw new IllegalArgumentException("Token de Google inválido");
            }
        } catch (Exception e) {
            log.error("Error validando token de Google", e);
            throw new IllegalArgumentException("No se pudo validar el token con Google");
        }
    }

    private LoginResponse verifyFacebookToken(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("email")) {
                String email = (String) response.get("email");
                String name = (String) response.get("name");
                return autoRegisterOrLoginSocialUser(email, name);
            } else {
                throw new IllegalArgumentException(
                        "El token de Facebook es inválido o el usuario no concedió permisos de email");
            }
        } catch (Exception e) {
            log.error("Error validando token de Facebook", e);
            throw new IllegalArgumentException("No se pudo validar el token con Facebook. Verifica que sea válido.");
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

        return new LoginResponse(jwtToken, user.getUsername(), user.getId(),
                companyId, getAvailableCompanyIds(userCompanies));
    }
}
