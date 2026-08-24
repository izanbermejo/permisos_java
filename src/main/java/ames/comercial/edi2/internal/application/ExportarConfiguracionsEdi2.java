package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.application.query.ObtenirConfiguracionsEDI2;
import ames.comercial.server.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportarConfiguracionsEdi2 {
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle /*, decimalStyle, dateTimeStyle */;
    int rowNum;

    public byte[] exportar() throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Configuracions");
        var dades = BeanUtils.getBean(ObtenirConfiguracionsEDI2.class).executar();

        aplicaAmpladaColumnes(sheet);
        creaEstilCapsalera();
        creaEstilsNumerics();

        generaCapsalera(sheet);
        omplirDades(sheet, dades);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 3000);   // codiClient
        sheet.setColumnWidth(colNum++, 12500);  // nomClient
        sheet.setColumnWidth(colNum++, 4000);   // tipusMissatge
        sheet.setColumnWidth(colNum++, 4000);   // estat client
        sheet.setColumnWidth(colNum++, 5500);   // estat configuració
        sheet.setColumnWidth(colNum++, 4500);   // fermOrientatiu
        sheet.setColumnWidth(colNum++, 7000);   // edibox
        sheet.setColumnWidth(colNum++, 5000);   // nad02
        sheet.setColumnWidth(colNum++, 4000);   // codiProveidor
        sheet.setColumnWidth(colNum++, 4000);   // estrategiaEdi
        sheet.setColumnWidth(colNum++, 4000);   // llocEntrega
        sheet.setColumnWidth(colNum++, 4000);   // diesSortida
        sheet.setColumnWidth(colNum++, 3500);   // diesRestar
        sheet.setColumnWidth(colNum++, 5500);   // considerarAlbarans
        sheet.setColumnWidth(colNum++, 5500);   // considerarDuesDates
        sheet.setColumnWidth(colNum++, 3000);   // diesTall
        sheet.setColumnWidth(colNum++, 5500);   // tipusDocumentEdi
        sheet.setColumnWidth(colNum++, 10000);  // comentaris
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
//        decimalStyle = workbook.createCellStyle();
//        decimalStyle.setDataFormat(format.getFormat("#,##0.00"));
        // Format per a data
//        dateTimeStyle = workbook.createCellStyle();
//        dateTimeStyle.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm:ss"));
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = { "Codi client",
                "Nom client",
                "Tipus missatge",
                "Estat del client",
                "Estat de la configuració",
                "Ferm/Orientatiu",
                "Edibox",
                "Nad02",
                "Codi proveidor",
                "Estrategia edi",
                "Lloc d'entrega",
                "Dies de sortida",
                "Dies a restar",
                "Considerar albarans",
                "Considerar dues dates",
                "Dies de tall",
                "Tipus document edi",
                "Comentaris" };

        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void omplirDades(XSSFSheet sheet, List<JSONObject> registres) {
        rowNum = 1;
        for (var r : registres) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(r.getString("codiClient"));
            row.createCell(colNum++).setCellValue(r.getString("nomClient"));
            row.createCell(colNum++).setCellValue(r.getString("tipusMissatge"));
            row.createCell(colNum++).setCellValue(mapEstat(r.optString("estat", "")));
            row.createCell(colNum++).setCellValue(r.getBoolean("isActiu") ? "ACTIVA" : "INACTIVA");
            row.createCell(colNum++).setCellValue(r.getString("fermOrientatiu"));
            row.createCell(colNum++).setCellValue(r.getString("ediBox"));
            row.createCell(colNum++).setCellValue(r.getString("nad02"));
            row.createCell(colNum++).setCellValue(r.getString("codiProveidor"));
            setCellIntegerValue(row.createCell(colNum++), r.getLong("estrategiaEdi"));
            row.createCell(colNum++).setCellValue(!r.optJSONArray("llocEntrega").isEmpty()
                    ? r.optJSONArray("llocEntrega").toString()
                    : "");
            row.createCell(colNum++).setCellValue(!r.getJSONObject("informacioSortida").optJSONArray("diesSortida").isEmpty()
                    ? r.getJSONObject("informacioSortida").optJSONArray("diesSortida").toString()
                    : "");
            setCellIntegerValue(row.createCell(colNum++), r.getJSONObject("informacioSortida").getLong("diesRestar"));
            row.createCell(colNum++).setCellValue(r.getBoolean("considerarAlbarans"));
            row.createCell(colNum++).setCellValue(r.getBoolean("considerarDuesDates"));
            setCellIntegerValue(row.createCell(colNum++), r.getLong("diesTall"));
            row.createCell(colNum++).setCellValue(r.optString("tipusDocumentEdi"));
            row.createCell(colNum++).setCellValue(r.optString("comentaris"));

//            row.createCell(colNum++).setCellValue(r.fabrica());
//            setCellIntegerValue(row.createCell(colNum++), r.of());
//            setCellDecimalValue(row.createCell(colNum++), r.pesFinal());
//            setCellDateTimeValue(row.createCell(colNum++), r.dataProcessat());
        }
    }

    private void setCellIntegerValue(XSSFCell cell, Long valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(intStyle);
    }

//    private void setCellDecimalValue(XSSFCell cell, BigDecimal valor) {
//        cell.setCellValue(valor.doubleValue());
//        cell.setCellStyle(decimalStyle);
//    }

//    private void setCellDateTimeValue(XSSFCell cell, LocalDateTime valor) {
//        cell.setCellValue(valor);
//        cell.setCellStyle(dateTimeStyle);
//    }

//    private void setCellDateTimeValue(XSSFCell cell, Optional<LocalDateTime> valor) {
//        if (valor != null && valor.isPresent()) {
//            LocalDateTime date = valor.get();
//            cell.setCellValue(date);
//        } else {
//            cell.setCellValue("");
//        }
//        cell.setCellStyle(dateTimeStyle);
//    }

    private String mapEstat(String estat) {
        return switch (estat) {
            case "A" -> "ACTIU";
            case "I" -> "INACTIU";
            case "E" -> "ELIMINAT";
            default -> "";
        };
    }

}
