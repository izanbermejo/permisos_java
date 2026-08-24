package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.advantage.IObtenirEmpresesAds.EmpresaAds;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.albarans.internal.application.query.ObtenirQuantitatServidaAcumulada;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.services.aviexp.BA.GenerarBAFactory;
import ames.comercial.albarans.internal.services.aviexp.ET.GenerarETFactory;
import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.comandes.ext.IObtenirClausComandesEdi.DataSolicitadaComandaEdi;
import ames.comercial.comandes.service.IProviderPes;
import ames.comercial.edi2.internal.application.ObtenirComandaEdi2;
import ames.comercial.edi2.internal.application.ObtenirEmbalatgesExpedicio;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEdiRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class GenerarBlocLinia {

    @Autowired ObtenirEmbalatgesExpedicio obtenirEmbalatgesExpedicio;
    @Autowired IProviderPes providerPes;
    @Autowired ObtenirComandaEdi2 obtenirComandaEdi;
    @Autowired ObtenirArticleClientAds obtenirArticleClientAds;
    @Autowired ObtenirQuantitatServidaAcumulada obtenirQuantitatServidaAcumulada;
    @Autowired ConfiguracioEntradesEdiRepository configEntradesEdi;

    public String executar(EmpresaAds empresaAds, KeyArticleClient clauArticleClient, KeyAlbara clauAlbara,
                           AcumulatAviExp acumulatAviExp, DataSolicitadaComandaEdi dataSolicitadaComandaEdi,
                           String tipusMissatgeEdi) {
        // Obtenció de l'articleClient
        var articleClient = obtenirArticleClientAds.query(clauArticleClient).orElseThrow();

        // Obtenció del pes de l'articleClient a través del provider de pesos
        var artint = clauArticleClient.artint();
        var pesPesa = providerPes.provide(artint).pes(artint);
        System.out.println("Pes de l'articleClient " + articleClient + ": " + pesPesa);

        // Obtenció de la comanda EDI (si no ve a null i existeix)
        Optional<ComandaEdi> comandaEdi = Objects.nonNull(dataSolicitadaComandaEdi)
                ? obtenirComandaEdi.executar(dataSolicitadaComandaEdi.keyComandaEdi())
                : Optional.empty();
        comandaEdi.ifPresent(System.out::println);

        // Obtenció de l'acumulat
        var quantitatServidaAcumulada = obtenirQuantitatServidaAcumulada.executar(clauArticleClient, clauAlbara);

        var sb = new StringBuilder();
        // LA
        sb.append(new GenerarLA(acumulatAviExp.totalPeces(), quantitatServidaAcumulada, articleClient.referencia(),
                articleClient.denominacio(),
                empresaAds.gateComp(), comandaEdi.map(ComandaEdi::la))
                .generar());
        // LB
        sb.append(new GenerarLR(pesPesa, comandaEdi.flatMap(ComandaEdi::lh), comandaEdi.flatMap(ComandaEdi::lg), comandaEdi.flatMap(ComandaEdi::ls))
                .generar());
        // LZ
        sb.append(new GenerarLZ(comandaEdi.flatMap(ComandaEdi::li)).generar());

        // Obtenció dels embalatges d'expedició associats a l'articleClient
        var embalatgesExpedicio = obtenirEmbalatgesExpedicio.executar(clauArticleClient);

        //Obtencio de la configuracio edi per saber si el client es dues dates o no.
        boolean isDuesDates = configEntradesEdi.obtenirConfiguracioEdi(clauArticleClient.clicod(), tipusMissatgeEdi).considerarDuesDates();

        // Obtenció del DA associat
        Optional<DA> optDa = comandaEdi.flatMap(comanda -> comanda.da(dataSolicitadaComandaEdi.dataSolicitada(), isDuesDates));

        // És diferent quan es tracta d'un palet homogeni o d'un palet mixte (aquest últim no porta BA's sino que
        // només porta els ET's que componen aquest palet mixte)
        if (acumulatAviExp.isPaletHomogeni()) {
            // Generació del bloc de línia per palet homogeni
            sb.append(generarPaletHomogeni(embalatgesExpedicio, acumulatAviExp, pesPesa, optDa));
        } else {
            // Generació del bloc de línia per palet mixte
            sb.append(generarPaletMixte(embalatgesExpedicio, acumulatAviExp, pesPesa, optDa));
        }

        return sb.toString();
    }

    private String generarPaletMixte(Optional<EmbalatgeExpedicio> optEmbalatge, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa, Optional<DA> optDA) {
        var sb = new StringBuilder();
        // Palet mixte => Només té el BA1 i seguidament els ET que composen aquest palet mixte
        // No té BA2 ja que no té etiqueta de transport associada a l'articleClient, sinó que té diverses etiquetes de transport associades a les diferents línies de l'albarà que componen aquest palet mixte
        var embalatge = optEmbalatge
                .flatMap(EmbalatgeExpedicio::nivell1Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell1NoRetornable))
                .orElse(ElementEmbalatgeExpedicio.caixaDefecte());
        sb.append(GenerarBAFactory.crear(embalatge, acumulatAviExp, pesPesa).generar());
        sb.append(GenerarETFactory.crear(embalatge, acumulatAviExp, optDA).generar());
        return  sb.toString();
    }

    private String generarPaletHomogeni(Optional<EmbalatgeExpedicio> optEmbalatge, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa, Optional<DA> optDA) {
        var sb = new StringBuilder();

        // BA5 (només en cas que estigui definit)
        var optEmbalatgeBa5 = optEmbalatge.flatMap(EmbalatgeExpedicio::nivell5Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell5NoRetornable));
        optEmbalatgeBa5.ifPresent(embalatge -> sb.append(GenerarBAFactory.crear(embalatge, acumulatAviExp, pesPesa).generar()));
        optEmbalatgeBa5.ifPresent(embalatge -> sb.append(GenerarETFactory.crear(embalatge, acumulatAviExp, optDA).generar()));

        // BA4 (només en cas que estigui definit)
        var optEmbalatgeBa4 = optEmbalatge.flatMap(EmbalatgeExpedicio::nivell4Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell4NoRetornable));
        optEmbalatgeBa4.ifPresent(embalatge -> sb.append(GenerarBAFactory.crear(embalatge, acumulatAviExp, pesPesa).generar()));
        optEmbalatgeBa4.ifPresent(embalatge -> sb.append(GenerarETFactory.crear(embalatge, acumulatAviExp, optDA).generar()));

        // BA3 (només en cas que estigui definit)
        var optEmbalatgeBa3 = optEmbalatge.flatMap(EmbalatgeExpedicio::nivell3Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell3NoRetornable));
        optEmbalatgeBa3.ifPresent(embalatge -> sb.append(GenerarBAFactory.crear(embalatge, acumulatAviExp, pesPesa).generar()));
        optEmbalatgeBa3.ifPresent(embalatge -> sb.append(GenerarETFactory.crear(embalatge, acumulatAviExp, optDA).generar()));

        // BA2 (en cas que estigui definit i sino s'agafa el per defecte)
        var embalatgeBa2 = optEmbalatge.flatMap(EmbalatgeExpedicio::nivell2Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell2NoRetornable))
                .orElse(ElementEmbalatgeExpedicio.paletDefecte());
        sb.append(GenerarBAFactory.crear(embalatgeBa2, acumulatAviExp, pesPesa).generar());
        sb.append(GenerarETFactory.crear(embalatgeBa2, acumulatAviExp, optDA).generar());

        // BA1 (en cas que estigui definit i sino s'agafa el per defecte)
        var embalatgeBa1 = optEmbalatge.flatMap(EmbalatgeExpedicio::nivell1Retornable)
                .or(() -> optEmbalatge.flatMap(EmbalatgeExpedicio::nivell1NoRetornable))
                .orElse(ElementEmbalatgeExpedicio.caixaDefecte());
        sb.append(GenerarBAFactory.crear(embalatgeBa1, acumulatAviExp, pesPesa).generar());
        sb.append(GenerarETFactory.crear(embalatgeBa1, acumulatAviExp, optDA).generar());

        return sb.toString();
    }

}
