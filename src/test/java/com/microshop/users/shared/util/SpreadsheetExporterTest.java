package com.microshop.users.shared.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios de {@link SpreadsheetExporter} — la utilidad que genera TODAS las
 * exportaciones XLSX/CSV del backend (~60 listados y 8 reportes).
 *
 * <p><b>Por qué existe este test.</b> Medido con {@code crap4java} el 2026-08-06,
 * {@code toXlsx} tenía complejidad ciclomática 13 con 0% de cobertura, o sea
 * CRAP = 13² · (1−0)³ + 13 = <b>182</b> contra un umbral de 8, y era el peor método
 * del servicio en cinco de los seis microservicios. La palanca dominante de la métrica
 * es la cobertura, no la complejidad: cubrir estas ramas baja el CRAP de 182 a ~13
 * sin tocar una línea de producción.
 *
 * <p>Se ejercitan las ramas que la fórmula cuenta: cabeceras null y no null, título null,
 * la rama de merge (que sólo aplica con más de una columna), lista de filas null, una fila
 * null <i>dentro</i> de la lista, y los cuatro tipos que distingue {@code setCellValue}
 * (null, Number, Boolean y el resto vía {@code toString}). Los privados
 * {@code setCellValue}, {@code appendCsvRow} y {@code escapeCsv} se cubren a través de la
 * API pública, que es como los usa el código real.
 */
class SpreadsheetExporterTest {

