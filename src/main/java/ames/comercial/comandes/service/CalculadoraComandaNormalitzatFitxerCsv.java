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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CalculadoraComandaNormalitzatFitxerCsv {

	@Autowired ObtenirClientAds obtenirClientAds;
	@Autowired ICalcularDiaSortida calcularDiaSortida;
	@Autowired IObtenirArticlesClientNormalitzatsByReferencia obtenirArticlesByReferencia;
	@Autowired CalculadoraComandaNormalitzat calculadora;


	public CalculComandaNormalitzatResponse calcula (String codiClient, InputStream inputStream) throws IOException {
		// Obtenció del client
		var cliAds = obtenirClientAds.get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
		// Lectura del CSV comprovant que tots els camps son vàlids
		List<Registre> registres = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String linia;
		int rowNum = 1;
		while ((linia = reader.readLine()) != null) {
			int finalRowNum = rowNum;
			String[] fields = linia.split(";");
			// 1a columna - Referència
			String referencia = fields[0];
			// 2a columna - Quantitat (ha de ser un enter major que 0)
			int quantitat = parseAndValidateInteger(fields[1]).orElseThrow(() -> new QuantitatNoValidaFila(finalRowNum));
			// 3a columna - Data sol·licitada (ha de ser una data vàlida en cas que estigui informada)
			LocalDate data = calcularProperDiaLaboral(LocalDate.now()); // Si no s'informa data, es pren el proper dia laboral a partir d'avui
			if (fields.length >= 3 && !fields[2].trim().isBlank()) {
				data = parseLocalDate(fields[2]).orElseThrow(() -> new DataNoValidaFila(finalRowNum));
			}
			registres.add(new Registre(referencia, quantitat, data));
			rowNum++;
		}
		// Obtenció dels articles client
		var articlesClient = obtenirArticlesByReferencia.query(registres.stream().map(Registre::referencia).collect(Collectors.toSet()));
		// Construcció del request per a la calculadora
		List<LiniaNormalitzatReq> linies = new ArrayList<>();
		rowNum = 1;
		for (Registre reg : registres) {
			var articleClient = articlesClient.get(reg.referencia);
			if (articleClient == null)
				throw new ReferenciaNoExisteixFila(reg.referencia, rowNum);
			// Comprovació de la quantitat
			if (reg.quantitat % articleClient.unitatsEmbalatge() != 0)
				throw new UnitatsEmbalatgeIncorrectaFila(reg.referencia, rowNum);
			// Construcció del request
			var req = LiniaNormalitzatReqImpl.builder()
					.linia(rowNum)
					.articleClient(articleClient.articleClient())
					.quantitat(reg.quantitat)
					.dataSolicitada(reg.data)
					.dataPrevistaSortida(calcularDiaSortida.executar(reg.data, cliAds.diesTransitClient()))
					.build();
			linies.add(req);
			rowNum++;
		}
		return calculadora.calcula(codiClient, linies);
	}

	private static LocalDate calcularProperDiaLaboral(LocalDate data) {
		data = data.plusDays(1); // Es comença a comptar a partir del dia següent
		while (data.getDayOfWeek().getValue() >= 6) { // 6 = dissabte, 7 = diumenge
			data = data.plusDays(1);
		}
		return data;
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

	private record Registre (String referencia, int quantitat, LocalDate data) {};

}
