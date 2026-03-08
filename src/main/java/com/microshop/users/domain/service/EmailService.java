package com.microshop.users.domain.service;

public interface EmailService {
    void sendOtp(String to, String otp);
}
