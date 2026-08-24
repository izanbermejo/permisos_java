package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.AlbaraNoTraspas;
import ames.comercial.albarans.AlbaransException.AlbaraTancat;
import ames.comercial.albarans.AlbaransException.ArticleAltraEmpresa;
import ames.comercial.albarans.AlbaransException.PecaJaAlAlbara;
import ames.comercial.albarans.AlbaransException.QuantitatLiniaInvalida;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbaraImpl;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.ComprovarMagatzemSII;
import ames.comercial.albarans.internal.services.IProviderInformacioArticleclient;
import ames.comercial.albarans.internal.services.PreusProviderSelector;
import ames.comercial.albarans.internal.services.RegistrarRetornPlataforma;
import ames.comercial.albarans.internal.services.ProviderInformacioArticleclient.InformacioArticleclient;
import ames.comercial.inventari.ext.CrearMovimentTraspasMagatzemEmpresaRequestImpl;
import ames.comercial.inventari.ext.ICrearMovimentTraspasMagatzemEmpresa;
import ames.comercial.inventari.ext.ICrearMovimentTraspasMagatzemEmpresa.CrearMovimentTraspasMagatzemEmpresaRequest;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Afegeix manualment una línia a un albarà de traspàs obert, generant-ne el moviment de traspàs
 * corresponent. És el segon pas de l'alta manual d'un traspàs, després de
 * {@link CrearAlbaraTraspasManual}, i també serveix per afegir línies a un traspàs ja existent.
 * <p>
 * La quantitat i el preu els posa l'usuari. Els avisos que hagi hagut de confirmar (embalatge i stock
 * negatiu) els calcula {@code PrevisualitzarAltaLiniaTraspas} i són purament informatius: aquí no es
 * tornen a comprovar perquè no impedeixen res. Sí que es revalida el que és bloquejant (albarà
 * modificable, peça de l'empresa d'origen i quantitat positiva), que no pot dependre del frontend.
 */
@Service
public class AfegirLiniaTraspas {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired IProviderInformacioArticleclient providerInformacioArticleclient;
    @Autowired PreusProviderSelector preusProvider;
    @Autowired ICrearMovimentTraspasMagatzemEmpresa crearMovimentTraspas;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;
    @Autowired RegistrarRetornPlataforma registrarRetornPlataforma;

    @Transactional
    public KeyLiniaAlbara executar(KeyAlbara idAlbara, AfegirLiniaTraspasRequest request) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        if (!albara.isTraspas()) {
            throw new AlbaraNoTraspas(idAlbara);
        }
        // No es poden modificar les línies d'un albarà facturat ni amb moviments de magatzem actius
        albara.checkPotModificarLinies();
        // Ni afegir-ne a un albarà tancat (cal reobrir-lo abans)
        if (albara.isTancat()) {
            throw new AlbaraTancat(idAlbara);
        }
        if (request.quantitat() <= 0) {
            throw new QuantitatLiniaInvalida(request.quantitat());
        }

        var info = providerInformacioArticleclient.provide(Set.of(request.articleClient())).get(request.articleClient());
        checkEmpresa(albara, info);
        checkPecaNoRepetida(idAlbara, request.articleClient(), info.matriu());

        // Si el traspàs es factura o no ja va quedar decidit en crear la capçalera: creua empreses i no
        // s'ha marcat com a abonable
        boolean isFacturable = albara.isTraspasFacturable();
        // Les línies dels traspassos cap a una plataforma del SII neixen amb tota la quantitat pendent de
        // consumir, que és el que després descompten els consums i els retorns d'aquella plataforma
        boolean isConsumible = albara.tipus() == TipusAlbara.TRASPAS_PLATAFORMA
                && comprovarMagatzemSII.executar(albara.magatzemTraspas());

