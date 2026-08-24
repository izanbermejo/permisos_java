package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.IObtenirDestinsTransport;
import ames.comercial.advantage.IObtenirTransportistesAds;
import ames.comercial.advantage.internal.ObtenirDestinsAds;
import ames.comercial.advantage.internal.ObtenirTransportistesAds;
import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans;
import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans.ObtenirUltimsAlbaransResponse.ObtenirUltimsAlbaransResponseAlbarans;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.I18N;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.KeyArticleClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExportarAlbarans {
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    CellStyle headerStyle, intStyle, dateTimeStyle, diferentFormaEnviamentStyle;
    int rowNum;
    List<ObtenirDestinsAds.DestiAds> destinsTransport;
    List<ObtenirTransportistesAds.TransportistaAds> transportistes;


    public byte[] exportar(KeyArticleClient articleClient, String codiAlbaraFacturaRefereciaTransit, long acumulatSegonsClient) throws IOException {

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Albarans");
        var response = BeanUtils.getBean(ObtenirUltimsAlbarans.class).executar(articleClient, codiAlbaraFacturaRefereciaTransit, acumulatSegonsClient);
        var dades = response.albarans();

        destinsTransport = BeanUtils.getBean(IObtenirDestinsTransport.class).all();
        transportistes = BeanUtils.getBean(IObtenirTransportistesAds.class).all();

        aplicaAmpladaColumnes(sheet);
        creaEstilCapsalera();
        creaEstilsNumerics();
        creaEstilsEspecials();

        generaCapsalera(sheet);
        omplirDades(sheet, dades);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    private void aplicaAmpladaColumnes(XSSFSheet sheet) {
        int colNum = 0;
        sheet.setColumnWidth(colNum++, 3000);   // albarà
        sheet.setColumnWidth(colNum++, 3000);   // factura
        sheet.setColumnWidth(colNum++, 4000);   // data
        sheet.setColumnWidth(colNum++, 15000);  // forma enviament
        sheet.setColumnWidth(colNum++, 12000);  // transportista
        sheet.setColumnWidth(colNum++, 4000);   // quantitat
        sheet.setColumnWidth(colNum++, 4000);   // acumulat ames
        sheet.setColumnWidth(colNum++, 3000);   // entregat
        sheet.setColumnWidth(colNum++, 7000);   // nota
        sheet.setColumnWidth(colNum++, 3000);   // albara especial
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
        // Format per a data
        dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));
    }

    private void creaEstilsEspecials() {
        diferentFormaEnviamentStyle = workbook.createCellStyle();
        Font diferentFormaEnviamentFont = workbook.createFont();
        diferentFormaEnviamentFont.setBold(true);
        diferentFormaEnviamentFont.setColor(IndexedColors.GREEN.getIndex());
        diferentFormaEnviamentStyle.setFont(diferentFormaEnviamentFont);
    }

    private void generaCapsalera(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] columnas = { I18N.getLiteral("albara"),
                I18N.getLiteral("factura"),
                I18N.getLiteral("data"),
                I18N.getLiteral("forma_enviament"),
                I18N.getLiteral("transportista"),
                I18N.getLiteral("quantitat"),
                I18N.getLiteral("acumulat"),
                I18N.getLiteral("entregat"),
                I18N.getLiteral("nota"),
                I18N.getLiteral("albara_especial") };

        int indexCol = 0;
        for (indexCol = 0; indexCol < columnas.length; indexCol++) {
            Cell cell = headerRow.createCell(indexCol);
            cell.setCellValue(columnas[indexCol]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void omplirDades(XSSFSheet sheet, List<ObtenirUltimsAlbaransResponseAlbarans> albarans) {
        rowNum = 1;
        for (var a : albarans) {
            var colNum = 0;
            var row = sheet.createRow(rowNum++);
            row.createCell(colNum++).setCellValue(a.id().codi());
            row.createCell(colNum++).setCellValue(a.factures().orElse(""));
            setCellDateTimeValue(row.createCell(colNum++), a.data());
            Cell cellFormaEnviament = row.createCell(colNum++);
                cellFormaEnviament.setCellValue(formaEnviament(a.formaEnviament(), a.incoterm(), a.desti()));
                if (!a.isMateixaFormaEnviamentHabitual()) cellFormaEnviament.setCellStyle(diferentFormaEnviamentStyle);
            row.createCell(colNum++).setCellValue(transportista(a.transportista().orElse("")));
            setCellIntegerValue(row.createCell(colNum++), a.quantitat());
            setCellIntegerValue(row.createCell(colNum++), a.acumulat());
            row.createCell(colNum++).setCellValue(a.isEntregat() ? "SÍ" : "NO");
            row.createCell(colNum++).setCellValue(a.nota().orElse(""));
            row.createCell(colNum++).setCellValue(a.albaraEspecial().orElse(""));
        }
    }

    private String formaEnviament(FormaEnviament formaEnviament, Incoterm incoterm, String desti) {
        String destiTransport = destinsTransport.stream()
                .filter(d -> d.codi().equals(desti))
                .findFirst()
                .map(d -> d.codi() + " - " + d.nom())
                .orElse("");
        return I18N.getLiteral(formaEnviament.toString()) + " • " + incoterm + " • " + destiTransport;
    }

    private String transportista(String transportista) {
        return transportistes.stream()
                .filter(d -> d.codi().equals(transportista))
                .findFirst()
                .map(d -> d.codi() + " - " + d.descripcio())
                .orElse("");
    }

    private void setCellIntegerValue(XSSFCell cell, Long valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(intStyle);
    }

    private void setCellDateTimeValue(XSSFCell cell, LocalDate valor) {
        cell.setCellValue(valor);
        cell.setCellStyle(dateTimeStyle);
    }

}
