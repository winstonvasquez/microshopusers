package com.microshop.users.application.dto;

/** Quién desbloqueó el POS. No lleva token: un PIN no abre sesión. */
public record PinLoginResponse(String username, Long userId, Long activeCompanyId) {}
