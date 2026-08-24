package ames.comercial.cache.articlesclient;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirArticlesClientCache {

    public List<RegistreCacheArticleClient> executar() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                    SELECT 
					    a.aclref, a.aclfab, a.aclden, a.artint, a.aclflg, a.clicod, a.empcod, a.empent, 
					    a.codfab, e.descrip as empresaDesc, ee.descrip as empresaEntDesc,
					    f.descrip, a.aclpre, a.acldiv, a.cost, a.divcost, a.preames, a.divames, 
					    a.aclstk, art.artpfin, a.bloqueostk, a.usubloq,
					    a.aclsts, a.codiean13, art.voludi, art.diescadu, art.artfam, fam.tabnom as "nomFamilia",
					    a.aclmage, mage.descrip as "descripe", a.aclmags, mags.descrip as "descrips",
					    a.aclucai, a.aclucap, a.bosxcai, a.tipus, a.acldel, a.comlcom,
					    a.aclnvt, a.aclsec, a.aclfenv,
						COALESCE(a.artara, art.artara) AS "codiPartida",
                  		par.partida,
                  		par.nom as "descpartara",
						a.resp2
					FROM comundb.artcli a
					LEFT JOIN comundb.art art ON art.artint = a.artint
					LEFT JOIN fab f ON a.codfab = f.fabcod
					LEFT JOIN comundb.mag mage ON a.aclmage = mage.magcod
					LEFT JOIN comundb.mag mags ON a.aclmags = mags.magcod
					LEFT JOIN comundb.empreses e ON e.codi=a.empcod
					LEFT JOIN comundb.empreses ee ON ee.codi=a.empent
					LEFT JOIN comundb.partara par ON COALESCE(a.artara, art.artara) = par.codi
					LEFT JOIN comundb.families fam ON fam.codi = art.artfam
					WHERE a.aclmage IS NOT NULL 
					    AND a.aclmags IS NOT NULL
				""");
        AdvantageDao.ResultSetAction<List<RegistreCacheArticleClient>> rsAction = rs -> {
            var result = new ArrayList<RegistreCacheArticleClient>();
            while (rs.next()) {
                result.add(RegistreCacheArticleClientImpl.builder()
                        .artint(rs.getString("artint"))
                        .flag(rs.getString("aclflg"))
                        .referencia(rs.getString("aclref"))
                        .article(rs.getString("aclfab"))
                        .codiClient(rs.getString("clicod"))
                        .empresa(rs.getString("empcod"))
                        .empresaDesc(rs.getString("empresaDesc"))
                        .empresaEntrega(rs.getString("empent"))
                        .empresaEntregaDesc(rs.getString("empresaEntDesc"))
                        .denominacio(rs.getString("aclden"))
                        .nivellTecnic(rs.getString("aclnvt"))
                        .isPesaSeguretat("S".equalsIgnoreCase(rs.getString("aclsec")))
                        .formaEnviament(rs.getString("aclfenv"))
                        .codiFabrica(rs.getString("codfab"))
                        .nomFabrica(rs.getString("descrip"))
                        .preu(Optional.ofNullable(rs.getBigDecimal("aclpre")).orElse(BigDecimal.ZERO))
                        .divisa(rs.getString("acldiv"))
                        .cost(readCost(rs))
                        .preuAmes(readPreuAmes(rs))
                        .stock(rs.getInt("aclstk"))
                        .stockMinim(rs.getInt("aclsts"))
                        .codiEan13(readOptionalString(rs, "codiean13"))
                        .isVolUdi(readOptionalString(rs, "voludi").map(v -> v.equals("S")).orElse(false))
                        .diesCaducitat(rs.getInt("diescadu"))
                        .codiFamilia(readOptionalString(rs, "artfam"))
                        .nomFamilia(readOptionalString(rs, "nomFamilia"))
                        .pes(Optional.ofNullable(rs.getBigDecimal("artpfin")).orElse(BigDecimal.ZERO))
                        .bloquejatStock(rs.getBoolean("bloqueostk"))
                        .usuariBloqueigStock(rs.getString("usubloq"))
                        .magatzemEntrada(rs.getString("aclmage"))
                        .magatzemEntradaDesc(rs.getString("descripe"))
                        .magatzemSortida(rs.getString("aclmags"))
                        .magatzemSortidaDesc(rs.getString("descrips"))
                        .unitatsEmbalatge(rs.getInt("aclucai"))
                        .bossesCaixa(rs.getInt("bosxcai"))
                        .caixesPalet(rs.getInt("aclucap"))
                        .tipus(TipusArticleClient.getByTipus(rs.getString("tipus")))
                        .codiProjectManager(rs.getString("acldel"))
                        .partidaArantCodi(readOptionalString(rs, "codiPartida").orElse(""))
                        .partidaArantDesc(readOptionalString(rs, "descpartara").orElse(""))
                        .partidaArantPartida(readOptionalString(rs, "partida").orElse(""))
                        .planificador(rs.getString("resp2"))
                        .notesEmbalatge(readOptionalString(rs, "comlcom"))
                        .build());
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    /**
     * El cost no sempre està informat. Només es considera informat quan té valor i és diferent de zero.
     * Si el cost està informat però no té divisa, s'agafa l'euro per defecte.
     */
    private Optional<Preu> readCost(ResultSet rs) throws SQLException {
        var cost = rs.getBigDecimal("cost");
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0)
            return Optional.empty();
        var divisa = readOptionalString(rs, "divcost").orElse(Divisa.EURO.symbol());
        return Optional.of(Preu.of(cost, divisa));
    }

    /**
     * El preu Ames (per traspassos) no sempre està informat. Només es considera informat quan té valor i és diferent de zero.
     * Si el preu AMES està informat però no té divisa, s'agafa l'euro per defecte.
     */
    private Optional<Preu> readPreuAmes(ResultSet rs) throws  SQLException {
        var preuAmes = rs.getBigDecimal("preames");
        if (preuAmes == null || preuAmes.compareTo(BigDecimal.ZERO) == 0)
            return Optional.empty();
        var divisa = readOptionalString(rs, "divames").orElse(Divisa.EURO.symbol());
        return Optional.of(Preu.of(preuAmes, divisa));
    }

    private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
        var s = rs.getString(column);
        return Strings.isNullOrEmpty(s) ? Optional.empty() : Optional.of(s);
    }

    @JsonDeserialize(builder = RegistreCacheArticleClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RegistreCacheArticleClient {
        String artint();
        String flag();
        String referencia();
        String article();
        String codiClient();
        String empresa();
        String empresaDesc();
        String empresaEntrega();
        String empresaEntregaDesc();
        String denominacio();
        String nivellTecnic();
        boolean isPesaSeguretat();
        String formaEnviament();
        String codiFabrica();
        String nomFabrica();
        BigDecimal preu();
        String divisa();
        Optional<Preu> cost();
        Optional<Preu> preuAmes();
        int stock();
        int stockMinim();
        Optional<String> codiEan13();
        boolean isVolUdi();
        int diesCaducitat();
        Optional<String> codiFamilia();
        Optional<String> nomFamilia();
        BigDecimal pes();
        boolean bloquejatStock();
        String usuariBloqueigStock();
        String magatzemEntrada();
        String magatzemEntradaDesc();
        String magatzemSortida();
        String magatzemSortidaDesc();
        int unitatsEmbalatge();
        int bossesCaixa();
        int caixesPalet();
        TipusArticleClient tipus();
        String codiProjectManager();
        String partidaArantCodi();
        String partidaArantDesc();
        String partidaArantPartida();
        String planificador();
        Optional<String> notesEmbalatge();
    }

}
