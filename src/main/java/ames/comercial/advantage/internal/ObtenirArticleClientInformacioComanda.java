package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponseImpl;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirArticleClientInformacioComanda implements IObtenirArticleClientInformacioComanda {

	@Autowired IObtenirFitxesMagatzem obtenirStockPesa;
	@Autowired LiniaComandaRepository liniaComandaRepository;

	private static final String SQL_BASE = """
			SELECT a.artint, a.aclfab, a.empcod, e.descrip as empresaDesc,
			a.empent, ee.descrip as empresaEntDesc,
			a.aclden, a.aclref, a.aclnvt,
			a.aclucai, a.aclucap, a.aclsts,	c.clicod, c.clinom, a.aclpre, a.acldiv,
			COALESCE(fe.diessor, c.diessor, ' ') as diessor,
			COALESCE(fe.diesres, c.diesres, 0) as diesres,
			a.aclmage, mage.descrip as "descripe", a.aclmags, mags.descrip as "descrips",
			mags.tipus as "tipusMagSortida", COALESCE(mags.diestra, 0) as "diesTransportMagSortida",
			c.obsmor, c.notes, c.notalb, a.comlcom,
			a.codfab as codiFabrica, f.descrip as descFabrica,
			SUBSTRING(c.clienv, 1, 2) "formaEnviament",
			SUBSTRING(c.clienv, 3, 3) "incoterm",
			SUBSTRING(c.clienv, 6, 4) "desti",
			c.clitra,
			c.empcod as empresaClient,
			fe.codcli is not null as isClientEDI,
			c.alertcom as comentariClient,
			a.alertcom as comentariArticle,
			c.bloimp, c.factri
			FROM comundb.artcli a
			LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod
			LEFT JOIN comundb.mag mage ON a.aclmage = mage.magcod
			LEFT JOIN comundb.mag mags ON a.aclmags = mags.magcod
			LEFT JOIN comundb.empreses e ON e.codi=a.empcod
			LEFT JOIN comundb.empreses ee ON ee.codi=a.empent
			LEFT JOIN fab f ON a.codfab = f.fabcod
			LEFT JOIN flagsedi fe ON a.clicod = fe.codcli
			WHERE\s
			""";

	public Optional<ArticleClientInformacioComandaResponse> executar(String articleClient) {
		// Si el codi d'article no té 13 (7 projecte + 6 client) caràcters no cal buscar ja que no es trobarà
		if (articleClient.trim().length() != 13)
			return Optional.empty();
		var projecte = articleClient.substring(0, 7).toUpperCase();
		var client = articleClient.substring(7, 13).toUpperCase();
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(SQL_BASE + "a.aclfab = ? AND c.clicod = ?;");
			statement.setString(1, projecte);
			statement.setString(2, client);
			return statement;
		};
		return executarQuery(prep);
	}

	public Optional<ArticleClientInformacioComandaResponse> executar(KeyArticleClient articleClient) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(SQL_BASE + "a.artint = ? AND a.clicod = ?;");
			statement.setString(1, articleClient.artint().toUpperCase());
			statement.setString(2, articleClient.clicod().toUpperCase());
			return statement;
		};
		return executarQuery(prep);
	}

	private Optional<ArticleClientInformacioComandaResponse> executarQuery(PreparedStatementProvider prep) {
		var optInformacioArticleClient = Optional.ofNullable(new AdvantageDao().query(prep, rsAction()));
		if (optInformacioArticleClient.isPresent()) {
			// Complementació amb els stocks i els dies de transport
			var infoArticleClient = optInformacioArticleClient.get();
			var stocks = obtenirStockPesa.executar(infoArticleClient.keyArticleClient());
			var optDataUltimaComanda = liniaComandaRepository.dataUltimaComanda(infoArticleClient.keyArticleClient());
			var newInfoArticleClient = ArticleClientInformacioComandaResponseImpl.builder()
					.from(infoArticleClient)
					.stocksMagatzems(stocks.fitxes())
					.stocksEstantsSatelit(stocks.fitxesSatelit())
					.dataUltimaComanda(optDataUltimaComanda)
					.build();
			optInformacioArticleClient = Optional.of(newInfoArticleClient);
		}
		return optInformacioArticleClient;
	}

	private ResultSetAction<ArticleClientInformacioComandaResponse> rsAction() {
		return rs -> {
			if (rs.next()) {
				return ArticleClientInformacioComandaResponseImpl.builder()
						.artint(rs.getString("artint"))
						.aclfab(rs.getString("aclfab"))
						.codiEmpresa(rs.getString("empcod"))
						.descEmpresa(rs.getString("empresaDesc"))
						.codiEmpresaEntrega(rs.getString("empent"))
						.descEmpresaEntrega(rs.getString("empresaEntDesc"))
						.codiEmpresaClient(rs.getString("empresaClient"))
						.denominacio(rs.getString("aclden"))
						.referencia(rs.getString("aclref"))
						.nivellTecnic(rs.getString("aclnvt"))
						.unitatsEmbalatge(rs.getLong("aclucai"))
						.numCaixesPalet(rs.getLong("aclucap"))
						.codiClient(rs.getString("clicod"))
						.nomClient(rs.getString("clinom"))
						.codiFabrica(rs.getString("codiFabrica"))
						.descFabrica(rs.getString("descFabrica"))
						.formaEnviament(FormaEnviament.getByCodi(rs.getString("formaEnviament")))
						.incoterm(Incoterm.valueOf(rs.getString("incoterm")))
						.desti(rs.getString("desti"))
						.codiTransportista(rs.getString("clitra"))
						.preu(rs.getBigDecimal("aclpre"))
						.divisa(rs.getString("acldiv"))
						.diesSortida(rs.getString("diessor"))
						.diesTransitClient(rs.getLong("diesres"))
						.magatzemEntrada(rs.getString("aclmage"))
						.magatzemEntradaDesc(rs.getString("descripe"))
						.magatzemSortidaDesc(rs.getString("descrips"))
						.magatzemSortida(rs.getString("aclmags"))
						.matazemSortidaPlataforma("P".equals(rs.getString("tipusMagSortida")))
						.diesTransitEntreMagatzems((rs.getLong("diesTransportMagSortida")))
						.notesMorositat(MapperUtils.readSafe(rs, "obsmor"))
						.notesClient(MapperUtils.readSafe(rs, "notes"))
						.notesLogistica(MapperUtils.readSafe(rs, "notalb"))
						.notesEmbalatge(MapperUtils.readSafe(rs, "comlcom"))
						.isClientEDI(rs.getBoolean("isClientEDI"))
						.isImpagament(MapperUtils.readOptionalString(rs, "bloimp").map(bloimp -> bloimp.equals("S")).orElse(false))
						.isClientProforma(MapperUtils.readOptionalString(rs, "factri").map(v -> v.equals("S")).orElse(false))
                        .comentariInternClient(MapperUtils.readSafe(rs,"comentariClient"))
                        .comentariInternArticle(MapperUtils.readSafe(rs,"comentariArticle"))
						.build();
			}
			return null;
		};
	}
}
