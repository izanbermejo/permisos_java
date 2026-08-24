package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.consum.SortidaPlataforma;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Retorn de mercaderia d'un magatzem plataforma marcat per al SII cap a un magatzem d'AMES: la
 * mercaderia surt de la plataforma sense passar per cap client, de manera que s'ha de descomptar el
 * pendent de consumir dels traspassos que hi havien entrat (FIFO) i deixar-ne apunt a
 * {@code albarans.sortides_plataforma} perquè el procés del SII el declari.
 * <p>
 * És el mateix mecanisme que el consum ({@link AfegirLiniesConsum}); l'única diferència és que la
 * línia de sortida no és la d'un albarà de consum sinó la del propi albarà de traspàs de retorn. Per
 * això els registres que en surten es poden desfer amb {@link DesferConsumPlataforma}, igual que els
 * dels consums.
 * <p>
 * <b>Compte amb els magatzems del registre</b>: en un retorn s'hi desen amb la convenció del Delphi,
 * que és la inversa de la d'un consum (veure {@link SortidaPlataforma}).
 * <p>
 * Un albarà és un retorn de plataforma quan surt d'una plataforma marcada i el magatzem receptor no
 * és una plataforma. Els traspassos entre plataformes en queden fora: la mercaderia no surt del
 * circuit, i el traspàs a la plataforma de destí ja hi genera el seu propi pendent de consumir. Els
 * que canvien d'empresa també, perquè un retorn no en canvia mai.
 * <p>
 * Si el pendent de consumir de la plataforma no cobreix la quantitat retornada, es llença
 * {@code PendentConsumirInsuficient} i s'avorta la transacció, igual que en un consum: vol dir que
 * l'stock de la plataforma i els traspassos que hi han entrat no quadren.
 */
@Service
public class RegistrarRetornPlataforma {

    @Autowired IObtenirMagatzems obtenirMagatzems;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;
    @Autowired ConsumirPendentTraspasFIFO consumirPendentTraspasFIFO;
    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;

    /**
     * Registra la sortida d'una peça si l'albarà és un retorn de plataforma; si no ho és, no fa res.
     * S'ha de cridar tant en crear una línia nova com en incrementar-ne una d'existent, amb la
     * quantitat que s'hi afegeix en cada cas.
     */
    public void registrar(Albara albara, KeyLiniaAlbara idLinia, KeyArticleClient articleClient, long quantitat) {
        if (!isRetornPlataforma(albara)) {
            return;
        }
        var plataforma = albara.magatzem();
        var segments = consumirPendentTraspasFIFO.executar(
                plataforma, albara.id().empresa(), articleClient, quantitat);
        for (var segment : segments) {
            sortidaPlataformaRepository.save(new SortidaPlataforma(
                    idLinia,
                    // Convenció del Delphi per als retorns (mantalb.pas:3834 i 3842): el magatzem de la
                    // sortida és el que rep la mercaderia i el del traspàs és la plataforma d'on surt,
                    // al revés que en un consum. És el que espera el procés que genera els fitxers del SII.
                    albara.magatzemTraspas(),
                    albara.data(),
                    articleClient,
                    segment.quantitat(),
                    segment.origen(),
                    plataforma,
                    segment.dataTraspas(),
                    segment.albtraspasQuantitat()));
        }
    }

    /**
     * Cert si l'albarà treu mercaderia d'una plataforma marcada per al SII cap a un magatzem d'AMES.
     * <p>
     * Ha de ser un {@code TRASPAS_MAGATZEM}: un retorn de plataforma no canvia mai d'empresa, perquè la
     * mercaderia que hi ha a la plataforma continua sent de l'empresa que la hi va enviar. El tipus és,
     * a més, el que despatxa les estratègies que desfan el retorn i que n'hi propaguen el canvi de data
     * ({@code EliminarAlbaraRetornPlataforma}, {@code CanviarDataAlbaraRetornPlataforma}), de manera que
     * registrar-ne un amb un altre tipus deixaria apunts que després ningú no desfaria.
     */
    public boolean isRetornPlataforma(Albara albara) {
        if (albara.tipus() != TipusAlbara.TRASPAS_MAGATZEM || !comprovarMagatzemSII.executar(albara.magatzem())) {
            return false;
        }
        return obtenirMagatzems.get(albara.magatzemTraspas())
                .map(m -> !m.isPlataforma())
                .orElse(false);
    }

}
