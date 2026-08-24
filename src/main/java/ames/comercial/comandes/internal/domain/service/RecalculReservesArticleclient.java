package ames.comercial.comandes.internal.domain.service;

import ames.comercial.calculadorareserves.request.CalculadoraReservesReq.LiniaCalculReservesReq;
import ames.comercial.calculadorareserves.request.CalculadoraReservesReqImpl;
import ames.comercial.calculadorareserves.response.CalculadoraReservesResp;
import ames.comercial.calculadorareserves.service.CalculadoraReserves;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.internal.application.command.RecaculcarServibleComanda;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.IProviderDiesReserva;
import ames.comercial.inventari.ext.IActualitzarReservaArticleclient;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RecalculReservesArticleclient implements IRecalculReservesArticleclient {

    ApplicationEventPublisher applicationEventPublisher;
    ComandaRepository comandaRepo;
    LiniaComandaRepository liniaRepo;
    IProviderDiesReserva providerDiesReserva;
    IObtenirStocks obtenirStocks;
    IActualitzarReservaArticleclient actualitzarReservaArticleclient;
    RecaculcarServibleComanda recaculcarServibleComanda;

    public RecalculReservesArticleclient(ApplicationEventPublisher applicationEventPublisher,
                                         ComandaRepository comandaRepo, LiniaComandaRepository liniaRepo,
                                         IProviderDiesReserva providerDiesReserva, IObtenirStocks obtenirStocks,
                                         IActualitzarReservaArticleclient actualitzarReservaArticleclient,
                                         RecaculcarServibleComanda recaculcarServibleComanda) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.comandaRepo = comandaRepo;
        this.liniaRepo = liniaRepo;
        this.providerDiesReserva = providerDiesReserva;
        this.obtenirStocks = obtenirStocks;
        this.actualitzarReservaArticleclient = actualitzarReservaArticleclient;
        this.recaculcarServibleComanda = recaculcarServibleComanda;
    }

    @Transactional
    @Override
    public void executar(KeyLiniaComanda linia) {
        // La línia de comanda ha d'existir
        var liniaSolicitada = liniaRepo.find(linia).orElseThrow(LiniaComandaNoExisteix::new);
        // La comanda ha d'existir
        var comanda = comandaRepo.find(linia.comanda()).orElseThrow(ComandaNoExisteix::new);
        // Empresa de la comanda
        var empresa = Empresa.getByClau(comanda.dades().empresa());
        //  En cas que ja estigui servida no es fa res
        if (liniaSolicitada.servida())
            return;
        // En cas que es sol·licita reserva una liniaSolicitada amb tot el pendent ja reservat no es fa res
        if (liniaSolicitada.quantitatPendent() == liniaSolicitada.quantitatReservada())
            return;
        executar(liniaSolicitada.articleClient(), empresa, liniaSolicitada);
    }

    @Transactional
    @Override
    public void executar(KeyArticleClient articleClient, Empresa empresa) {
        executar(articleClient, empresa, null);
    }

    private void executar (KeyArticleClient articleClient, Empresa empresa, LiniaComanda liniaSolicitada) {
        // Càlcul de la data màxima on es poden sol·licitar reserves
        var dataMaxReserva = dataMaximaReserva();
        // Obtenció de l'stock total de la peça
        var stockPesa = obtenirStocks.query(articleClient, empresa);
        var stockTotal = stockPesa.map(Stock::stock).orElse(0L);
        // Línies pendents de servir de l'article client
        var liniesPendents = liniaRepo.findByArticlePendent(articleClient, empresa);
        // Càlcul de les línies que han de sol·licitar reserves (tenen pendent de reservar i no superen la data)
        var liniesPendentsReserva = liniesPendents.stream()
                .filter(l -> l.dataSolicitada().isBefore(dataMaxReserva))
                .toList();
        // Càlcul de les reserves
        Optional<LiniaCalculReservesReq> optLiniaPreferent = liniaSolicitada == null
                ? Optional.empty()
                : Optional.of(CalculadoraReserves.buildRequest(liniaSolicitada));
        var respServ = new CalculadoraReserves(CalculadoraReservesReqImpl.builder()
                .stock(stockTotal)
                .linies(liniesPendentsReserva.stream().map(CalculadoraReserves::buildRequest).toList())
                .liniaPreferent(optLiniaPreferent)
                .build()).executar();
        // Obtenció de les linies modificades en el procés de recalcul
        var liniesModificades = liniesAmbReservaModificada(liniesPendentsReserva, liniaSolicitada, respServ);
        // Es guarden les línies modificades
        liniaRepo.save(liniesModificades);
        var reservaFinal = respServ.stockReservat();
        // Recàlcul del reservable de les comandes que pot ser han canviat
        recalculServible(liniesModificades);
        // Actualització de l'stock reservat a l'inventari
        actualitzarReservaArticleclient.executar(articleClient, empresa, reservaFinal);
    }

    private LocalDate dataMaximaReserva() {
        var diesMaxReserva = providerDiesReserva.provide();
        return RequestThread.dateLocal().plusDays(diesMaxReserva).plusDays(1);
    }

    private List<LiniaComanda> liniesAmbReservaModificada(List<LiniaComanda> linies, LiniaComanda liniaPreferent, CalculadoraReservesResp resultatReserva) {
        var result = new ArrayList<LiniaComanda>();
        // S'ha d'afegir la línia preferent en cas que estigui definida
        var liniesTotal = new ArrayList<LiniaComanda>(linies);
        if (liniaPreferent != null)
            liniesTotal.add(liniaPreferent);
        for (var linia : resultatReserva.linies()) {
            // S'actualitza la reserva per cada línia que ha canviat la seva quantitat reservada
            var liniaOriginal = liniesTotal.stream().filter(l -> l.id().equals(linia.clauLinia())).findAny().orElseThrow();
            if (linia.quantitatReservada() != liniaOriginal.quantitatReservada())
                result.add(liniaOriginal.actualitzarReserva(linia.quantitatReservada()));
        }
        return result;
    }

    private void recalculServible(List<LiniaComanda> linies) {
        // Per cada identificador s'ha de recalcular si és servible
        // Recàlcul del servible de les comandes que han canviat les seves línies
        Set<Long> codisComanda = linies.stream()
                .map(l -> l.id().comanda())
                .collect(Collectors.toSet());
        codisComanda.forEach(recaculcarServibleComanda::executar);
    }

}
