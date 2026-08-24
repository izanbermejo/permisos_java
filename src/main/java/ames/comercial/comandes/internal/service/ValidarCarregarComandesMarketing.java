package ames.comercial.comandes.internal.service;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.comandes.internal.application.command.CarregarComandesMarketing.RegistreCarregarComandesMarketing;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.KeyArticleClient;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class ValidarCarregarComandesMarketing {

    @Autowired IObtenirClientAds obtenirClientAds;

    public List<RegistreCarregarComandesMarketing> executar (InputStream inputStream) {
        List<RegistreCarregarComandesMarketing> registres = new ArrayList<>();
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            // Sempre s'agafa la primera fulla
            var fulla = obtenirFulla(workbook);
            int rowIndex = 1;
            Row fila;
            while ((fila = fulla.getRow(rowIndex)) != null) {
                if (isFilaBuida(fila))
                    break;
                registres.add(new RegistreCarregarComandesMarketing(
                        checkClient(fila),
                        checkNomClient(fila),
                        checkArticle(fila),
                        checkQuantitat(fila),
                        checkData(fila)
                ));
                rowIndex++;
            }
        } catch (Exception e) {
            throw new AppException("Error carregant les comandes de màrqueting: " + e.getMessage(), e);
        }
        return registres;
    }

    private Sheet obtenirFulla (Workbook workbook) {
        Sheet fulla = workbook.getSheetAt(0);
        if (fulla == null)
            throw new AppException("No hi ha cap fulla al full de càlcul a processar");
        return fulla;
    }

    private String checkClient(Row fila) {
        var cellValue = fila.getCell(0).getStringCellValue();
        var client = obtenirClientAds.get(cellValue);
        if (client.isEmpty())
            throw new AppException("Client " + cellValue + " de la fila " + fila.getRowNum()+1 + " no existeix");
        return cellValue;
    }

    private String checkNomClient(Row fila) {
        var cellValue = fila.getCell(1).getStringCellValue();
        return cellValue;
    }

    private KeyArticleClient checkArticle(Row fila) {
        var cellValue = fila.getCell(3).getStringCellValue();
        var articleClient = new ObtenirArticleClientAds().query(cellValue, "000000");
        if (articleClient.isEmpty()) {
            throw new AppException("Articleclient " + cellValue + " de la fila " + fila.getRowNum()+1 + " no existeix");
        }
        return articleClient.get().clau();
    }

    private LocalDate checkData(Row fila) {
        var cell = fila.getCell(6);
        if (!DateUtil.isCellDateFormatted(cell))
            throw new AppException("Data malament formatada a la fila" + fila.getRowNum()+1);
        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private long checkQuantitat(Row fila) {
        var cell = fila.getCell(5);
        if (cell == null || cell.getCellType() != CellType.NUMERIC)
            throw new AppException("Quantitat malament formatada a la fila" + fila.getRowNum()+1);
        double valor = cell.getNumericCellValue();
        if (valor <= 0 || valor != Math.floor(valor)) {
            throw new RuntimeException("El número de la quantitat d'stock ha de ser mes que gran que 0 (fila " + fila.getRowNum()+1 + ")");
        }
        return (long)valor;
    }

    private boolean isFilaBuida (Row fila) {
        if (fila == null) return true;
        Cell celda = fila.getCell(0);
        return (celda == null) || CellType.BLANK.equals(celda.getCellType()) || celda.getStringCellValue().isBlank();
    }

}
