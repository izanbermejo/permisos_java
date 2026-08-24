package ames.comercial.inventari.internal.application.query;

import ames.comercial.advantage.internal.ObtenirCanviDivisa;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Llistat de ferralla dels {@value #MESOS_PERIODE} mesos anteriors al mes de referència: els
 * moviments de tipus {@link TipusMoviment#FERRALLA} del període, acumulats per article-client i
 * valorats a preu de cost. Inclou totes les empreses i magatzems.
 * <p>
 * Substitueix el procediment {@code Ferralla01} de l'aplicació Delphi, que recorria {@code his}
 * d'Advantage registre a registre i deixava el resultat a {@code lfer.dbf}. La consulta només llegeix
 * i suma unitats; la valoració i la conversió de divisa es resolen aquí.
 * <p>
 * El cost que s'aplica és el vigent a la cache en el moment de generar el llistat, no el del dia del
 * moviment, igual que el Delphi, que llegia {@code artcli.dbf} a mesura que recorria l'històric. És
 * el criteri que va introduir la revisió del 2013 en passar del preu de venda al preu de cost.
 */
@Service
public class ObtenirFerralla {

	static final Logger log = LogManager.getLogger(ObtenirFerralla.class.getName());

	/** Mesos naturals sencers que abasta el llistat, tots anteriors al mes de referència. */
	public static final int MESOS_PERIODE = 12;

	/** Divisa en què es valora tot el llistat, independentment de la divisa de cost de cada peça. */
	private static final Divisa DIVISA_LLISTAT = Divisa.EURO;

	/** Mesos que es recula com a màxim buscant un tipus de canvi disponible. */
	private static final int MESOS_MAXIM_RECULADA_CANVI = 12;

	/**
	 * Unitats de ferralla acumulades per article-client. S'agrupa per {@code (artint, clicod)} i no
	 * per {@code codi_fabrica + clicod} com feia el Delphi, de manera que cada fila conserva el seu
	 * cost: si dos articles interns del mateix client comparteixen codi de fàbrica, aquí surten
	 * separats i el Delphi els fusionava en una sola fila.
	 * <p>
	 * El nom del client es llegeix amb {@code DISTINCT ON} perquè una segona fila per al mateix
	 * {@code clicod} multiplicaria les files del grup i falsejaria la suma d'unitats.
	 */
	private static final String SQL_FERRALLA = """
			SELECT m.artint, m.clicod,
			       ac.codi_fabrica, ac.referencia, ac.denominacio, ac.cost, ac.divisa_cost,
			       cli.nom,
			       SUM(m.quantitat) AS unitats
			FROM inventari.moviment m
			LEFT JOIN cache.cache_article_client ac
			       ON ac.artint = m.artint AND ac.clicod = m.clicod
			LEFT JOIN (SELECT DISTINCT ON (clicod) clicod, nom
			           FROM cache.cache_client) cli ON cli.clicod = m.clicod
			WHERE m.tipus = :tipus
			  AND m.data >= :dataInici
			  AND m.data <= :dataFi
			GROUP BY m.artint, m.clicod, ac.codi_fabrica, ac.referencia, ac.denominacio,
			         ac.cost, ac.divisa_cost, cli.nom
			HAVING SUM(m.quantitat) <> 0
			""";

	@Autowired NamedParameterJdbcTemplate jdbcAmes;

	public List<ItemFerralla> executar(YearMonth mes) {
		var periode = periodeDe(mes);
		var files = obtenirFiles(periode);
		avisarPecesSenseDescriptius(files);
		var canvis = obtenirCanvis(files, periode.dataFi());
		return files.stream()
				.map(fila -> muntarItem(fila, canvis))
				.sorted(Comparator.comparing(ItemFerralla::articleClient))
				.toList();
	}

	/**
	 * Els {@value #MESOS_PERIODE} mesos naturals sencers que precedeixen {@code mes}, que no hi entra:
	 * per al mes de referència 2026-07 el període va de l'01-07-2025 al 30-06-2026. Així, una tasca
	 * que s'executi l'1 de cada mes amb el mes en curs sempre treballa sobre mesos ja tancats.
	 */
	public static Periode periodeDe(YearMonth mes) {
		return new Periode(
				mes.minusMonths(MESOS_PERIODE).atDay(1),
				mes.minusMonths(1).atEndOfMonth());
	}

	private List<FilaFerralla> obtenirFiles(Periode periode) {
		var params = new MapSqlParameterSource("tipus", TipusMoviment.FERRALLA.name())
				.addValue("dataInici", periode.dataInici())
				.addValue("dataFi", periode.dataFi());
		return jdbcAmes.query(SQL_FERRALLA, params, (rs, i) -> new FilaFerralla(
				rs.getString("artint"),
				rs.getString("clicod"),
				readOptionalString(rs, "codi_fabrica"),
				readOptionalString(rs, "referencia"),
				readOptionalString(rs, "denominacio"),
				readOptionalString(rs, "nom"),
				rs.getLong("unitats"),
				Optional.ofNullable(rs.getBigDecimal("cost")).orElse(BigDecimal.ZERO),
				readOptionalString(rs, "divisa_cost").map(Divisa::getBySymbol)));
	}

	private ItemFerralla muntarItem(FilaFerralla fila, Map<Divisa, BigDecimal> canvis) {
		return ItemFerrallaImpl.builder()
				.artint(fila.artint())
				.clicod(fila.clicod())
				.articleClient(fila.codiFabrica().orElse("") + fila.clicod())
				.referencia(fila.referencia())
				.denominacio(fila.denominacio())
				.nomClient(fila.nomClient())
				.unitats(fila.unitats())
				.cost(fila.cost())
				.divisaCost(fila.divisaCost())
				.valoracio(valoracio(fila, canvis))
				.build();
	}

	/**
	 * Valoració de les unitats convertida a {@link #DIVISA_LLISTAT}. Delega a {@link Preu#imp(long)}
	 * perquè les divises de tipus cèntim (p.ex. {@code "EUR%"}) es divideixin per 100, i arrodoneix a
	 * 2 decimals com feia el {@code D_To_D} del Delphi. Si la peça no té divisa de cost o no s'ha
	 * trobat cap tipus de canvi, l'import queda a zero.
	 * <p>
	 * Com que el cost és constant dins de cada article-client, valorar la suma d'unitats dona el
	 * mateix que valorar moviment a moviment i sumar després, tret de l'arrodoniment: el Delphi
	 * arrodonia cada moviment a 2 decimals abans d'acumular-lo.
	 */
	private BigDecimal valoracio(FilaFerralla fila, Map<Divisa, BigDecimal> canvis) {
		var canvi = fila.divisaCost().map(Divisa::base).map(canvis::get);
		if (canvi.isEmpty())
			return BigDecimal.ZERO;
		return Preu.of(fila.cost(), fila.divisaCost().get()).imp(fila.unitats())
				.multiply(canvi.get())
				.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Factor de conversió a {@link #DIVISA_LLISTAT} de cada divisa de cost present al llistat. Es
	 * busca el canvi de la data final del període, que és la que el Delphi passava al {@code D_To_D},
	 * i si aquell dia no en té es recula dia a dia fins a trobar-ne un, de manera que un dia sense
	 * cotització (festius, caps de setmana) no deixi la peça sense valorar. Les divises que no
	 * arriben a tenir cap canvi no hi surten.
	 */
	private Map<Divisa, BigDecimal> obtenirCanvis(Collection<FilaFerralla> files, LocalDate dataFi) {
		var dataMinima = dataFi.minusMonths(MESOS_MAXIM_RECULADA_CANVI);
		var canvis = new HashMap<Divisa, BigDecimal>();
		var divises = files.stream()
				.map(FilaFerralla::divisaCost)
				.flatMap(Optional::stream)
				.map(Divisa::base)
				.distinct()
				.toList();
		for (var divisa : divises) {
			if (DIVISA_LLISTAT.equals(divisa)) {
				canvis.put(divisa, BigDecimal.ONE);
				continue;
			}
			var canvisPerDia = new ObtenirCanviDivisa().get(divisa, DIVISA_LLISTAT, dataMinima, dataFi);
			for (var data = dataFi; !data.isBefore(dataMinima); data = data.minusDays(1)) {
				var canvi = canvisPerDia.get(data);
				if (canvi != null && canvi.signum() > 0) {
					canvis.put(divisa, canvi);
					break;
				}
			}
			if (!canvis.containsKey(divisa))
				log.warn("Llistat de ferralla: sense tipus de canvi de {} a {} fins al {}; les peces "
						+ "amb aquesta divisa de cost queden valorades a zero",
						divisa.symbol(), DIVISA_LLISTAT.symbol(), dataFi);
		}
		return canvis;
	}

	/**
	 * El Delphi avortava el llistat sencer si un moviment no tenia article-client a {@code artcli.dbf}
	 * o client a {@code cli6.dbf}. Aquí es deixa passar, perquè una peça donada de baixa a Advantage
	 * surt de la cache però conserva els moviments de ferralla, i perdre tot el llistat per això no
	 * ajuda ningú. Queda constància al log i la peça surt sense descriptius i valorada a zero.
	 */
	private void avisarPecesSenseDescriptius(List<FilaFerralla> files) {
		var orfes = files.stream()
				.filter(f -> f.codiFabrica().isEmpty() || f.nomClient().isEmpty())
				.map(f -> f.artint() + "/" + f.clicod())
				.toList();
		if (!orfes.isEmpty())
			log.warn("Llistat de ferralla: {} article(s)-client sense fitxa a la cache, surten sense "
					+ "descriptius i valorats a zero: {}", orfes.size(), orfes);
	}

	private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
		var valor = Strings.nullToEmpty(rs.getString(column)).trim();
		return valor.isEmpty() ? Optional.empty() : Optional.of(valor);
	}

	/** Dates inicial i final del període, totes dues incloses, com les {@code sDIn}/{@code sDFi} del Delphi. */
	public record Periode(LocalDate dataInici, LocalDate dataFi) {}

	private record FilaFerralla(String artint, String clicod, Optional<String> codiFabrica,
			Optional<String> referencia, Optional<String> denominacio, Optional<String> nomClient,
			long unitats, BigDecimal cost, Optional<Divisa> divisaCost) {}

	@JsonDeserialize(builder = ItemFerrallaImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ItemFerralla {

		String artint();
		String clicod();

		/**
		 * Clau amb què el Delphi agrupava el llistat: codi de fàbrica de l'article-client seguit del
		 * codi de client, sense separador (el camp {@code ARTCLI} del dbf).
		 */
		String articleClient();

		Optional<String> referencia();
		Optional<String> denominacio();
		Optional<String> nomClient();

		/** Unitats de ferralla acumulades del període. Mai no és zero: aquestes files es descarten. */
		long unitats();

		/** Cost unitari vigent a la cache, en {@link #divisaCost()}. */
		BigDecimal cost();

		Optional<Divisa> divisaCost();

		/**
		 * {@code unitats × cost} convertit a euros al canvi de la data final del període i arrodonit a
		 * 2 decimals (el camp {@code IMPORTS} del dbf). Zero si no s'ha pogut convertir. Sempre en
		 * euros, sigui quina sigui {@link #divisaCost()}, de manera que la columna és sumable.
		 */
		BigDecimal valoracio();
	}

}
