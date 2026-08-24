package ames.comercial.variacio;

import ames.comercial.advantage.IObtenirClientsInternsAds;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.ObtenirCanviDivisa;
import ames.comercial.advantage.internal.ObtenirPesPremsatAds;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.variacio.VariacioComandaRepository;
import ames.comercial.comandes.internal.infraestructure.variacio.VariacioComplReqImpl;
import ames.comercial.shared.Divisa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "tasca.variacio", havingValue = "true", matchIfMissing = false)
public class TascaComplementaVariacioComandes {

    private static final Logger logger = LoggerFactory.getLogger(TascaComplementaVariacioComandes.class);

    @Autowired VariacioComandaRepository variacioRepo;
    @Autowired ComandaRepository comandaRepo;
    @Autowired IObtenirClientsInternsAds obtenirClientsInternsAds;

    // Cada hora
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 600_000)
    public void executar() {
        // Obtenció de les línies pendents de complementar
        var pendents = variacioRepo.getPendentsComplementar();
        // Obtenció dels clients interns (invaliden el registre de la variació)
        var clientsInterns = obtenirClientsInternsAds.get();

        logger.info("INICI TASCA PER COMPLEMENTAR VARIACIÓ AMB REGISTRES: {}", pendents.size());
        // Per cada línia s'ha de complementar la seva informació
        for (var p : pendents) {
            var comanda = comandaRepo.find(p.liniaComanda().comanda()).orElseThrow();;
            var artcli = new ObtenirArticleClientAds().query(p.articleClient()).orElseThrow();
            var pes = new ObtenirPesPremsatAds().query(p.articleClient().artint());
            // En cas que la divisa no sigui EUR cal calcular el factor de canvi a EUR
            var factorCanvi = BigDecimal.ONE;
            if (!Divisa.EURO.equals(p.divisa().base())) {
                factorCanvi = new ObtenirCanviDivisa().get(p.divisa(), Divisa.EURO, p.dataVariacio());
            }

            var complRecord = VariacioComplReqImpl.builder()
                    .clientCodi(comanda.dades().client())
                    .clientDesc(comanda.dades().clientNom())
                    .codiPesa(artcli.article())
                    .referencia(artcli.referencia())
                    .fabrica(artcli.codiFabrica())
                    .projectManager(artcli.codiProjectManager())
                    .factEur(factorCanvi)
                    .pes(pes)
                    .isValid(clientsInterns.stream().noneMatch(ci -> ci.codi().equals(comanda.dades().client())))
                    .build();
            variacioRepo.complementa(p.liniaComanda(), p.datareg(), complRecord);
        }
    }

}
