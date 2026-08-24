package ames.comercial.inventari.internal.application.query;

import ames.comercial.advantage.internal.ObtenirCanviDivisa;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Llistat de peces immobilitzades: articles-client que tenen existències al final del mes demanat,
 * que ja en tenien a l'inici del període i que no han tingut cap sortida durant els
 * {@value #MESOS_FINESTRA} mesos de la finestra. Inclou totes les empreses i magatzems.
 * <p>
 * Exigir que ja hi hagués existències a l'inici del període deixa fora les peces acabades de
 * fabricar que encara no han sortit, que és una situació normal i pot durar mesos. Si no acaben
 * sortint, hi apareixeran quan la finestra les atrapi.
 * <p>
 * Substitueix el procediment {@code PcsInm01} de l'aplicació Delphi, que recorria {@code his}
 * d'Advantage registre a registre. Les consultes només llegeixen i sumen; tota la lògica de signes,
 * de finestra temporal i del criteri d'immobilització es resol aquí.
 * <p>
 * L'estoc de referència és el de {@code inventari.fitxa} (estoc d'avui), de manera que per a un mes
 * ja tancat cal desfer els moviments posteriors al mes abans d'avaluar el criteri.
 */
@Service
public class ObtenirPecesImmobilitzades {

	/**
	 * Mesos naturals sencers de la finestra sense sortides, comptant-hi el mes demanat: per al mes
	 * 2026-06 la finestra va de l'01-07-2025 al 30-06-2026.
	 */
	public static final int MESOS_FINESTRA = 12;

	/** Límit superior per a la consulta de moviments posteriors al mes demanat. */
	private static final LocalDate DATA_INFINIT = LocalDate.of(9999, 12, 31);

	/** Nombre màxim de claus per consulta amb {@code IN}, pel límit de paràmetres de PostgreSQL. */
	private static final int MIDA_LOT_CLAUS = 5000;

	/** Divisa en què es valora tot el llistat, independentment de la divisa de cada article-client. */
	private static final Divisa DIVISA_LLISTAT = Divisa.EURO;

	/** Mesos que es recula com a màxim buscant un tipus de canvi disponible. */
	private static final int MESOS_MAXIM_RECULADA_CANVI = 12;

	/**
	 * Tipus d'article-client que no formen part del llistat. S'exclouen unint la cache a les
	 * consultes de candidats: {@code (artint, clicod)} és clau única a {@code cache_article_client},
	 * de manera que el JOIN no pot multiplicar files ni falsejar les sumes.
	 */
	private static final List<TipusArticleClient> TIPUS_EXCLOSOS = List.of(TipusArticleClient.MARKETING);

	private static final String SQL_STOCKS = """
			SELECT f.artint, f.clicod, f.stock
			FROM inventari.fitxa f
			LEFT JOIN cache.cache_article_client ac ON ac.artint = f.artint AND ac.clicod = f.clicod
			WHERE f.stock <> 0
			  AND (ac.tipus IS NULL OR ac.tipus NOT IN (:tipusExclosos))
			""";

	private static final String SQL_MOVIMENTS = """
			SELECT m.artint, m.clicod, m.tipus, m.quantitat > 0 AS is_positiu, SUM(m.quantitat) AS quantitat
			FROM inventari.moviment m
			LEFT JOIN cache.cache_article_client ac ON ac.artint = m.artint AND ac.clicod = m.clicod
			WHERE m.data >= :dataDes
			  AND m.data <= :dataFins
			  AND (ac.tipus IS NULL OR ac.tipus NOT IN (:tipusExclosos))
			GROUP BY m.artint, m.clicod, m.tipus, m.quantitat > 0
			""";

	private static final String SQL_DARRERES_DATES = """
			SELECT artint, clicod, tipus, MAX(data) AS darrera_data
			FROM inventari.moviment
			WHERE (artint, clicod) IN (:claus)
			  AND quantitat > 0
			  AND data <= :dataFi
			  AND tipus IN ('ENTRADA', 'SORTIDA')
			GROUP BY artint, clicod, tipus
			""";

	private static final String SQL_DESCRIPTIUS = """
			SELECT ac.artint, ac.clicod, ac.codi_fabrica, ac.referencia, ac.denominacio,
			       ac.codi_familia, ac.nom_familia, ac.preu, ac.divisa,
			       cli.nom, cli.delegat_codi
			FROM cache.cache_article_client ac
			LEFT JOIN (SELECT DISTINCT ON (clicod) clicod, nom, delegat_codi
			           FROM cache.cache_client) cli ON cli.clicod = ac.clicod
			WHERE (ac.artint, ac.clicod) IN (:claus)
			""";