    @Test
    @DisplayName("toXlsx: título en la fila 0, cabeceras en la 1 y datos desde la 2")
    void toXlsxColocaTituloCabecerasYDatos() throws Exception {
        byte[] xlsx = SpreadsheetExporter.toXlsx(
                "Ventas de agosto",
                List.of("Producto", "Cantidad", "Activo"),
                List.of(Arrays.asList("Café", 3, true)));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = wb.getSheetAt(0);
            assertThat(hoja.getSheetName()).isEqualTo("Datos");
            assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Ventas de agosto");
            assertThat(hoja.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Producto");
            assertThat(hoja.getRow(1).getCell(2).getStringCellValue()).isEqualTo("Activo");
            assertThat(hoja.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Café");

            // El título y las cabeceras van en negrita; los datos no.
            assertThat(((XSSFCellStyle) hoja.getRow(0).getCell(0).getCellStyle()).getFont().getBold()).isTrue();
            assertThat(((XSSFCellStyle) hoja.getRow(1).getCell(0).getCellStyle()).getFont().getBold()).isTrue();
        }
    }

    @Test
    @DisplayName("toXlsx: cada tipo va a su celda — número como numérico, booleano como booleano, null como vacío")
    void toXlsxRespetaElTipoDeCadaValor() throws Exception {
        byte[] xlsx = SpreadsheetExporter.toXlsx(
                "Tipos",
                List.of("Texto", "Numero", "Booleano", "Nulo"),
                List.of(Arrays.asList("hola", 12.5, false, null)));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = wb.getSheetAt(0);
            Cell texto = hoja.getRow(2).getCell(0);
            Cell numero = hoja.getRow(2).getCell(1);
            Cell booleano = hoja.getRow(2).getCell(2);
            Cell nulo = hoja.getRow(2).getCell(3);

            assertThat(texto.getCellType()).isEqualTo(CellType.STRING);
            assertThat(numero.getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(numero.getNumericCellValue()).isEqualTo(12.5);
            assertThat(booleano.getCellType()).isEqualTo(CellType.BOOLEAN);
            assertThat(booleano.getBooleanCellValue()).isFalse();
            // Un null NO deja la celda ausente: la escribe como cadena vacía.
            assertThat(nulo.getCellType()).isEqualTo(CellType.STRING);
            assertThat(nulo.getStringCellValue()).isEmpty();
        }
    }

    @Test
    @DisplayName("toXlsx: el merge del título sólo aplica con más de una columna")
    void toXlsxSoloFusionaElTituloSiHayVariasColumnas() throws Exception {
        byte[] varias = SpreadsheetExporter.toXlsx("T", List.of("A", "B", "C"), List.of());
        byte[] unaSola = SpreadsheetExporter.toXlsx("T", List.of("A"), List.of());

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(varias))) {
            assertThat(wb.getSheetAt(0).getNumMergedRegions()).isEqualTo(1);
            assertThat(wb.getSheetAt(0).getMergedRegion(0).getLastColumn()).isEqualTo(2);
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(unaSola))) {
            assertThat(wb.getSheetAt(0).getNumMergedRegions()).isZero();
        }
    }

    @Test
    @DisplayName("toXlsx: null en título, cabeceras o filas no revienta")
    void toXlsxEsNullSafe() throws Exception {
        byte[] xlsx = SpreadsheetExporter.toXlsx(null, null, null);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = wb.getSheetAt(0);
            assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).isEmpty();
            assertThat(hoja.getNumMergedRegions()).isZero();   // numCols = 0, no hay merge
            assertThat(hoja.getRow(2)).isNull();               // sin filas de datos
        }
    }

    @Test
    @DisplayName("toXlsx: una cabecera null se escribe vacía y una fila null se salta sin romper las siguientes")
    void toXlsxToleraNullsInternos() throws Exception {
        byte[] xlsx = SpreadsheetExporter.toXlsx(
                "Huecos",
                Arrays.asList("A", null),
                Arrays.asList(Arrays.asList("x", "y"), null, Arrays.asList("z", "w")));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = wb.getSheetAt(0);
            assertThat(hoja.getRow(1).getCell(1).getStringCellValue()).isEmpty();
            assertThat(hoja.getRow(2).getCell(0).getStringCellValue()).isEqualTo("x");
            // La fila null crea la fila pero sin celdas; la siguiente sigue en su sitio.
            assertThat(hoja.getRow(3).getPhysicalNumberOfCells()).isZero();
            assertThat(hoja.getRow(4).getCell(0).getStringCellValue()).isEqualTo("z");
        }
    }

    @Test
    @DisplayName("toCsv: BOM UTF-8, separador coma, CRLF y todo campo entrecomillado")
    void toCsvEscribeBomComasYCrlf() {
        byte[] csv = SpreadsheetExporter.toCsv(
                List.of("Producto", "Precio"),
                List.of(Arrays.asList("Café", 10)));

        String texto = new String(csv, StandardCharsets.UTF_8);
        assertThat(texto.charAt(0)).isEqualTo('﻿');   // el BOM es lo que hace que Excel lea UTF-8
        assertThat(texto).contains("\"Producto\",\"Precio\"\r\n");
        assertThat(texto).contains("\"Café\",\"10\"\r\n");
    }

    @Test
    @DisplayName("toCsv: las comillas internas se duplican y los null salen como campo vacío")
    void toCsvEscapaComillasYNulls() {
        byte[] csv = SpreadsheetExporter.toCsv(
                List.of("Nombre"),
                List.of(Arrays.asList("Pantalla 24\" HD"), Arrays.asList((Object) null)));

        String texto = new String(csv, StandardCharsets.UTF_8);
        assertThat(texto).contains("\"Pantalla 24\"\" HD\"");
        assertThat(texto).contains("\"\"\r\n");
    }

    @Test
    @DisplayName("toCsv: cabeceras null y filas null producen sólo el BOM")
    void toCsvEsNullSafe() {
        String texto = new String(SpreadsheetExporter.toCsv(null, null), StandardCharsets.UTF_8);
        assertThat(texto).isEqualTo("﻿");
    }

    @Test
    @DisplayName("toCsv: una fila null dentro de la lista escribe una línea vacía, no rompe")
    void toCsvToleraFilaNull() {
        String texto = new String(
                SpreadsheetExporter.toCsv(null, Arrays.asList(null, Arrays.asList("a"))),
                StandardCharsets.UTF_8);
        assertThat(texto).isEqualTo("﻿\r\n\"a\"\r\n");
    }

    @Test
    @DisplayName("plano: quita etiquetas, resuelve entidades y colapsa espacios")
    void planoLimpiaElHtmlDelEditorDeTextoRico() {
        assertThat(SpreadsheetExporter.plano(null)).isEmpty();
        assertThat(SpreadsheetExporter.plano("<p>Hola <b>mundo</b></p>")).isEqualTo("Hola mundo");
        assertThat(SpreadsheetExporter.plano("Caf&eacute; &amp; t&eacute;")).isEqualTo("Café & té");
        assertThat(SpreadsheetExporter.plano("<ul><li>uno</li><li>dos</li></ul>")).isEqualTo("uno dos");
        assertThat(SpreadsheetExporter.plano("   ya   limpio   ")).isEqualTo("ya limpio");
    }
}
