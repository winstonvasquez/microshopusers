package com.microshop.users.application.dto;

public record SaasModuleDto(Long id, String code, String name, String description, String icon, String routePrefix, boolean enabled) {}