	@Autowired NamedParameterJdbcTemplate jdbcAmes;

	public List<ItemPecaImmobilitzada> executar(YearMonth mes) {
		var dataFi = mes.atEndOfMonth();
		var dataInici = mes.minusMonths(MESOS_FINESTRA - 1L).atDay(1);

		// 1. Estoc actual i moviments, acumulats per article-client
		var acumuladors = new HashMap<KeyArticleClient, Acumulador>();
		for (var fila : obtenirStocks())
			acumulador(acumuladors, fila.clau()).afegirStock(fila.stock());
		for (var fila : obtenirMoviments(dataFi.plusDays(1), DATA_INFINIT))
			acumulador(acumuladors, fila.clau()).afegirPosterior(fila);
		for (var fila : obtenirMoviments(dataInici, dataFi))
			acumulador(acumuladors, fila.clau()).afegirFinestra(fila);

		// 2. Criteri d'immobilització: té existències al final del mes, ja en tenia a l'inici del
		//    període i no ha tingut cap sortida durant la finestra. Les claus sense estoc ni
		//    moviments posteriors cauen aquí mateix.
		var candidats = acumuladors.entrySet().stream()
				.filter(entry -> entry.getValue().stockDataFi() > 0)
				.filter(entry -> entry.getValue().stockDataInici() > 0)
				.filter(entry -> entry.getValue().sortidesFinestra() == 0)
				.toList();
		if (candidats.isEmpty())
			return List.of();

		// 3. Descriptius i darreres dates, només de les peces que sortiran al llistat
		var claus = candidats.stream().map(Map.Entry::getKey).toList();
		var darreresDates = obtenirDarreresDates(claus, dataFi);
		var descriptius = obtenirDescriptius(claus);
		var canvis = obtenirCanvis(descriptius.values(), mes);

		return candidats.stream()
				.map(entry -> muntarItem(entry.getKey(), entry.getValue(),
						darreresDates.getOrDefault(entry.getKey(), new EnumMap<>(TipusMoviment.class)),
						Optional.ofNullable(descriptius.get(entry.getKey())), canvis))
				.sorted(Comparator.comparing(ItemPecaImmobilitzada::valoracio).reversed())
				.toList();
	}

	private ItemPecaImmobilitzada muntarItem(KeyArticleClient clau, Acumulador acumulador,
			Map<TipusMoviment, LocalDate> darreresDates, Optional<FilaDescriptius> descriptius,
			Map<Divisa, BigDecimal> canvis) {
		var unitats = acumulador.stockDataFi();
		var preu = descriptius.map(FilaDescriptius::preu).orElse(BigDecimal.ZERO);
		var divisa = descriptius.flatMap(FilaDescriptius::divisa);
		var darreraEntrada = Optional.ofNullable(darreresDates.get(TipusMoviment.ENTRADA));
		var darreraSortida = Optional.ofNullable(darreresDates.get(TipusMoviment.SORTIDA));
		return ItemPecaImmobilitzadaImpl.builder()
				.artint(clau.artint())
				.clicod(clau.clicod())
				.article(descriptius.flatMap(FilaDescriptius::article))
				.referencia(descriptius.flatMap(FilaDescriptius::referencia))
				.denominacio(descriptius.flatMap(FilaDescriptius::denominacio))
				.codiFamilia(descriptius.flatMap(FilaDescriptius::codiFamilia))
				.nomFamilia(descriptius.flatMap(FilaDescriptius::nomFamilia))
				.nomClient(descriptius.flatMap(FilaDescriptius::nomClient))
				.delegat(descriptius.flatMap(FilaDescriptius::delegat))
				.unitats(unitats)
				.preu(preu)
				.divisa(divisa)
				.valoracio(valoracio(preu, divisa, unitats, canvis))
				.darreraEntrada(darreraEntrada)
				.darreraSortida(darreraSortida)
				.build();
	}

