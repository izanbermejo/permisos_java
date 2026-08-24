package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.consum.SortidaPlataforma;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbaraImpl;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.ServirComandaFIFO.SegmentServir;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda;
import ames.comercial.inventari.ext.ICrearMovimentSortida;
import ames.comercial.inventari.internal.domain.service.CrearMovimentSortidaRequestImpl;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentSortida.CrearMovimentSortidaRequest;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Afegeix línies de consum a una capçalera d'albarà de consum ja persistida.
 * <p>
 * Per cada peça consumida s'apliquen <b>dos repartiments FIFO independents</b> sobre la mateixa
 * quantitat, cadascun amb el seu conjunt de segments:
 * <ol>
 *   <li><b>Segments de servir</b> ({@link ServirComandaFIFO}): reparteixen la quantitat entre les
 *       línies de comanda pendents de l'article-client (per data sol·licitada), decrementant-ne el
 *       pendent de servir. Bloqueja si el pendent de servir no cobreix la quantitat.</li>
 *   <li><b>Segments de consum</b> ({@link ConsumirPendentTraspasFIFO}): reparteixen la quantitat
 *       entre les línies dels albarans de traspàs a plataforma, decrementant-ne el pendent de
 *       consumir. Bloqueja si el pendent de consumir no cobreix la quantitat. <b>Només si la
 *       plataforma està marcada per al SII</b> ({@link ComprovarMagatzemSII}): a la resta de
 *       plataformes les línies de traspàs no tenen pendent de consumir i no hi ha res a repartir.</li>
 * </ol>
 * Després crea una <b>única línia de consum</b> per peça (amb el preu i la informació de peça del
 * sistema, i la informació de comanda de la primera línia servida), genera <b>un moviment de sortida
 * per cada segment de servir</b> —cada moviment queda vinculat a la seva línia de comanda, per això el
 * servit es reflecteix a l'històric i es pot desfer— i registra la traçabilitat cap als traspassos a
 * {@code sortides_plataforma} (un registre per segment de consum), que és el que declara el procés del
 * SII. Als magatzems no marcats no hi ha segments de consum i, per tant, tampoc traçabilitat.
 * <p>
 * La capçalera sempre existeix abans ({@code CrearCapsaleraConsum}): les línies s'hi afegeixen d'una
 * en una des de {@code AfegirLiniaConsum}. La numeració de línia continua a partir de l'última
 * existent ({@code nextNumero}), per la qual cosa serveix tant per a albarans buits com amb línies.
 */
@Service
public class AfegirLiniesConsum {

    @Autowired ConsumirPendentTraspasFIFO consumirPendentTraspasFIFO;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;
    @Autowired ServirComandaFIFO servirComandaFIFO;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;
    @Autowired IProviderInformacioArticleclient providerInformacioArticleclient;
    @Autowired IObtenirInformacioLiniesComanda obtenirInformacioLiniesComanda;
    @Autowired ICrearMovimentSortida crearMovimentSortida;

