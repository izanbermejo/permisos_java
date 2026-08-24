package ames.comercial.comandes.internal.application.command;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.immutables.value.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Component
public class DescarregaStockSeguretat {

    private NamedParameterJdbcTemplate jdbcAmes;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    int rowNum;
    CellStyle intStyle, headerStyle, dateStyle;
    public DescarregaStockSeguretat(NamedParameterJdbcTemplate jdbcAmes){
        this.jdbcAmes = jdbcAmes;
    }

    public byte[] executar() throws IOException {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("StockSeg");
        aplicaAmpladaColumnes(sheet);
        creaEstils();
        generaCapsalera(sheet);
        var dades = obtenirDades();
        omplirDades(sheet, dades);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void creaEstils() {
        headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat format = workbook.createDataFormat();
        // Format per a enters
        intStyle = workbook.createCellStyle();
        intStyle.setDataFormat(format.getFormat("#,##0"));

        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 3800);
        sheet.setColumnWidth(colNum++, 2800);
        sheet.setColumnWidth(colNum++, 2500);
        sheet.setColumnWidth(colNum++, 3000);
        sheet.setColumnWidth(colNum++, 2800);
        sheet.setColumnWidth(colNum++, 15000);
        sheet.setColumnWidth(colNum++, 10000);
        sheet.setColumnWidth(colNum++, 5000);
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = {
                "Articleclient",
                "Data stock",
                "Quantitat",
                "Tipus A/C",
                "Client",
                "Nom client",
                "Denominació",
                "Referència"
        };

        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    private List<RecordStockSeguretat> obtenirDades() {
        var params = new MapSqlParameterSource();
        RowMapper<RecordStockSeguretat> mapper = this::stockSeguretatMapper;
        return jdbcAmes.query("""
                    SELECT
                        ac.codi_fabrica AS codiFabrica,
                        ac.clicod AS clicod,
                        lc.data_solicitada AS data,
                        lc.quantitat AS quantitat,
                        lc.tipus AS tipus,
                        cc.nom AS nomClient,
                        ac.denominacio AS denominacio,
                        ac.referencia AS referencia
                    FROM comandes.linia_comanda lc
                    LEFT JOIN "cache".cache_article_client ac
                        ON ac.artint = lc.artint
                        AND ac.clicod = lc.clicod
                    LEFT JOIN "cache".cache_client cc
                        ON cc.clicod = ac.clicod
                    WHERE lc.actual
                      AND lc.tipus IN ('STOCK_SEG_AMES', 'STOCK_SEG_CLIENT')
                    ORDER BY data_solicitada ASC;
                """, params, mapper);
    }

    private void omplirDades(XSSFSheet sheet, List<RecordStockSeguretat> registres) {
        rowNum = 1;
        for (var r : registres) {
            int colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(r.articleclient());
            var dateCell = row.createCell(colNum);
            dateCell.setCellStyle(dateStyle);
            dateCell.setCellValue(r.data());
            colNum++;
            var intCell = row.createCell(colNum);
            intCell.setCellStyle(intStyle);
            intCell.setCellValue(r.quantitat());
            colNum++;
            row.createCell(colNum++).setCellValue(r.stockClient());
            row.createCell(colNum++).setCellValue(r.clicod());
            row.createCell(colNum++).setCellValue(r.nomClient());
            row.createCell(colNum++).setCellValue(r.denominacio());
            row.createCell(colNum++).setCellValue(r.referencia());
        }
    }

    private RecordStockSeguretat stockSeguretatMapper(ResultSet rs, int rowNum) throws SQLException {
        return RecordStockSeguretatImpl.builder()
                .codiFabrica(rs.getString("codiFabrica"))
                .clicod(rs.getString("clicod"))
                .data(rs.getDate("data"))
                .quantitat(rs.getLong("quantitat"))
                .tipus(rs.getString("tipus"))
                .nomClient(rs.getString("nomClient"))
                .denominacio(rs.getString("denominacio"))
                .referencia(rs.getString("referencia"))
                .build();
    }

    @JsonDeserialize(builder = RecordStockSeguretatImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RecordStockSeguretat {

        String codiFabrica();
        String clicod();
        Date data();
        Long quantitat();
        String tipus();
        String nomClient();
        String denominacio();
        String referencia();

        @Value.Derived
        default String articleclient() {
            return codiFabrica() + clicod();
        }

        @Value.Derived
        default String stockClient() {
            return "STOCK_SEG_CLIENT".equalsIgnoreCase(tipus()) ? "C" :
                    "STOCK_SEG_AMES".equalsIgnoreCase(tipus()) ? "A" : "";
        }
    }
}


