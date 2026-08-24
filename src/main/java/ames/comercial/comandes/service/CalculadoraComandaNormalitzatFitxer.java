package ames.comercial.comandes.service;

import ames.comercial.advantage.IObtenirArticlesClientNormalitzatsByReferencia;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.comandes.ComandesException.DataNoValidaFila;
import ames.comercial.comandes.ComandesException.QuantitatNoValidaFila;
import ames.comercial.comandes.ComandesException.ReferenciaNoExisteixFila;
import ames.comercial.comandes.ComandesException.UnitatsEmbalatgeIncorrectaFila;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.LiniaNormalitzatReqImpl;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponse;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class CalculadoraComandaNormalitzatFitxer {

	@Autowired ObtenirClientAds obtenirClientAds;
	@Autowired ICalcularDiaSortida calcularDiaSortida;
	@Autowired IObtenirArticlesClientNormalitzatsByReferencia obtenirArticlesByReferencia;
	@Autowired CalculadoraComandaNormalitzat calculadora;


	public CalculComandaNormalitzatResponse calcula (String codiClient, InputStream inputStream) throws IOException {
		// Obtenció del client
		var cliAds = obtenirClientAds.get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
		// Obtenció de la 1a pestanya de l'Excel amb les dades
		Workbook wb = WorkbookFactory.create(inputStream);
		Sheet sheet = wb.getSheetAt(0);
		// Comprovació que el format de totes les columnes es vàlid
		// mentre s'omple una llista d'Strings amb les referències
		Set<String> referencies = new HashSet<>();
		for (Row row : sheet) {
			// Comprovació si la fila està buida per deixar de llegir
			if (isRowEmpty(row))
				break;
			// 1a columna - Referència
			referencies.add(row.getCell(0).getStringCellValue());
			// 2a columna - Quantitat (ha de ser un enter major que 0)
			readPositiveInteger(row.getCell(1)).orElseThrow(() -> new QuantitatNoValidaFila(row.getRowNum()+1));
			// 3a columna - Data sol·licitada (ha de ser una data vàlida)
			readDate(row.getCell(2)).orElseThrow(() -> new DataNoValidaFila(row.getRowNum()+1));
		}
		// Obtenció dels articles client
		var articlesClient = obtenirArticlesByReferencia.query(referencies);
		// Construcció del request per a la calculadora
		List<LiniaNormalitzatReq> linies = new ArrayList<>();
		for (Row row : sheet) {
			// Comprovació si la fila està buida per deixar de llegir
			if (isRowEmpty(row))
				break;
			var referencia = row.getCell(0).getStringCellValue();
			var rowNum = row.getRowNum()+1;
			int quantitat = readPositiveInteger(row.getCell(1)).orElseThrow();
			var data = readDate(row.getCell(2)).orElseThrow();
			var articleClient = articlesClient.get(referencia);
			if (articleClient == null)
				throw new ReferenciaNoExisteixFila(referencia, rowNum);
			// Comprovació de la quantitat
			if (quantitat % articleClient.unitatsEmbalatge() != 0)
				throw new UnitatsEmbalatgeIncorrectaFila(referencia, rowNum);

			// Construcció del request
			var req = LiniaNormalitzatReqImpl.builder()
					.linia(rowNum)
					.articleClient(articleClient.articleClient())
					.quantitat(quantitat)
					.dataSolicitada(data)
					.dataPrevistaSortida(calcularDiaSortida.executar(data, cliAds.diesTransitClient()))
					.build();
			linies.add(req);
		}
		return calculadora.calcula(codiClient, linies);
	}

	private static Optional<Integer> readPositiveInteger (Cell cell) {
		// En cas que la cell sigui un String s'intenta parsejar a un Integer
		if (cell.getCellType() == CellType.STRING) {
			return parseAndValidateInteger(cell.getStringCellValue());
		}

		// En cas que la cell no sigui de tipus numèric es torna empty ja que no
		// serà ni un String (comprovat anteriorment) ni un número
		if (cell.getCellType() != CellType.NUMERIC)
			return Optional.empty();

		double value = cell.getNumericCellValue();
		if (value == (int)value && value > 0)
			return Optional.of((int)value);

		return Optional.empty();
	}

	private static Optional<LocalDate> readDate (Cell cell) {
		// En cas que la cell sigui un String s'intenta parsejar a un LocalDate
		if (cell.getCellType() == CellType.STRING) {
			return parseLocalDate(cell.getStringCellValue());
		}

		// En cas que la cell no sigui de tipus numèric es torna empty ja que no
		// serà ni un String (comprovat anteriorment) ni una data
		if (cell.getCellType() != CellType.NUMERIC)
			return Optional.empty();

		if (DateUtil.isCellDateFormatted(cell)) {
			return Optional.of(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}

		return Optional.empty();
	}

	/**
	 * Parseja un String a Integer sempre que tingui un format vàlid i sigui major que 0
	 * @param value String a parsejar
	 * @return Optional amb l'integer si es vàlid, altrament Optional.empty
	 */
	private static Optional<Integer> parseAndValidateInteger(String value) {
		try {
			int number = Integer.parseInt(value);
			return (number > 0) ? Optional.of(number) : Optional.empty();
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	private static Optional<LocalDate> parseLocalDate(String value) {
		// S'intenta parsejar la data tenint en compte que l'any pot venir amb 2 o 4 dígits
		return parseLocalDate(value, "d/M/yyyy").or(() -> parseLocalDate(value, "d/M/yy"));
    }

	private static Optional<LocalDate> parseLocalDate(String value, String pattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		try {
			return Optional.of(LocalDate.parse(value, formatter));
		} catch (DateTimeParseException e) {
			return Optional.empty();
		}
	}

	private static boolean isRowEmpty(Row row) {
		if (row == null)
			return true;
		for (Cell cell : row) {
			if (cell != null && cell.getCellType() != CellType.BLANK)
				return false;
		}
		return true;
	}

}
