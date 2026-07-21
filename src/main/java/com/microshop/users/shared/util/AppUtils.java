package com.microshop.users.shared.util;

import com.microshop.users.shared.constants.AppConstants;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.function.Function;

@UtilityClass
public class AppUtils {

    public static final String FECHA_FORMATO = "dd/MM/yyyy";

    /** Concatena nombres y apellidos, null-safe, con un solo espacio entre ambos. */
    public static String fullName(String nombres, String apellidos) {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        if (n.isEmpty()) {
            return a;
        }
        if (a.isEmpty()) {
            return n;
        }
        return n + " " + a;
    }

    /** Normaliza un término de búsqueda: en blanco -> null, si no lo retorna trimmeado. */
    public static String searchTermOrNull(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    /** Retorna BigDecimal.ZERO si el valor es null. */
    public static BigDecimal zeroIfNull(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /** Suma varargs de BigDecimal, tratando cada null como cero. */
    public static BigDecimal sum(BigDecimal... vs) {
        BigDecimal total = BigDecimal.ZERO;
        if (vs == null) {
            return total;
        }
        for (BigDecimal v : vs) {
            total = total.add(zeroIfNull(v));
        }
        return total;
    }

    /** Redondea un monto a la escala/redondeo monetario estándar del sistema. Null-safe. */
    public static BigDecimal redondearMoneda(BigDecimal v) {
        if (v == null) {
            return null;
        }
        return v.setScale(AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);
    }

    /** Convierte un Object a Long: Number->longValue(), String->parseLong (try/catch), si no null. */
    public static Long toLongOrNull(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** Extrae el id de una entidad solo si esta no es null; si no, retorna null. */
    public static <T, R> R idOrNull(T entity, Function<T, R> idExtractor) {
        return entity == null ? null : idExtractor.apply(entity);
    }
}
