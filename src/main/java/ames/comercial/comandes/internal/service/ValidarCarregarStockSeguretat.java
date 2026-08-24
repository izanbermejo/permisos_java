package ames.comercial.comandes.internal.service;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.comandes.internal.application.command.CarregarStocksSeguretat.RegistreCarregarStockSeguretat;
import ames.comercial.server.exception.AppException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class ValidarCarregarStockSeguretat {

    @Autowired IObtenirArticleClientInformacioComanda obtenirArticleClient;

    public List<RegistreCarregarStockSeguretat> executar (InputStream inputStream) {
        List<RegistreCarregarStockSeguretat> registres = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Sempre s'agafa la primera fulla
            var fulla = obtenirFulla(workbook);
            int rowIndex = 1;
            Row fila;
            while ((fila = fulla.getRow(rowIndex)) != null) {
                if (isFilaBuida(fila))
                    break;
                registres.add(new RegistreCarregarStockSeguretat(
                        checkArticleClient(fila),
                        checkQuantitat(fila),
                        checkData(fila),
                        checkStockClient(fila)
                ));
                rowIndex++;
            }
        } catch (Exception e) {
            throw new AppException("Error carregant els stocks de seguretat: " + e.getMessage(), e);
        }
        return registres;
    }

    private Sheet obtenirFulla (Workbook workbook) {
        Sheet fulla = workbook.getSheetAt(0);
        if (fulla == null)
            throw new AppException("No hi ha cap fulla al full de càlcul a processar");
        return fulla;
    }

    private String checkArticleClient(Row fila) {
        var cellValue = fila.getCell(0).getStringCellValue();
        var articleClient = obtenirArticleClient.executar(cellValue);
        if (articleClient.isEmpty())
            throw new AppException("Articleclient " + articleClient + " de la fila " + fila.getRowNum()+1 + " no existeix");
        return cellValue;
    }

    private LocalDate checkData(Row fila) {
        var cell = fila.getCell(1);
        if (!DateUtil.isCellDateFormatted(cell))
            throw new AppException("Data malament formatada a la fila" + fila.getRowNum()+1);
        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private long checkQuantitat(Row fila) {
        var cell = fila.getCell(2);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            throw new AppException(
                    "Quantitat malament formatada a la fila " + (fila.getRowNum() + 1) +
                            " (només es permeten nombres enters sense decimals ni lletres)"
            );
        }

        double valor = cell.getNumericCellValue();

        if (valor < 0 || valor != Math.floor(valor)) {
            throw new RuntimeException(
                    "El número de la quantitat ha de ser enter i >=0 (fila " + (fila.getRowNum() + 1) + ")"
            );
        }
        return (long) valor;
    }

    private boolean checkStockClient(Row fila) {
        var cell = fila.getCell(3);
        String text = cell != null ? cell.getStringCellValue().trim().toUpperCase() : null;
        if (text == null || (!text.equals("C") && !text.equals("A")))
            throw new AppException("L'indicador de si es stock de client o Ames ha de ser C o A (fila" + fila.getRowNum()+1 + ")");
        return text.equals("C");
    }

    private boolean isFilaBuida (Row fila) {
        if (fila == null) return true;
        Cell celda = fila.getCell(0);
        return (celda == null) || CellType.BLANK.equals(celda.getCellType()) || celda.getStringCellValue().isBlank();
    }

}
