package com.microshop.users.shared.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.web.util.HtmlUtils;

/**
 * Utilidad compartida para exportar listados a XLSX y CSV con datos limpios
 * (sin markup HTML). Reemplaza el patrón de "scrapear la tabla del frontend"
 * generando el archivo directamente en el backend a partir de datos ya
 * mapeados (DTOs), null-safe en todo el proceso.
 */
public final class SpreadsheetExporter {

    /** Code point del BOM (Byte Order Mark) UTF-8, para que Excel detecte el encoding del CSV. */
    private static final char BOM_UTF8 = 0xFEFF;

    private SpreadsheetExporter() {
        // Utilidad estática: no instanciable.
    }

    /**
     * Genera un workbook XLSX con: fila 0 = título (merge sobre todas las
     * columnas, negrita), fila 1 = cabeceras (negrita + fondo), filas
     * siguientes = datos. Autoajusta el ancho de columnas al final.
     */
    public static byte[] toXlsx(String titulo, List<String> cabeceras, List<List<Object>> filas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Datos");
            int numCols = cabeceras != null ? cabeceras.size() : 0;

            escribirTitulo(workbook, sheet, titulo, numCols);
            escribirCabeceras(workbook, sheet, cabeceras);
            escribirFilas(sheet, filas);
            autoajustarColumnas(sheet, numCols);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando archivo XLSX", e);
        }
    }

    /** Fila 0: título en negrita y tamaño mayor, fusionado sobre todas las columnas. */
    private static void escribirTitulo(XSSFWorkbook workbook, Sheet sheet, String titulo, int numCols) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue(titulo != null ? titulo : "");
        cell.setCellStyle(style);
        // Con una sola columna no hay nada que fusionar (POI rechaza un rango de una celda).
        if (numCols > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, numCols - 1));
        }
    }

    /** Fila 1: cabeceras en negrita con fondo gris claro. */
    private static void escribirCabeceras(XSSFWorkbook workbook, Sheet sheet, List<String> cabeceras) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row row = sheet.createRow(1);
        if (cabeceras == null) {
            return;
        }
        for (int i = 0; i < cabeceras.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cabeceras.get(i) != null ? cabeceras.get(i) : "");
            cell.setCellStyle(style);
        }
    }

    /** Filas de datos, a partir de la 2. Una fila null se deja vacía en vez de romper el export. */
    private static void escribirFilas(Sheet sheet, List<List<Object>> filas) {
        if (filas == null) {
            return;
        }
        int rowIdx = 2;
        for (List<Object> fila : filas) {
            Row row = sheet.createRow(rowIdx++);
            if (fila == null) {
                continue;
            }
            for (int i = 0; i < fila.size(); i++) {
                setCellValue(row.createCell(i), fila.get(i));
            }
        }
    }

    private static void autoajustarColumnas(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** Asigna el valor de una celda según el tipo real del dato, null-safe. */
    private static void setCellValue(Cell cell, Object valor) {
        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (valor instanceof Boolean b) {
            cell.setCellValue(b);
        } else {
            cell.setCellValue(valor.toString());
        }
    }

    /**
     * Genera un CSV en UTF-8 con BOM (para que Excel detecte el encoding
     * correctamente), separador coma, saltos de línea \r\n y cada campo
     * escapado entre comillas dobles (duplicando comillas internas). Valores
     * null se serializan como cadena vacía.
     */
    public static byte[] toCsv(List<String> cabeceras, List<List<Object>> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM_UTF8);

        if (cabeceras != null) {
            appendCsvRow(sb, cabeceras);
        }
        if (filas != null) {
            for (List<Object> fila : filas) {
                appendCsvRow(sb, fila);
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendCsvRow(StringBuilder sb, List<?> valores) {
        if (valores == null) {
            sb.append("\r\n");
            return;
        }
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(valores.get(i)));
        }
        sb.append("\r\n");
    }

    /**
     * Quita el markup HTML de un valor guardado por el editor de texto enriquecido
     * del frontend (negritas, listas, párrafos, etc.), dejando texto plano apto para
     * una celda de hoja de cálculo. Null-safe: devuelve cadena vacía si {@code html} es null.
     */
    public static String plano(String html) {
        if (html == null) {
            return "";
        }
        String sinEtiquetas = html.replaceAll("<[^>]*>", " ");
        String sinEntidades = HtmlUtils.htmlUnescape(sinEtiquetas);
        return sinEntidades.replaceAll("\\s+", " ").trim();
    }

    /** Escapa un valor para CSV: comillas dobles alrededor, duplicando las internas. */
    private static String escapeCsv(Object valor) {
        String texto = valor != null ? valor.toString() : "";
        String escapado = texto.replace("\"", "\"\"");
        return "\"" + escapado + "\"";
    }
}
