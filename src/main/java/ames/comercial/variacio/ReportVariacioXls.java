package ames.comercial.variacio;

import ames.comercial.advantage.internal.ObtenirCanviDivisa;
import ames.comercial.server.RequestThread;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportVariacioXls {

    private NamedParameterJdbcTemplate jdbc;
    Divisa divisaReport;
    LocalDate dataInici;
    LocalDate dataFins;
    List<String> fabriques;
    LocalDate dataSolicitadaInici;
    LocalDate dataSolicitadaFins;

    private Map<LocalDate, BigDecimal> canvisEurUsd;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    CellStyle headerStyle, intStyle, decimalStyle;
    int rowNum;

    public ReportVariacioXls(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public byte[] generar (LocalDate dataInici, LocalDate dataFins, Divisa divisa, List<String> fabriques,
                           LocalDate dataSolicitadaInici, LocalDate dataSolicitadaFins) throws IOException {
        // Assignació paràmetres
        // Només es pot demanar el report en EUR o USD
        if (!(Divisa.EURO.equals(divisa) || Divisa.DOLAR.equals(divisa)))
            throw new AppException("Report només disponible en EUR o USD");
        this.divisaReport = divisa;
        this.dataInici = dataInici;
        this.dataFins = dataFins;
        this.fabriques = fabriques;
        this.dataSolicitadaInici = dataSolicitadaInici;
        this.dataSolicitadaFins = dataSolicitadaFins;
        // Construcció del report
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Var");
        // S'aplica l'ample a les columnes
        aplicaAmpladaColumnes(sheet);
        // S'aplica l'estil a la capçalera
        creaEstilCapsalera(sheet);
        // Creació d'estils numèrics
        creaEstilsNumerics(sheet);
        // Creació fila capçalera
        generaCapsalera(sheet);
        // Obtenció dels canvis de EUR a USD
        canvisEurUsd = new ObtenirCanviDivisa().get(Divisa.DOLAR, Divisa.EURO, dataInici, dataFins);
        // Obtenció de dades
        var dades = obtenirDades();
        omplirDades(sheet, dades);
        // Fila resum
        omplirResum(sheet);
        // Preparació de la resposta
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 2000);   // Codi client
        sheet.setColumnWidth(colNum++, 10000);  // Nom client
        sheet.setColumnWidth(colNum++, 3000);  // Unitats
        sheet.setColumnWidth(colNum++, 5000);   // Import
        sheet.setColumnWidth(colNum++, 5000);   // Kgs
        sheet.setColumnWidth(colNum++, 5000);   // Euros/Kgs
        sheet.setColumnWidth(colNum++, 2000);   // Codi fàbrica
        sheet.setColumnWidth(colNum++, 3000);   // Comanda
        sheet.setColumnWidth(colNum++, 3000);   // Linia
        sheet.setColumnWidth(colNum++, 5500);   // Articleclient
    }

    private void creaEstilCapsalera(XSSFSheet sheet) {
        headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void creaEstilsNumerics(XSSFSheet sheet) {
        DataFormat format = workbook.createDataFormat();
        // Format per a enters
        intStyle = workbook.createCellStyle();
        intStyle.setDataFormat(format.getFormat("#,##0"));
        // Format per a decimals
        decimalStyle = workbook.createCellStyle();
        decimalStyle.setDataFormat(format.getFormat("#,##0.00"));
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"Codi client",
                "Nom client",
                "Unitats",
                "Import " + divisaReport.symbol(),
                "Kgs",
                "Euros/Kgs",
                "Fàbrica",
                "Comanda",
                "Linia",
                "Articleclient"};
        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
        headerRow.createCell(indexCol).setCellValue(buildData(dataInici) + " - " + buildData(dataFins) + ": " + String.join(",", fabriques));
    }

    private String buildData(LocalDate data) {
        // Obtenim el nom del mes en l'idioma passat per paràmetres
        var monthWords = data.getMonth().getDisplayName(TextStyle.SHORT, RequestThread.idioma()).split(" ");
        var monthName = monthWords[monthWords.length - 1];
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        return String.format("%d %s %d", data.getDayOfMonth(), monthName, data.getYear());
    }

    private void omplirDades(XSSFSheet sheet, List<RecordVariacio> registres) {
        rowNum = 1;
        for (var r : registres) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(r.codiClient());
            row.createCell(colNum++).setCellValue(r.descClient());
            setCellIntegerValue(row.createCell(colNum++), r.unitats());
            setCellDecimalValue(row.createCell(colNum++), r.imp());
            setCellDecimalValue(row.createCell(colNum++), r.pes());
            // INICI fórmula (euros / kgs)
            String eurosRef = new CellReference(row.getRowNum(), colNum-2).formatAsString();
            String kgsRef = new CellReference(row.getRowNum(), colNum-1).formatAsString();
            Cell formulaCell = row.createCell(colNum++);
            formulaCell.setCellFormula(eurosRef + "/" + kgsRef);
            formulaCell.setCellStyle(decimalStyle);
            // FI fórmula (euros / kgs)
            row.createCell(colNum++).setCellValue(r.fabrica());
            row.createCell(colNum++).setCellValue(r.comandaFormat());
            row.createCell(colNum++).setCellValue(r.numeroFormat());
            row.createCell(colNum++).setCellValue(r.articleClient());
        }
    }

    private List<RecordVariacio> obtenirDades() {
        var params = new MapSqlParameterSource();
        params.addValue("data_inicial", dataInici);
        params.addValue("data_final", dataFins);
        params.addValue("fabrica", fabriques.toArray(new String[0]), Types.ARRAY);
        params.addValue("data_solicitada_inicial", dataSolicitadaInici);
        params.addValue("data_solicitada_final", dataSolicitadaFins);
        RowMapper<RecordVariacio> mapper = Divisa.EURO.equals(divisaReport) ? this::mapRecordsEur : this::mapRecordsUsd;
        return jdbc.query("""
                SELECT
                    client_codi, client_desc,
                    MIN(data_variacio) as data_variacio,
                    sum(quantitat)::bigint AS quantitat,
                    sum("import") as import,
                    LEFT(divisa, 3) as divisaBase,
                    sum(ROUND("import"*factor_eur, 2)) AS euros,
                    sum(ROUND(pes*quantitat/1000, 2)) AS kgs,
                    fabrica, comanda, numero, codi_fabrica, clicod
                FROM comandes.variacio v
                WHERE "valid" AND processat
                	AND data_variacio BETWEEN :data_inicial AND :data_final
                	AND fabrica = any(:fabrica)
                	AND data_solicitada BETWEEN :data_solicitada_inicial AND :data_solicitada_final
                GROUP BY client_codi, client_desc, fabrica, comanda, numero, codi_fabrica, clicod, LEFT(divisa, 3)
                HAVING SUM(quantitat)<>0 OR sum("import")<>0
                ORDER BY client_desc, codi_fabrica, comanda, numero;
                """, params, mapper);
    }

    private RecordVariacio mapRecordsEur(ResultSet rs, int rowNum) throws SQLException {
        return RecordVariacioImpl.builder()
                .codiClient(rs.getString("client_codi"))
                .descClient(rs.getString("client_desc"))
                .unitats(rs.getLong("quantitat"))
                .imp(rs.getBigDecimal("euros"))
                .pes(rs.getBigDecimal("kgs"))
                .fabrica(rs.getString("fabrica"))
                .comanda(rs.getLong("comanda"))
                .numero(rs.getLong("numero"))
                .codiFabrica(rs.getString("codi_fabrica"))
                .clicod(rs.getString("clicod"))
                .build();
    }

    private RecordVariacio mapRecordsUsd(ResultSet rs, int rowNum) throws SQLException {
        var divisaLinia = Divisa.getBySymbol(rs.getString("divisaBase"));
        var factorDivisa = BigDecimal.ONE;
        if (!divisaReport.symbol().equals(divisaLinia.symbol())) {
            if (Divisa.EURO.equals(divisaLinia)) {
                factorDivisa = Objects.requireNonNull(canvisEurUsd.get(rs.getDate("data_variacio").toLocalDate()),
                        "No hi ha canvi de USD a EUR per la data: " + rs.getDate("data_variacio"));
                factorDivisa= BigDecimal.ONE.divide(factorDivisa, 5, RoundingMode.HALF_UP);
            } else {
                throw new AppException("Hi han línies amb divisa diferent de EUR i USD (" + divisaLinia.symbol() + ")");
            }
        }
        return RecordVariacioImpl.builder()
                .codiClient(rs.getString("client_codi"))
                .descClient(rs.getString("client_desc"))
                .unitats(rs.getLong("quantitat"))
                .imp(rs.getBigDecimal("import").multiply(factorDivisa).setScale(2, RoundingMode.HALF_UP))
                .pes(rs.getBigDecimal("kgs"))
                .fabrica(rs.getString("fabrica"))
                .comanda(rs.getLong("comanda"))
                .numero(rs.getLong("numero"))
                .codiFabrica(rs.getString("codi_fabrica"))
                .clicod(rs.getString("clicod"))
                .build();
    }

    private void omplirResum(XSSFSheet sheet) {
        var rowTotal = sheet.createRow(rowNum);
        Cell cell = rowTotal.createCell(0);
        cell.setCellValue("TOTAL");
        cell.setCellStyle(headerStyle);
        // Quantitat total
        var letraColumna = CellReference.convertNumToColString(2);
        String formula = String.format("SUM(%s2:%s%d)", letraColumna, letraColumna, rowNum);
        cell = rowTotal.createCell(2);
        if (rowNum > 1) {
            cell.setCellFormula(formula);
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(intStyle);
        // Import total
        letraColumna = CellReference.convertNumToColString(3);
        formula = String.format("SUM(%s2:%s%d)", letraColumna, letraColumna, rowNum);
        cell = rowTotal.createCell(3);
        if (rowNum > 1) {
            cell.setCellFormula(formula);
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(decimalStyle);
        // Kgs total
        letraColumna = CellReference.convertNumToColString(4);
        formula = String.format("SUM(%s2:%s%d)", letraColumna, letraColumna, rowNum);
        cell = rowTotal.createCell(4);
        if (rowNum > 1) {
            cell.setCellFormula(formula);
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(decimalStyle);
    }

    private void setCellIntegerValue(XSSFCell cell, Long valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(intStyle);
    }

    private void setCellDecimalValue(XSSFCell cell, BigDecimal valor) {
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(decimalStyle);
    }

    @JsonDeserialize(builder = RecordVariacioImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RecordVariacio {
        String codiClient();
        String descClient();
        Long unitats();
        BigDecimal imp();
        BigDecimal pes();
        String fabrica();
        Long comanda();
        Long numero();
        String codiFabrica();
        String clicod();

        @Derived
        default String comandaFormat() { return String.format("%07d", comanda()); }

        @Derived
        default String numeroFormat() { return String.format("%04d", numero()); }

        @Derived
        default String articleClient() { return codiFabrica()+clicod(); }
    }

}
