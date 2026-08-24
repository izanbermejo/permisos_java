package ames.comercial.inventari.internal.application.query;

import ames.comercial.inventari.internal.application.query.ObtenirPecesImmobilitzades.ItemPecaImmobilitzada;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.Divisa;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Full de càlcul del llistat de peces immobilitzades. Substitueix el dbf {@code pcsinmov} que
 * generava el procediment {@code PcsInm01} de l'aplicació Delphi, i en manté els noms i l'ordre
 * de les columnes.
 */
@Service
public class ExportarPecesImmobilitzades {

    /** Format del període tal com el gravava el dbf: any i mes enganxats, p.ex. 202606. */
    private static final DateTimeFormatter FORMAT_ANYMES = DateTimeFormatter.ofPattern("yyyyMM");

    /** Noms de les columnes del dbf original, en el seu ordre. */
    private static final String[] COLUMNES = {
            "ANYMES", "ARTICLE", "CLIENT", "DELEGAT", "FAMILIA", "NOMART", "REFER", "NOMCLI",
            "NOMFAM", "DATAFAB", "DATAENV", "UNITATS", "PREU", "DIVISA", "IMPORT" };

    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle, decimalStyle, dateStyle;
    int rowNum;

    public byte[] exportar(YearMonth mes) throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Immobilitzats");
        var dades = BeanUtils.getBean(ObtenirPecesImmobilitzades.class).executar(mes);

        aplicaAmpladaColumnes(sheet);
        creaEstilCapsalera();
        creaEstilsNumerics();

        generaCapsalera(sheet);
        omplirDades(sheet, mes.format(FORMAT_ANYMES), dades);
        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 2500);   // ANYMES
        sheet.setColumnWidth(colNum++, 3000);   // ARTICLE
        sheet.setColumnWidth(colNum++, 3000);   // CLIENT
        sheet.setColumnWidth(colNum++, 5000);   // DELEGAT
        sheet.setColumnWidth(colNum++, 3000);   // FAMILIA
        sheet.setColumnWidth(colNum++, 12000);  // NOMART
        sheet.setColumnWidth(colNum++, 6000);   // REFER
        sheet.setColumnWidth(colNum++, 12000);  // NOMCLI
        sheet.setColumnWidth(colNum++, 8000);   // NOMFAM
        sheet.setColumnWidth(colNum++, 5000);   // DATAFAB
        sheet.setColumnWidth(colNum++, 5000);   // DATAENV
        sheet.setColumnWidth(colNum++, 4000);   // UNITATS
        sheet.setColumnWidth(colNum++, 4000);   // PREU
        sheet.setColumnWidth(colNum++, 3000);   // DIVISA
        sheet.setColumnWidth(colNum++, 4500);   // IMPORT
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

    private void omplirDades(XSSFSheet sheet, String anyMes, List<ItemPecaImmobilitzada> peces) {
        rowNum = 1;
        for (var p : peces) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(anyMes);
            row.createCell(colNum++).setCellValue(p.article().orElse(""));
            row.createCell(colNum++).setCellValue(p.clicod());
            row.createCell(colNum++).setCellValue(p.delegat().orElse(""));
            row.createCell(colNum++).setCellValue(p.codiFamilia().orElse(""));
            row.createCell(colNum++).setCellValue(p.denominacio().orElse(""));
            row.createCell(colNum++).setCellValue(p.referencia().orElse(""));
            row.createCell(colNum++).setCellValue(p.nomClient().orElse(""));
            row.createCell(colNum++).setCellValue(p.nomFamilia().orElse(""));
            setCellDateValue(row.createCell(colNum++), p.darreraEntrada());
            setCellDateValue(row.createCell(colNum++), p.darreraSortida());
            setCellIntegerValue(row.createCell(colNum++), p.unitats());
            setCellDecimalValue(row.createCell(colNum++), p.preu());
            row.createCell(colNum++).setCellValue(p.divisa().map(Divisa::symbol).orElse(""));
            setCellDecimalValue(row.createCell(colNum++), p.valoracio());
        }
    }

    private void setCellIntegerValue(XSSFCell cell, Long valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(intStyle);
    }

    private void setCellDecimalValue(XSSFCell cell, BigDecimal valor) {
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(decimalStyle);
    }

    /** Les peces que no han sortit mai (o que no tenen cap entrada) deixen la cel·la buida. */
    private void setCellDateValue(XSSFCell cell, Optional<LocalDate> valor) {
        if (valor.isPresent())
            cell.setCellValue(valor.get());
        cell.setCellStyle(dateStyle);
    }

}