	/**
	 * Valoració de l'estoc convertida a {@link #DIVISA_LLISTAT}. Delega a {@link Preu#imp(long)}
	 * perquè les divises de tipus cèntim (p.ex. {@code "EUR%"}) es divideixin per 100, i arrodoneix
	 * a 2 decimals com feia el {@code D_To_D} del Delphi. Si no hi ha divisa o no s'ha trobat cap
	 * tipus de canvi, l'import queda a zero, també com el Delphi.
	 */
	private BigDecimal valoracio(BigDecimal preu, Optional<Divisa> divisa, long unitats,
			Map<Divisa, BigDecimal> canvis) {
		var canvi = divisa.map(Divisa::base).map(canvis::get);
		if (canvi.isEmpty())
			return BigDecimal.ZERO;
		return Preu.of(preu, divisa.get()).imp(unitats)
				.multiply(canvi.get())
				.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Factor de conversió a {@link #DIVISA_LLISTAT} de cada divisa present al llistat. Es busca el
	 * canvi de l'1 del mes demanat, com feia el Delphi, i si aquell dia no en té es recula dia a dia
	 * fins a trobar-ne un, de manera que un dia sense cotització (festius, caps de setmana) no deixi
	 * la peça sense valorar. Les divises que no arriben a tenir cap canvi no hi surten.
	 */
	private Map<Divisa, BigDecimal> obtenirCanvis(Collection<FilaDescriptius> descriptius, YearMonth mes) {
		var dataCanvi = mes.atDay(1);
		var dataMinima = mes.minusMonths(MESOS_MAXIM_RECULADA_CANVI).atDay(1);
		var canvis = new HashMap<Divisa, BigDecimal>();
		var divises = descriptius.stream()
				.map(FilaDescriptius::divisa)
				.flatMap(Optional::stream)
				.map(Divisa::base)
				.distinct()
				.toList();
		for (var divisa : divises) {
			if (DIVISA_LLISTAT.equals(divisa)) {
				canvis.put(divisa, BigDecimal.ONE);
				continue;
			}
			var canvisPerDia = new ObtenirCanviDivisa().get(divisa, DIVISA_LLISTAT, dataMinima, dataCanvi);
			for (var data = dataCanvi; !data.isBefore(dataMinima); data = data.minusDays(1)) {
				var canvi = canvisPerDia.get(data);
				if (canvi != null && canvi.signum() > 0) {
					canvis.put(divisa, canvi);
					break;
				}
			}
		}
		return canvis;
	}

	private List<FilaStock> obtenirStocks() {
		return jdbcAmes.query(SQL_STOCKS, paramsTipusExclosos(), (rs, i) -> new FilaStock(
				llegirClau(rs),
				rs.getLong("stock")));
	}

	private List<FilaMoviment> obtenirMoviments(LocalDate dataDes, LocalDate dataFins) {
		var params = paramsTipusExclosos()
				.addValue("dataDes", dataDes)
				.addValue("dataFins", dataFins);
		return jdbcAmes.query(SQL_MOVIMENTS, params, (rs, i) -> new FilaMoviment(
				llegirClau(rs),
				TipusMoviment.valueOf(rs.getString("tipus")),
				rs.getBoolean("is_positiu"),
				rs.getLong("quantitat")));
	}

	private Map<KeyArticleClient, Map<TipusMoviment, LocalDate>> obtenirDarreresDates(
			List<KeyArticleClient> claus, LocalDate dataFi) {
		var resultat = new HashMap<KeyArticleClient, Map<TipusMoviment, LocalDate>>();
		for (var lot : lots(claus)) {
			var params = new MapSqlParameterSource("claus", tuples(lot))
					.addValue("dataFi", dataFi);
			jdbcAmes.query(SQL_DARRERES_DATES, params, rs -> {
				var clau = llegirClau(rs);
				resultat.computeIfAbsent(clau, k -> new EnumMap<>(TipusMoviment.class))
						.put(TipusMoviment.valueOf(rs.getString("tipus")),
								rs.getDate("darrera_data").toLocalDate());
			});
		}
		return resultat;
	}

	private Map<KeyArticleClient, FilaDescriptius> obtenirDescriptius(List<KeyArticleClient> claus) {
		var resultat = new HashMap<KeyArticleClient, FilaDescriptius>();
		for (var lot : lots(claus)) {
			var params = new MapSqlParameterSource("claus", tuples(lot));
			jdbcAmes.query(SQL_DESCRIPTIUS, params, rs -> {
				resultat.put(llegirClau(rs), new FilaDescriptius(
						readOptionalString(rs, "codi_fabrica"),
						readOptionalString(rs, "referencia"),
						readOptionalString(rs, "denominacio"),
						readOptionalString(rs, "codi_familia"),
						readOptionalString(rs, "nom_familia"),
						readOptionalString(rs, "nom"),
						readOptionalString(rs, "delegat_codi"),
						Optional.ofNullable(rs.getBigDecimal("preu")).orElse(BigDecimal.ZERO),
						readOptionalString(rs, "divisa").map(Divisa::getBySymbol)));
			});
		}
		return resultat;
	}

	private MapSqlParameterSource paramsTipusExclosos() {
		return new MapSqlParameterSource("tipusExclosos",
				TIPUS_EXCLOSOS.stream().map(TipusArticleClient::name).toList());
	}

	private Acumulador acumulador(Map<KeyArticleClient, Acumulador> acumuladors, KeyArticleClient clau) {
		return acumuladors.computeIfAbsent(clau, k -> new Acumulador());
	}

	/** Les claus es passen com a llista de parelles perquè Spring les expandeixi a {@code (?,?),(?,?)}. */
	private List<Object[]> tuples(List<KeyArticleClient> claus) {
		return claus.stream().map(c -> new Object[] { c.artint(), c.clicod() }).toList();
	}

	private List<List<KeyArticleClient>> lots(List<KeyArticleClient> claus) {
		var lots = new ArrayList<List<KeyArticleClient>>();
		for (int i = 0; i < claus.size(); i += MIDA_LOT_CLAUS)
			lots.add(claus.subList(i, Math.min(i + MIDA_LOT_CLAUS, claus.size())));
		return lots;
	}

	private KeyArticleClient llegirClau(ResultSet rs) throws SQLException {
		return KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod"));
	}

	private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
		var valor = Strings.nullToEmpty(rs.getString(column)).trim();
		return valor.isEmpty() ? Optional.empty() : Optional.of(valor);
	}

	/**
	 * Reconstrueix l'estoc d'un article-client cap enrere a partir de l'estoc actual, i acumula el
	 * que cal per decidir si la peça està immobilitzada. Els imports dels moviments ja arriben
	 * sumats de la base de dades, agrupats per tipus i signe.
	 */
	private static class Acumulador {

		private long stockActual;
		private long deltaPosterior;
		private long deltaFinestra;
		private long sortidesFinestra;

		void afegirStock(long stock) {
			stockActual += stock;
		}

		void afegirPosterior(FilaMoviment fila) {
			deltaPosterior += fila.quantitatCalculFitxa();
		}

		void afegirFinestra(FilaMoviment fila) {
			deltaFinestra += fila.quantitatCalculFitxa();
			if (fila.isSortidaReal())
				sortidesFinestra += fila.quantitat();
		}

		/** Estoc al final del mes demanat: l'actual desfent els moviments posteriors. */
		long stockDataFi() {
			return stockActual - deltaPosterior;
		}

		/** Estoc a l'inici de la finestra: el del final del mes desfent els moviments de la finestra. */
		long stockDataInici() {
			return stockDataFi() - deltaFinestra;
		}

		long sortidesFinestra() {
			return sortidesFinestra;
		}
	}

	private record FilaStock(KeyArticleClient clau, long stock) {}

	private record FilaMoviment(KeyArticleClient clau, TipusMoviment tipus,
			boolean isPositiu, long quantitat) {

		long quantitatCalculFitxa() {
			return tipus.quantitatCalculFitxa(quantitat);
		}

		/** Sortida de debò, no una devolució de client (que és una SORTIDA amb quantitat negativa). */
		boolean isSortidaReal() {
			return TipusMoviment.SORTIDA.equals(tipus) && isPositiu;
		}
	}

	private record FilaDescriptius(Optional<String> article, Optional<String> referencia,
			Optional<String> denominacio, Optional<String> codiFamilia, Optional<String> nomFamilia,
			Optional<String> nomClient, Optional<String> delegat, BigDecimal preu,
			Optional<Divisa> divisa) {}

	@JsonDeserialize(builder = ItemPecaImmobilitzadaImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ItemPecaImmobilitzada {

		String artint();
		String clicod();
		Optional<String> article();
		Optional<String> referencia();
		Optional<String> denominacio();
		Optional<String> codiFamilia();
		Optional<String> nomFamilia();
		Optional<String> nomClient();

		/** Codi del delegat del client ({@code cli6.clidel} d'Advantage, via la cache). */
		Optional<String> delegat();

		/** Existències al final del mes de referència. */
		long unitats();

		BigDecimal preu();
		Optional<Divisa> divisa();

		/**
		 * {@code unitats × preu} convertit a euros al canvi de l'1 del mes demanat i arrodonit a 2
		 * decimals. Zero si no s'ha pogut convertir. Sempre en euros, sigui quina sigui
		 * {@link #divisa()}, de manera que la columna és sumable.
		 */
		BigDecimal valoracio();

		/** Darrera entrada positiva fins al final del mes; buida si mai no ha entrat res. */
		Optional<LocalDate> darreraEntrada();

		/** Darrera sortida positiva fins al final del mes; buida si mai no ha sortit res. */
		Optional<LocalDate> darreraSortida();
	}

}
