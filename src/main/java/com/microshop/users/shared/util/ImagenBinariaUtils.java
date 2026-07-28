package com.microshop.users.shared.util;

import com.microshop.users.shared.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * Validación y normalización de imágenes subidas para almacenarse como binario en BD.
 * Compartido por el logotipo de empresa (usuarios) y la foto de empleado (rrhh),
 * replicando el patrón ya usado en microshopventas (BannerCommandService).
 */
public final class ImagenBinariaUtils {

    private ImagenBinariaUtils() {
    }

    /** Tamaño máximo aceptado: 500 KB. */
    public static final long MAX_BYTES = 512_000L;

    /** MIME types permitidos. */
    public static final Set<String> MIMES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    /**
     * Valida el archivo (no vacío, MIME permitido, tamaño máximo y magic bytes)
     * y retorna su contenido binario.
     *
     * @throws BusinessException con mensaje en español si alguna validación falla.
     */
    public static byte[] validarYExtraer(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo de imagen está vacío.");
        }
        String mime = file.getContentType();
        if (mime == null || !MIMES_PERMITIDOS.contains(mime)) {
            throw new BusinessException("Tipo de imagen no permitido. Use JPEG, PNG o WebP.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("La imagen supera el límite de 500 KB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Error al leer el archivo de imagen.");
        }

        // Verificación de magic bytes (defensa en profundidad contra Content-Type spoofing)
        if (!esBinarioCoherente(bytes, mime)) {
            throw new BusinessException("El contenido del archivo no corresponde al tipo declarado.");
        }
        return bytes;
    }

    /** Compara la cabecera del binario contra la firma esperada del MIME declarado. */
    public static boolean esBinarioCoherente(byte[] bytes, String mime) {
        if (bytes == null || bytes.length < 4) return false;
        return switch (mime) {
            case "image/jpeg" -> bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8;
            case "image/png"  -> bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50
                              && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47;
            case "image/webp" -> bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49
                              && bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x46;
            default -> false;
        };
    }

    /** MIME seguro para el header Content-Type: nunca devuelve un valor no whitelisteado. */
    public static String resolveContentType(String mime) {
        return (mime != null && MIMES_PERMITIDOS.contains(mime)) ? mime : "application/octet-stream";
    }
}
