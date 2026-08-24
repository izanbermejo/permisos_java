package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.AlbaraNoTraspas;
import ames.comercial.albarans.AlbaransException.ArticleAltraEmpresa;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.services.CalcularAvisosEmbalatge;
import ames.comercial.albarans.internal.services.IProviderInformacioArticleclient;
import ames.comercial.albarans.internal.services.PreusProviderSelector;
import ames.comercial.albarans.internal.services.ProviderInformacioArticleclient.InformacioArticleclient;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.magatzem.internal.domain.Magatzem;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.SharedExceptions.MagatzemNoExisteix;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Serveix les dues consultes que acompanyen l'alta manual d'una línia d'albarà de traspàs, abans de
 * persistir-la amb {@code AfegirLiniaTraspas}:
 * <ul>
 *   <li>{@link #preparar} — en seleccionar l'article-client al buscador: dades de la peça, preu proposat
 *       (el mateix que aplicaria la creació automàtica de traspàs) i stock actual al magatzem d'origen.</li>
 *   <li>{@link #validar} — en acceptar la quantitat i el preu: els avisos que l'usuari ha de confirmar
 *       (quantitat no múltiple de l'embalatge i stock resultant negatiu). Cap dels dos impedeix afegir la
 *       línia: són informatius i el frontend els mostra perquè l'usuari confirmi o torni enrere.</li>
 * </ul>
 * Les dues comproven que l'article-client sigui de l'empresa d'origen de l'albarà, cosa que sí que és
 * bloquejant: l'stock surt de la fitxa d'inventari de l'empresa de l'article.
 */
@Service
public class PrevisualitzarAltaLiniaTraspas {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired IObtenirMagatzems obtenirMagatzems;
    @Autowired IProviderInformacioArticleclient providerInformacioArticleclient;
    @Autowired PreusProviderSelector preusProvider;
    @Autowired IObtenirStocks obtenirStocks;
    @Autowired CalcularAvisosEmbalatge calcularAvisosEmbalatge;

    @Transactional(readOnly = true)
    public PreparacioLiniaTraspasResponse preparar(KeyAlbara idAlbara, KeyArticleClient articleClient) {
        var context = context(idAlbara, articleClient);
        var info = context.info();

        // Preu proposat: tarifa AMES si el traspàs es factura (canvi d'empresa i no abonable, decidit ja a
        // la capçalera) i tarifa de client si no. És el mateix criteri que després aplica AfegirLiniaTraspas.
        var preu = preusProvider.provide(context.albara().isTraspasFacturable(), Set.of(articleClient))
                .getOrDefault(articleClient, Preu.of(BigDecimal.ZERO, info.preu().divisa()));

        return new PreparacioLiniaTraspasResponse(
                info.matriu(), info.referencia(), info.nivellTecnic(), info.denominacio(), info.nomClient(),
                info.unitatsEmbalatge(), info.caixesPalet(),
                preu.valor(), preu.divisa().symbol(),
                stockActual(context));
    }

    @Transactional(readOnly = true)
    public ValidacioLiniaTraspasResponse validar(KeyAlbara idAlbara, KeyArticleClient articleClient, long quantitat) {
        var context = context(idAlbara, articleClient);
        var info = context.info();

        var avisEmbalatge = calcularAvisosEmbalatge.calcular(quantitat, info.unitatsEmbalatge(), info.caixesPalet(),
                context.magatzemOrigen().isControlatInternament());

        long stockActual = stockActual(context);
        long stockResultant = stockActual - quantitat;

        return new ValidacioLiniaTraspasResponse(
                avisEmbalatge.map(Enum::name).orElse(null),
                avisEmbalatge.map(a -> a.severitat().name()).orElse(null),
                info.unitatsEmbalatge(), info.caixesPalet(),
                stockActual, quantitat, stockResultant, stockResultant < 0);
    }

    /** Stock de la peça a la fitxa d'inventari de l'empresa emissora de l'albarà, al magatzem d'origen */
    private long stockActual(ContextLinia context) {
        return obtenirStocks
                .query(context.info().articleClient(), context.empresaOrigen(), context.magatzemOrigen().codi())
                .map(s -> s.stock())
                .orElse(0L);
    }

    /**
     * Carrega i valida el context comú de les dues consultes: l'albarà ha d'existir i ser de traspàs, el
     * magatzem d'origen ha d'existir i la peça ha de ser de l'empresa emissora de l'albarà. El magatzem de
     * destí no cal rellegir-lo: el que se'n necessitava (si el traspàs es factura) ja el diu la capçalera.
     */
    private ContextLinia context(KeyAlbara idAlbara, KeyArticleClient articleClient) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        if (!albara.isTraspas()) {
            throw new AlbaraNoTraspas(idAlbara);
        }
        var magatzemOrigen = obtenirMagatzems.get(albara.magatzem())
                .orElseThrow(() -> new MagatzemNoExisteix(albara.magatzem()));

        var info = providerInformacioArticleclient.provide(Set.of(articleClient)).get(articleClient);
        checkEmpresa(albara, info);

        return new ContextLinia(albara, Empresa.getByClau(albara.id().empresa()), magatzemOrigen, info);
    }

    /**
     * L'article-client ha de pertànyer a l'empresa que emet l'albarà, entenent per pertànyer qualsevol de
     * les seves dues empreses de facturació (veure {@link InformacioArticleclient#isDeEmpresa}): l'stock
     * d'una peça viu tant a la seva empresa com a la d'entrega, perquè els traspassos entre empreses el
     * mouen d'una a l'altra i des d'allà encara se'n poden fer de nous.
     */
    private void checkEmpresa(Albara albara, InformacioArticleclient info) {
        if (!info.isDeEmpresa(albara.id().empresa())) {
            throw new ArticleAltraEmpresa(info.matriu(), info.codiEmpresa(), info.codiEmpresaEntrega(),
                    albara.id().empresa());
        }
    }

    private record ContextLinia(Albara albara, Empresa empresaOrigen, Magatzem magatzemOrigen,
                                InformacioArticleclient info) {}

    public record PreparacioLiniaTraspasResponse(
            String matriu,
            String referencia,
            String nivellTecnic,
            String denominacio,
            String nomClient,
            /** Unitats per caixa de l'article-client */
            long unitatsEmbalatge,
            /** Caixes per palet de l'article-client; 0 si no n'hi ha */
            long caixesPalet,
            /** Preu proposat per a la línia; l'usuari el pot sobreescriure */
            BigDecimal preu,
            String divisa,
            /** Stock actual de la peça al magatzem d'origen de l'albarà */
            long stockActual
    ) {}

    public record ValidacioLiniaTraspasResponse(
            /** Tipus d'avís d'embalatge ({@link ames.comercial.albarans.internal.services.AvisEmbalatge}), o null */
            String avisEmbalatge,
            /** Severitat de l'avís d'embalatge (INFO/AVIS/ERROR), o null */
            String severitatEmbalatge,
            long unitatsEmbalatge,
            long caixesPalet,
            long stockActual,
            long quantitat,
            long stockResultant,
            // Sense el @JsonProperty explícit, Jackson tractaria l'accessor com un is-getter i el
            // publicaria com a "stockNegatiu"
            @JsonProperty("isStockNegatiu") boolean isStockNegatiu
    ) {}

}