    public void executar(Albara capsalera, List<PecaConsum> peces) {
        var empresa = capsalera.id().empresa();
        var magatzem = capsalera.magatzem();
        // El pendent de consumir dels traspassos i la traçabilitat cap al SII només existeixen als
        // magatzems marcats; a la resta el consum es fa igualment, però no descompta res ni deixa apunt
        boolean isSii = comprovarMagatzemSII.executar(magatzem);

        // Informació d'article-client (preu i dades de peça) obtinguda del sistema, com en la resta d'albarans
        var infoArticles = providerInformacioArticleclient.provide(
                peces.stream().map(PecaConsum::articleClient).toList());

        long numeroLinia = liniaAlbaraRepository.nextNumero(capsalera.id());
        for (var peca : peces) {
            var infoArticle = infoArticles.get(peca.articleClient());
            // 1) Servit de comanda FIFO: serveix les línies de comanda pendents (per data sol·licitada),
            //    decrementant-ne el pendent de servir. Bloqueja si el pendent no cobreix la quantitat.
            //    Es fa primer per avortar la transacció abans de tocar cap altra dada.
            var segmentsServir = servirComandaFIFO.executar(Empresa.getByClau(empresa), peca.articleClient(), peca.quantitat());
            // 2) Consum FIFO: descompta el pendent de consumir de les línies de traspàs a plataforma
            //    (pot afectar-ne diverses). Bloqueja si el pendent de consumir no cobreix la quantitat.
            //    Només als magatzems marcats per al SII; als altres no hi ha pendent que descomptar.
            var segmentsConsum = isSii
                    ? consumirPendentTraspasFIFO.executar(magatzem, empresa, peca.articleClient(), peca.quantitat())
                    : List.<ConsumirPendentTraspasFIFO.SegmentConsum>of();

            // Informació de comanda de la línia de consum (com en una línia de sortida). Si el servit ha
            // repartit la quantitat entre diverses línies de comanda, s'agafa la de la primera servida.
            var infoComanda = infoComandaPrimeraLiniaServida(segmentsServir);

            // Una única línia d'albarà per peça, amb el preu del sistema (no de les línies de traspàs)
            var linia = LiniaAlbaraImpl.builder()
                    .id(KeyLiniaAlbara.of(capsalera.id(), numeroLinia++))
                    .articleClient(peca.articleClient())
                    .informacioPesa(infoArticle.toInformacioPesa())
                    .infoComanda(infoComanda)
                    .quantitat(peca.quantitat())
                    .preu(infoArticle.preu())
                    .isPreuFixat(false)
                    .quantitatPendentFacturar(peca.quantitat())
                    .quantitatPendentConsumir(0)
                    .identificadorConsum(peca.identificadorConsum())
                    .build();
            liniaAlbaraRepository.save(linia);
            ReplicaAdvantage.instance().addLiniaAlbaraInsert(linia);

            // Un moviment de sortida (SORTIDA amb client) per cada segment de servir, vinculat a la seva
            // línia de comanda. La suma dels segments de servir és igual a la quantitat de la peça (el
            // servit FIFO bloqueja si no la cobreix), de manera que l'stock descomptat és el total consumit.
            for (var segmentServir : segmentsServir) {
                crearMovimentSortida.executar(buildMovimentRequest(capsalera, linia, segmentServir));
            }

            // Traçabilitat cap als traspassos: un registre per cada segment de consum (línia de traspàs
            // FIFO consumida), tots contra aquesta línia de consum, per poder-ne restablir el pendent en
            // desfer. Als magatzems no marcats per al SII no hi ha segments i no s'escriu res.
            for (var segmentConsum : segmentsConsum) {
                sortidaPlataformaRepository.save(new SortidaPlataforma(
                        linia.id(),
                        capsalera.magatzem(),
                        capsalera.data(),
                        peca.articleClient(),
                        segmentConsum.quantitat(),
                        segmentConsum.origen(),
                        segmentConsum.magatzemTraspas(),
                        segmentConsum.dataTraspas(),
                        segmentConsum.albtraspasQuantitat()));
            }
        }
    }

    /**
     * Informació de comanda per a la línia de consum, obtinguda de la primera línia de comanda servida
     * (per data sol·licitada). Encara que el servit FIFO pot repartir la quantitat entre diverses línies
     * de comanda, la línia de consum en referencia només una (la primera), igual que una línia de sortida.
     */
    private InformacioComanda infoComandaPrimeraLiniaServida(List<SegmentServir> segmentsServir) {
        var primeraLinia = segmentsServir.get(0).clauLinia();
        var dto = obtenirInformacioLiniesComanda.executar(List.of(primeraLinia)).get(0);
        return InformacioComanda.of(primeraLinia.comanda(), dto.comandaSegonsClient(), dto.programa());
    }

    private CrearMovimentSortidaRequest buildMovimentRequest(Albara albara, LiniaAlbara linia, SegmentServir segmentServir) {
        return CrearMovimentSortidaRequestImpl.builder()
                .articleClient(linia.articleClient())
                .empresa(albara.id().empresa())
                .magatzem(albara.magatzem())
                .data(albara.data())
                .quantitat(segmentServir.quantitat())
                .client(albara.client().orElseThrow())
                .liniaAlbara(linia.id())
                // El consum serveix una línia de comanda pendent (FIFO): el moviment hi queda vinculat
                .liniaComanda(segmentServir.clauLinia())
                .build();
    }

    /** Peça a consumir: article-client, quantitat i identificador de consum opcional. */
    public record PecaConsum(KeyArticleClient articleClient, long quantitat, Optional<String> identificadorConsum) {}

}
