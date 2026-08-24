package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.application.query.ObtenirConfiguracionsAviExp;
import ames.comercial.server.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportarConfiguracionsAviExp {
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle;
    int rowNum;

    public byte[] exportar() throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Configuracions");
        var dades = BeanUtils.getBean(ObtenirConfiguracionsAviExp.class).executar();

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
        sheet.setColumnWidth(colNum++, 4000);   // codiProveidor
        sheet.setColumnWidth(colNum++, 7000);   // avi_exp
        sheet.setColumnWidth(colNum++, 5500);   // Afegir Comanda
        sheet.setColumnWidth(colNum++, 5500);   // Estat client
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
        intStyle = workbook.createCellStyle();
        intStyle.setDataFormat(format.getFormat("#,##0"));
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = {
            "Codi client",
            "Nom client",
            "Codi proveidor",
            "Avi/Exp",
            "Afegir Comanda?",
            "Estat del client"
        };

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
            row.createCell(colNum++).setCellValue(r.getString("codiProveidor"));
            row.createCell(colNum++).setCellValue(r.getBoolean("volAviExp"));
            row.createCell(colNum++).setCellValue(r.getBoolean("volEnviarLG"));
            row.createCell(colNum++).setCellValue(mapEstat(r.optString("estat", "")));
        }
    }

    private String mapEstat(String estat) {
        return switch (estat) {
            case "A" -> "ACTIU";
            case "I" -> "INACTIU";
            case "E" -> "ELIMINAT";
            default -> "";
        };
    }

}
