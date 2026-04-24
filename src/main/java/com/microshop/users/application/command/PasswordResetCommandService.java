package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.microshop.users.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Servicio de comando para el flujo de recuperación y restablecimiento de contraseña.
 *
 * Estrategia de seguridad:
 * - forgotPassword() siempre responde 200 sin revelar si el email existe en la BD.
 * - Si JavaMailSender no está configurado, el enlace se registra en el log (modo desarrollo).
 * - El token es un UUID aleatorio con expiración de 1 hora y uso único.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordResetCommandService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@microshop.com}")
    private String fromEmail;

    /**
     * Inicia el flujo de recuperación de contraseña para el email indicado.
     * Siempre retorna sin excepción para no revelar si el email existe.
     *
     * @param email correo electrónico del usuario
     */
    public void forgotPassword(String email) {
        usuarioRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            // Eliminar tokens anteriores del usuario para evitar tokens huérfanos
            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

            PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                    .userId(user.getId())
                    .token(token)
                    .expiresAt(expiresAt)
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Recuperación de contraseña - MicroShop");
                message.setText("Hola,\n\nSe solicitó restablecer tu contraseña.\n\n"
                        + "Haz clic en el siguiente enlace (válido por 1 hora):\n"
                        + resetLink + "\n\n"
                        + "Si no solicitaste este cambio, ignora este correo.\n\n"
                        + "— MicroShop ERP");
                mailSender.send(message);
                log.info("Email de recuperación enviado a {}", email);
            } catch (MailException ex) {
                // Fallback: registrar en log para desarrollo
                log.warn("No se pudo enviar email a {}. RESET LINK: {}", email, resetLink);
                log.debug("Error de envío de email", ex);
            }
        });
    }

    /**
     * Restablece la contraseña del usuario a partir de un token válido.
     *
     * @param token       token de recuperación recibido por correo
     * @param newPassword nueva contraseña en texto plano (mínimo 8 caracteres)
     * @throws IllegalArgumentException si el token no existe, ya fue usado o expiró
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("El token ya fue utilizado");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("El token ha expirado");
        }

        var user = usuarioRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Contraseña restablecida para usuario ID {}", user.getId());
    }
}
