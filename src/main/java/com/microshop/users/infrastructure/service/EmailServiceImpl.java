package com.microshop.users.infrastructure.service;

import com.microshop.users.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final String from = "noreply@microshop.com";

    @Override
    public void sendOtp(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Tu código de verificación - MicroShop");
            message.setText(
                    "Hola,\n\n" +
                            "Tu código de verificación es: " + otp + "\n\n" +
                            "Este código expira en 10 minutos.\n\n" +
                            "Si no solicitaste este código, ignora este mensaje.\n\n" +
                            "— El equipo de MicroShop");
            mailSender.send(message);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException(messageSource.getMessage("email.verification.send.error", null, LocaleContextHolder.getLocale()));
        }
    }
}
