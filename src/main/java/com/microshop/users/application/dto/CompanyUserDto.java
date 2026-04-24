package com.microshop.users.application.dto;

import java.time.Instant;
import java.util.List;

public record CompanyUserDto(
    Long userId,
    String username,
    String fullName,
    String email,
    List<String> roles,
    boolean isActive,
    Instant assignedAt
) {}
