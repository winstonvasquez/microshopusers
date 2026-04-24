package com.microshop.users.infrastructure.service;

import com.microshop.users.domain.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpServiceImpl implements OtpService {

    private static final long OTP_EXPIRY_SECONDS = 600;
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    @Override
    public String generateAndStore(String email) {
        String otp = generateOtp();
        store.put(email.toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));
        log.info("OTP generated for {}", email);
        return otp;
    }

    @Override
    public boolean verify(String email, String otp) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null)
            return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email.toLowerCase());
            return false;
        }
        return entry.otp().equals(otp);
    }

    @Override
    public void invalidate(String email) {
        store.remove(email.toLowerCase());
    }

    private String generateOtp() {
        int number = RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", number);
    }

    private record OtpEntry(String otp, Instant expiresAt) {
    }
}
