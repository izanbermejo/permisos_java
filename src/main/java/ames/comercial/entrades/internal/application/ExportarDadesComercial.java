package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.application.query.BuscarEntradesComercial;
import ames.comercial.entrades.internal.application.query.BuscarEntradesComercial.*;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.I18N;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExportarDadesComercial {
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle, decimalStyle, dateTimeStyle;
    int rowNum;

    public byte[] exportar(BuscarEntradesComercialRequest request) throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Entrades");
        var dades = BeanUtils.getBean(BuscarEntradesComercial.class).executar(request);

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
        sheet.setColumnWidth(colNum++, 4000);   // id
        sheet.setColumnWidth(colNum++, 3000);   // article
        sheet.setColumnWidth(colNum++, 3000);   // client
        sheet.setColumnWidth(colNum++, 4000);   // magatzem
        sheet.setColumnWidth(colNum++, 4000);   // fabrica
        sheet.setColumnWidth(colNum++, 4000);   // quantitat
        sheet.setColumnWidth(colNum++, 5000);   // quantitat caixa
        sheet.setColumnWidth(colNum++, 5000);   // of
        sheet.setColumnWidth(colNum++, 4000);   // pes premsat
        sheet.setColumnWidth(colNum++, 4000);   // pes final
        sheet.setColumnWidth(colNum++, 5500);   // data entrada
        sheet.setColumnWidth(colNum++, 5500);   // data processament
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
        dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm:ss"));
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = { I18N.getLiteral("id_entrada"),
                I18N.getLiteral("article"),
                I18N.getLiteral("client"),
                I18N.getLiteral("magatzem"),
                I18N.getLiteral("fabrica"),
                I18N.getLiteral("quantitat"),
                I18N.getLiteral("quantitat_caixa"),
                I18N.getLiteral("of"),
                I18N.getLiteral("pes_premsat"),
                I18N.getLiteral("pes_final"),
                I18N.getLiteral("data_entrada"),
                I18N.getLiteral("data_processament")};
        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void omplirDades(XSSFSheet sheet, List<BuscarEntradesComercialResponse> registres) {
        rowNum = 1;
        for (var r : registres) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(r.idEntradaFabrica());
            row.createCell(colNum++).setCellValue(r.article());
            row.createCell(colNum++).setCellValue(r.client());
            row.createCell(colNum++).setCellValue(r.magatzem());
            row.createCell(colNum++).setCellValue(r.fabrica());
            setCellIntegerValue(row.createCell(colNum++), r.quantitat());
            setCellIntegerValue(row.createCell(colNum++), r.quantitatCaixa());
            setCellIntegerValue(row.createCell(colNum++), r.of());
            setCellDecimalValue(row.createCell(colNum++), r.pesPremsat());
            setCellDecimalValue(row.createCell(colNum++), r.pesFinal());
            setCellDateTimeValue(row.createCell(colNum++), r.dataAlta());
            setCellDateTimeValue(row.createCell(colNum++), r.dataProcessat());
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

    private void setCellDateTimeValue(XSSFCell cell, LocalDateTime valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(dateTimeStyle);
    }

    private void setCellDateTimeValue(XSSFCell cell, Optional<LocalDateTime> valor) {
        if (valor != null && valor.isPresent()) {
            LocalDateTime date = valor.get();
            cell.setCellValue(date);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(dateTimeStyle);
    }

}
