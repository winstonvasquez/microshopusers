package com.microshop.users.shared.constants;

/**
 * Mensajes de error centralizados.
 * Usados por los services para lanzar excepciones con mensajes consistentes.
 */
public final class MensajesError {

    private MensajesError() {
    }

    // Usuario
    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String USUARIO_USERNAME_DUPLICADO = "El nombre de usuario ya existe: ";
    public static final String USUARIO_EMAIL_DUPLICADO = "El email ya existe: ";
    public static final String USUARIO_DOCUMENTO_DUPLICADO = "El número de documento ya existe: ";

    // Empresa
    public static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";
    public static final String EMPRESA_RUC_DUPLICADO = "Ya existe una empresa con RUC: ";
    public static final String USUARIO_NO_PERTENECE_EMPRESA = "El usuario no pertenece a la empresa especificada";

    // Autenticación
    public static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    public static final String SESION_EXPIRADA = "Sesión expirada o no encontrada";
    public static final String OTP_INVALIDO = "Código de verificación incorrecto o expirado";
    public static final String TOKEN_GOOGLE_INVALIDO = "Token de Google inválido";
    public static final String TOKEN_FACEBOOK_INVALIDO = "El token de Facebook es inválido o sin permisos de email";
    public static final String PROVEEDOR_NO_SOPORTADO = "Provider no soportado aún: ";

    // Vendedor
    public static final String VENDEDOR_PERFIL_DUPLICADO = "El usuario ya tiene un perfil de vendedor";
    public static final String VENDEDOR_NO_ENCONTRADO = "Perfil de vendedor no encontrado";

    // Rol
    public static final String ROL_NO_ENCONTRADO = "Rol no encontrado: ";
}