        var preu = Preu.of(request.preu(), request.divisa());
        var linia = LiniaAlbaraImpl.builder()
                .id(KeyLiniaAlbara.of(idAlbara, liniaAlbaraRepository.nextNumero(idAlbara)))
                .articleClient(request.articleClient())
                .informacioPesa(info.toInformacioPesa())
                .quantitat(request.quantitat())
                .preu(preu)
                // Es marca com a preu fixat només si l'usuari ha canviat el preu que se li proposava
                .isPreuFixat(!preu.isEquals(preuProposat(isFacturable, request.articleClient(), info)))
                .quantitatPendentConsumir(isConsumible ? request.quantitat() : 0)
                .quantitatPendentFacturar(isFacturable ? request.quantitat() : 0)
                .build();

        liniaAlbaraRepository.save(linia);
        crearMovimentTraspas.executar(buildMoviment(albara, linia));
        ReplicaAdvantage.instance().addLiniaAlbaraInsert(linia);
        // Si el traspàs treu mercaderia d'una plataforma del SII, la línia n'és una sortida i s'ha de
        // descomptar el pendent de consumir dels traspassos que hi havien entrat
        registrarRetornPlataforma.registrar(albara, linia.id(), linia.articleClient(), linia.quantitat());
        // En un traspàs abonable, en comptes de facturar-se es genera el pendent d'abonar de la línia
        if (albara.isTraspasAbonable()) {
            ReplicaAdvantage.instance().addPenAboInsert(albara, linia);
        }

        return linia.id();
    }

    /**
     * A un albarà de traspàs una peça només hi pot tenir una línia. El pendent d'abonar de l'Advantage
     * ({@code penabo}) no té número de línia, de manera que dues línies de la mateixa peça hi generarien
     * registres indistingibles i eliminar-ne una se'ls enduria tots dos. Es controla sempre, i no només
     * en els abonables, perquè un albarà es pot marcar com a abonable després de tenir línies.
     */
    private void checkPecaNoRepetida(KeyAlbara idAlbara, KeyArticleClient articleClient, String matriu) {
        liniaAlbaraRepository.findByAlbara(idAlbara).stream()
                .filter(l -> l.articleClient().equals(articleClient))
                .findFirst()
                .ifPresent(l -> {
                    throw new PecaJaAlAlbara(matriu, l.id().liniaFormat());
                });
    }

    private Preu preuProposat(boolean isFacturable, KeyArticleClient articleClient, InformacioArticleclient info) {
        return preusProvider.provide(isFacturable, Set.of(articleClient))
                .getOrDefault(articleClient, Preu.of(BigDecimal.ZERO, info.preu().divisa()));
    }

    /** La peça ha de ser d'alguna de les empreses de l'article-client (veure {@code PrevisualitzarAltaLiniaTraspas}) */
    private void checkEmpresa(Albara albara, InformacioArticleclient info) {
        if (!info.isDeEmpresa(albara.id().empresa())) {
            throw new ArticleAltraEmpresa(info.matriu(), info.codiEmpresa(), info.codiEmpresaEntrega(),
                    albara.id().empresa());
        }
    }

    private CrearMovimentTraspasMagatzemEmpresaRequest buildMoviment(Albara albara, LiniaAlbara linia) {
        return CrearMovimentTraspasMagatzemEmpresaRequestImpl.builder()
                .articleClient(linia.articleClient())
                .idLiniaAlbara(linia.id())
                .data(albara.data())
                .quantitat(linia.quantitat())
                .empresaOrigen(Empresa.getByClau(albara.id().empresa()))
                .magatzemOrigen(albara.magatzem())
                .empresaDesti(Empresa.getByClau(albara.empresaTraspas()))
                .magatzemDesti(albara.magatzemTraspas())
                .build();
    }

    @JsonDeserialize(builder = AfegirLiniaTraspasRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AfegirLiniaTraspasRequest {
        KeyArticleClient articleClient();
        long quantitat();
        BigDecimal preu();
        String divisa();
    }

}
