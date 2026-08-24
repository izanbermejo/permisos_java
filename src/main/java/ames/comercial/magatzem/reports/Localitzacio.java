package ames.comercial.magatzem.reports;

import ames.comercial.advantage.internal.ObtenirFifoPeses;
import ames.comercial.advantage.internal.ObtenirFifoPeses.FifoPeses;
import ames.comercial.advantage.internal.ObtenirLocalitzacioPalet;
import ames.comercial.advantage.internal.ObtenirLocalitzacioPalet.PaletInfo;
import ames.comercial.shared.KeyArticleClient;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class Localitzacio {

    @Autowired ObtenirFifoPeses obtenirFifoPeses;
    @Autowired ObtenirLocalitzacioPalet obtenirLocalitzacioPalet;

    public ByteArrayOutputStream generateReport(KeyArticleClient keyArticleClient) throws Exception {

        var fifo = obtenirFifoPeses.get(keyArticleClient);
        var palets = obtenirLocalitzacioPalet.get(keyArticleClient);

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet palSheet = wb.createSheet("PALETS");
        int f = 0;
        String[] headersPalet = {
                "DATAENT", "QUANT", "QUANT CAIXES", "ESTANTE",
                "ETIPALE", "FABRICA", "MAGCOD", "EMBALATGE"
        };
        Row headerRow = palSheet.createRow(f++);
        for (int i = 0; i < headersPalet.length; i++)
            headerRow.createCell(i).setCellValue(headersPalet[i]);

        for (PaletInfo p : palets) {
            Row row = palSheet.createRow(f++);
            row.createCell(0).setCellValue(p.dataEntrada().toString());
            row.createCell(1).setCellValue(p.peces());
            row.createCell(2).setCellValue(p.caixes());
            row.createCell(3).setCellValue(p.estante());
            row.createCell(4).setCellValue(p.palet());
            row.createCell(5).setCellValue(p.fabrica());
            row.createCell(6).setCellValue(p.codiMagatzem());
            row.createCell(7).setCellValue(p.embalatge());
        }
        for (int i = 0; i < headersPalet.length; i++) palSheet.autoSizeColumn(i);

        XSSFSheet fifoSheet = wb.createSheet("FIFO");
        f = 0;
        String[] headers = {
                "DATA", "LOT", "QUANT", "ESTANTE", "DATAENT",
                "ETICAJA", "ETIPALE", "FABRICA", "MAGCOD", "EMBALATGE"
        };
        headerRow = fifoSheet.createRow(f++);
        for (int i = 0; i < headers.length; i++)
            headerRow.createCell(i).setCellValue(headers[i]);

        for (FifoPeses fp : fifo) {
            Row row = fifoSheet.createRow(f++);
            row.createCell(0).setCellValue(fp.data().toString());
            row.createCell(1).setCellValue(fp.lot());
            row.createCell(2).setCellValue(fp.quantitat());
            row.createCell(3).setCellValue(fp.estante());
            row.createCell(4).setCellValue(fp.dataEntrada().toString());
            row.createCell(5).setCellValue(fp.etiquetaCaixa());
            row.createCell(6).setCellValue(fp.etiquetaPaler());
            row.createCell(7).setCellValue(fp.fabrica());
            row.createCell(8).setCellValue(fp.codiMagatzem());
            row.createCell(9).setCellValue(fp.embalatge());
        }

        for (int i = 0; i < headers.length; i++)
            fifoSheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        wb.close();
        return baos;
    }
}