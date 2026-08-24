package ames.comercial.inventari.internal.application.query;

import ames.comercial.inventari.internal.application.query.ObtenirFerralla.ItemFerralla;
import ames.comercial.inventari.internal.application.query.ObtenirFerralla.Periode;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.Divisa;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Full de càlcul del llistat de ferralla. Substitueix el dbf {@code lfer} que generava el
 * procediment {@code Ferralla01} de l'aplicació Delphi, i en manté els noms i l'ordre de les
 * columnes de la A a la I.
 * <p>
 * Les columnes {@code COST} i {@code DIVCOST} són una millora respecte del dbf, que només gravava
 * l'import ja convertit: són els dos valors d'{@code artcli} amb què s'ha calculat {@code IMPORTS},
 * de manera que l'usuari en pot veure la procedència i detectar de seguida un cost en una divisa
 * diferent de l'euro.
 */
@Service
public class ExportarFerralla {

    /** Noms de les columnes: les nou del dbf original, en el seu ordre, i el cost al final. */
    private static final String[] COLUMNES = {
            "DATAIN", "DATAFI", "ARTCLI", "REFER", "DESCRIP", "NOMCLI", "UNITATS", "IMPORTS", "DIV",
            "COST", "DIVCOST" };

    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle, decimalStyle, costStyle, dateStyle;
    int rowNum;

    public byte[] exportar(YearMonth mes) throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Ferralla");
        var dades = BeanUtils.getBean(ObtenirFerralla.class).executar(mes);
        var periode = ObtenirFerralla.periodeDe(mes);

        aplicaAmpladaColumnes(sheet);
        creaEstilCapsalera();
        creaEstilsNumerics();

        generaCapsalera(sheet);
        omplirDades(sheet, periode, dades);
        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 5000);   // DATAIN
        sheet.setColumnWidth(colNum++, 5000);   // DATAFI
        sheet.setColumnWidth(colNum++, 5000);   // ARTCLI
        sheet.setColumnWidth(colNum++, 6000);   // REFER
        sheet.setColumnWidth(colNum++, 12000);  // DESCRIP
        sheet.setColumnWidth(colNum++, 12000);  // NOMCLI
        sheet.setColumnWidth(colNum++, 4000);   // UNITATS
        sheet.setColumnWidth(colNum++, 4500);   // IMPORTS
        sheet.setColumnWidth(colNum++, 3000);   // DIV
        sheet.setColumnWidth(colNum++, 4000);   // COST
        sheet.setColumnWidth(colNum++, 3000);   // DIVCOST
    }

    private void creaEstilCapsalera() {
        headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void creaEstilsNumerics() {
        DataFormat format = workbook.createDataFormat();
        // Format per a enters
        intStyle = workbook.createCellStyle();
        intStyle.setDataFormat(format.getFormat("#,##0"));
        // Format per a decimals
        decimalStyle = workbook.createCellStyle();
        decimalStyle.setDataFormat(format.getFormat("#,##0.00"));
        // Format per al cost unitari, que sol tenir més de dos decimals
        costStyle = workbook.createCellStyle();
        costStyle.setDataFormat(format.getFormat("#,##0.0000"));
        // Format per a data
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int indexCol = 0; indexCol < COLUMNES.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(COLUMNES[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Les dues dates del període es repeteixen a cada fila, com feia el dbf. {@code DIV} és sempre
     * l'euro perquè {@link ItemFerralla#valoracio()} ja arriba convertida; {@code COST} i
     * {@code DIVCOST}, en canvi, són el valor cru de la cache, i per tant l'una i l'altra divisa
     * poden ser diferents. Amb una divisa de cost de tipus cèntim ({@code EUR%}) el cost va en
     * cèntims i {@code IMPORTS} ja incorpora la divisió per 100.
     */
    private void omplirDades(XSSFSheet sheet, Periode periode, List<ItemFerralla> ferralles) {
        rowNum = 1;
        for (var f : ferralles) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            setCellDateValue(row.createCell(colNum++), periode.dataInici());
            setCellDateValue(row.createCell(colNum++), periode.dataFi());
            row.createCell(colNum++).setCellValue(f.articleClient());
            row.createCell(colNum++).setCellValue(f.referencia().orElse(""));
            row.createCell(colNum++).setCellValue(f.denominacio().orElse(""));
            row.createCell(colNum++).setCellValue(f.nomClient().orElse(""));
            setCellIntegerValue(row.createCell(colNum++), f.unitats());
            setCellDecimalValue(row.createCell(colNum++), f.valoracio());
            row.createCell(colNum++).setCellValue(Divisa.EURO.symbol());
            setCellCostValue(row.createCell(colNum++), f.cost());
            row.createCell(colNum++).setCellValue(f.divisaCost().map(Divisa::symbol).orElse(""));
        }
    }

    private void setCellIntegerValue(XSSFCell cell, long valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(intStyle);
    }

    private void setCellDecimalValue(XSSFCell cell, BigDecimal valor) {
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(decimalStyle);
    }

    private void setCellCostValue(XSSFCell cell, BigDecimal valor) {
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(costStyle);
    }

    private void setCellDateValue(XSSFCell cell, LocalDate valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(dateStyle);
    }

}
