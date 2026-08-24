package ames.comercial.comandes;

import ames.comercial.comandes.internal.application.query.ObtenirDetallComandesArticle;
import ames.comercial.comandes.internal.application.query.ObtenirDetallComandesClient;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExportarDetallComandesArticle {
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle, decimalStyle, dateStyle;
    int rowNum;

    public byte[] exportar(String article, String client, LocalDate dataInici, LocalDate dataFi, boolean mostrarEliminades) throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Detalls");
        var dades = BeanUtils.getBean(ObtenirDetallComandesArticle.class).executar(article, client, dataInici, dataFi, mostrarEliminades);

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
        sheet.setColumnWidth(colNum++, 4000);   // comanda ames
        sheet.setColumnWidth(colNum++, 3000);   // numero
        sheet.setColumnWidth(colNum++, 6000);   // comanda client
        sheet.setColumnWidth(colNum++, 4000);   // clicod
        sheet.setColumnWidth(colNum++, 10000);  // nom client
        sheet.setColumnWidth(colNum++, 5000);   // tipus
        sheet.setColumnWidth(colNum++, 4000);   // codi fabrica
        sheet.setColumnWidth(colNum++, 6000);   // referencia
        sheet.setColumnWidth(colNum++, 5000);   // data solicitada
        sheet.setColumnWidth(colNum++, 6000);   // data prevista sortida
        sheet.setColumnWidth(colNum++, 7500);   // data prevista sortida interna
        sheet.setColumnWidth(colNum++, 6500);   // data confirmada fabrica
        sheet.setColumnWidth(colNum++, 4000);   // quantitat
        sheet.setColumnWidth(colNum++, 4500);   // quantitat servida
        sheet.setColumnWidth(colNum++, 5500);   // quantitat pendent
        sheet.setColumnWidth(colNum++, 3500);   // preu
        sheet.setColumnWidth(colNum++, 3000);   // divisa
        sheet.setColumnWidth(colNum++, 5500);   // comIntern
        sheet.setColumnWidth(colNum++, 5500);   // comClient
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
        String[] columnas = { I18N.getLiteral("comanda_ames"),
                I18N.getLiteral("numero"),
                I18N.getLiteral("comanda_client"),
                I18N.getLiteral("codi_client"),
                I18N.getLiteral("nom_client"),
                I18N.getLiteral("tipus"),
                I18N.getLiteral("codi_fabrica"),
                I18N.getLiteral("referencia"),
                I18N.getLiteral("data_solicitada"),
                I18N.getLiteral("data_prevista_sortida"),
                I18N.getLiteral("data_prevista_sortida_interna"),
                I18N.getLiteral("data_confirmada_fabrica"),
                I18N.getLiteral("quantitat"),
                I18N.getLiteral("quantitat_servida"),
                I18N.getLiteral("quantitat_pendent"),
                I18N.getLiteral("preu"),
                I18N.getLiteral("divisa"),
                I18N.getLiteral("comIntern"),
                I18N.getLiteral("comClient")};
        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void omplirDades(XSSFSheet sheet, List<ObtenirDetallComandesArticle.ObtenirDetallComandesArticleResponse> registres) {
        rowNum = 1;
        for (var r : registres) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            setCellIntegerValue(row.createCell(colNum++), r.comandaAMES());
            setCellIntegerValue(row.createCell(colNum++), r.numero());
            row.createCell(colNum++).setCellValue(r.comandaClient());
            row.createCell(colNum++).setCellValue(r.cliCod());
            row.createCell(colNum++).setCellValue(r.nomClient());
            row.createCell(colNum++).setCellValue(r.tipus());
            row.createCell(colNum++).setCellValue(r.codiFabrica());
            row.createCell(colNum++).setCellValue(r.referencia());
            setCellDateValue(row.createCell(colNum++), r.dataSolicitada());
            setCellDateValue(row.createCell(colNum++), r.dataPrevistaSortida());
            setCellDateValue(row.createCell(colNum++), r.dataPrevistaSortidaInterna());
            setCellDateValue(row.createCell(colNum++), r.dataConfirmadaFabrica());
            setCellIntegerValue(row.createCell(colNum++), r.quantitat());
            setCellIntegerValue(row.createCell(colNum++), r.quantitatServida());
            setCellIntegerValue(row.createCell(colNum++), r.quantitatPendent());
            setCellDecimalValue(row.createCell(colNum++), r.preu());
            row.createCell(colNum++).setCellValue(r.divisa());
            row.createCell(colNum++).setCellValue(r.comIntern().orElse(""));
            row.createCell(colNum++).setCellValue(r.comClient().orElse(""));
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

    private void setCellDateValue(XSSFCell cell, LocalDate valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(dateStyle);
    }

    private void setCellDateValue(XSSFCell cell, Optional<LocalDate> valor) {
        if (valor != null && valor.isPresent()) {
            LocalDate date = valor.get();
            cell.setCellValue(date);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(dateStyle);
    }

}
