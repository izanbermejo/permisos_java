package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.internal.auxiliar.ConditionGenerator;
import ames.comercial.albarans.internal.domain.linia.InformacioPesa;
import ames.comercial.albarans.internal.domain.linia.InformacioPesaImpl;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProviderInformacioArticleclient implements IProviderInformacioArticleclient {

    @Autowired JdbcTemplate jdbcTemplate;

    @Override
    public IProviderInformacioArticleclientResponse provide(Collection<KeyArticleClient> articleClients) {
        var conjuntArticleClients = Set.copyOf(articleClients);

        String placeholders = ConditionGenerator.generate(conjuntArticleClients, 2);
        String sql = """
          SELECT ac.*,
            (SELECT cc.nom FROM cache.cache_client cc WHERE cc.clicod = ac.clicod LIMIT 1) AS nom_client
          FROM cache.cache_article_client ac
          WHERE (ac.artint, ac.clicod) IN (%s)
        """.formatted(placeholders);

        Object[] params = conjuntArticleClients.stream()
                .flatMap(key -> Stream.of(
                        key.artint(),
                        key.clicod()
                )).toArray();

        List<InformacioArticleclient> listArticles = jdbcTemplate.query(sql,
                (rs, rowNum) ->
                    InformacioArticleclientImpl.builder()
                        .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                        .codiEmpresa(rs.getString("empresa"))
                        .codiEmpresaEntrega(rs.getString("empresa_entrega"))
                        .nomClient(Optional.ofNullable(rs.getString("nom_client")).orElse(""))
                        .matriu(rs.getString("codi_fabrica"))
                        .referencia(rs.getString("referencia"))
                        .nivellTecnic(rs.getString("nivell_tecnic"))
                        .denominacio(rs.getString("denominacio"))
                        .codiPartidaArantzelaria(rs.getString("partida_arant_codi"))
                        .partidaArantzelaria(rs.getString("partida_arant_partida"))
                        .descripcioPartidaArantzelaria(Optional.ofNullable(rs.getString("partida_arant_desc")).orElse(""))
                        .codiEan13(Optional.ofNullable(rs.getString("codi_ean13")))
                        .isVolUdi(rs.getBoolean("is_vol_udi"))
                        .diesCaducitat(rs.getInt("dies_caducitat"))
                        .codiFamilia(Optional.ofNullable(rs.getString("codi_familia")))
                        .unitatsEmbalatge(rs.getLong("unitats_embalatge"))
                        .bossesCaixa(rs.getLong("bosses_caixa"))
                        .caixesPalet(rs.getLong("caixes_palet"))
                        .pesUnitari(Optional.ofNullable(rs.getBigDecimal("pes")).orElse(BigDecimal.ZERO))
                        .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                        .build()
            , params);

        return new ProviderInformacioArticleclientResponse(listArticles);

    }

    public static class ProviderInformacioArticleclientResponse implements IProviderInformacioArticleclientResponse {

        List<InformacioArticleclient> informacioArticleclients;

        public ProviderInformacioArticleclientResponse(List<InformacioArticleclient> informacioArticleclients) {
            this.informacioArticleclients = informacioArticleclients;
        }

        @Override
        public InformacioArticleclient get(KeyArticleClient articleClient) {
            return informacioArticleclients.stream()
                    .filter(info -> info.articleClient().equals(articleClient))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No s'ha trobat informació per a l'article client: " + articleClient));
        }

        @Override
        public Empresa empresa() {
            Set<String> empreses = informacioArticleclients.stream()
                    .map(InformacioArticleclient::codiEmpresa)
                    .collect(Collectors.toSet());

            // Només pot haber una empresa per a tots els articles clients, si n'hi ha més d'una no es podrà fer l'albarà
            if (empreses.size() > 1)
                throw new RuntimeException("S'han trobat múltiples empreses per als articles clients: " + empreses);

            return Empresa.getByClau(empreses.iterator().next());
        }

        @Override
        public Empresa empresaDesti() {
            Set<String> empreses = informacioArticleclients.stream()
                    .map(InformacioArticleclient::codiEmpresaEntrega)
                    .collect(Collectors.toSet());

            // Només pot haber una empresa per a tots els articles clients, si n'hi ha més d'una no es podrà fer l'albarà
            if (empreses.size() > 1)
                throw new RuntimeException("S'han trobat múltiples empreses de traspàs per als articles clients: " + empreses);

            return Empresa.getByClau(empreses.iterator().next());
        }

    }

    @JsonDeserialize(builder = InformacioArticleclientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface InformacioArticleclient {
        KeyArticleClient articleClient();
        String codiEmpresa();
        String codiEmpresaEntrega();
        String nomClient();
        String matriu();
        String referencia();
        String nivellTecnic();
        String denominacio();
        String codiPartidaArantzelaria();
        String partidaArantzelaria();
        String descripcioPartidaArantzelaria();
        Optional<String> codiEan13();
        boolean isVolUdi();
        int diesCaducitat();
        Optional<String> codiFamilia();
        /** Unitats del nivell base d'embalatge (Advantage {@code ACLUCAI}); veure {@link InformacioPesa#unitatsEmbalatge()} */
        long unitatsEmbalatge();
        /** Bosses per caixa de l'article-client (Advantage {@code BOSXCAI}); 0 si no n'hi ha */
        long bossesCaixa();
        /** Caixes per palet de l'article-client (Advantage {@code ACLUCAP}); 0 si no n'hi ha */
        long caixesPalet();
        /** Pes unitari de la peça en grams (Advantage {@code ART.ARTPFIN}) */
        BigDecimal pesUnitari();
        Preu preu();

        /**
         * Cert si l'article-client pertany a l'empresa donada. Un article-client té dues empreses de
         * facturació: la pròpia ({@link #codiEmpresa()}) i la d'entrega ({@link #codiEmpresaEntrega()}),
         * que és la que fa possibles els traspassos entre empreses. L'stock de la peça pot viure a
         * qualsevol de les dues, perquè un traspàs d'empresa el mou de la primera a la segona.
         */
        default boolean isDeEmpresa(String empresa) {
            return codiEmpresa().equals(empresa) || codiEmpresaEntrega().equals(empresa);
        }

        default InformacioPesa toInformacioPesa() {
            return InformacioPesaImpl.builder()
                    .matriu(matriu())
                    .referencia(referencia())
                    .nivellTecnic(nivellTecnic())
                    .denominacio(denominacio())
                    .codiPartidaArantzelaria(codiPartidaArantzelaria())
                    .partidaArantzelaria(partidaArantzelaria())
                    .codiEan13(codiEan13())
                    .isVolUdi(isVolUdi())
                    .diesCaducitat(diesCaducitat())
                    .codiFamilia(codiFamilia())
                    .pesUnitari(pesUnitari())
                    .unitatsEmbalatge(unitatsEmbalatge())
                    .bossesCaixa(bossesCaixa())
                    .caixesPalet(caixesPalet())
                    .build();
        }

    }

}
