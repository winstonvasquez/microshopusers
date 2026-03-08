package com.microshop.users.domain.service;

public interface OtpService {
    String generateAndStore(String email);

    boolean verify(String email, String otp);

    void invalidate(String email);
}
