package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.application.service.parser.capsalera.*;
import ames.comercial.edi2.internal.application.service.parser.comanda.*;
import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdiImpl;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi.Estat;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdiImpl;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.server.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ParsejarFitxersInbox {

    @Autowired ParsejarLiniesEdi parsejarLiniesEdi;

    public record ResultatParsejat(long idMissatge, List<CapsaleraEdi> capsaleres,List<ComandaEdi> comandes) {}

    /**
     * Processa els registres pendents de l'inbox EDI.
     * Per cada missatge, divideix les línies en blocs, construeix les capçaleres i les guarda.
     */
    @Transactional
    public List<ResultatParsejat> executar(long idMissatge, String Linies) {
        List<ResultatParsejat> resultats = new ArrayList<>();
        List<String> linies = Arrays.stream(Linies.split("\\R")).toList();
        List<List<String>> blocs = dividirPerBlocs(linies);

        resultats.add(llegirCapsaleres(idMissatge, blocs));

        return resultats;
    }

    /**
     * Divideix les línies del missatge en blocs separats pel registre ZZ.
     * Cada bloc correspon a una capçalera amb les seves comandes.
     */
    private List<List<String>> dividirPerBlocs(List<String> linies) {
        List<List<String>> blocs = new ArrayList<>();
        List<String> blocActual = new ArrayList<>();

        for (String linia : linies) {
            if (linia.startsWith("ZZ")) {
                //Quan entra aqui vol dir que ha detectat el final del missatge (o bloc), si no, va afegint la linia
                //al bloc actual (else)
                blocs.add(blocActual);
                //el bloc fins ara era null, pero com s'ha anat afegint les linies al bloc actual (else), passa a tenir tota
                // la info del bloc actual
                blocActual = new ArrayList<>();
                //es reseteja el blocActual.
            } else {
                blocActual.add(linia);
            }
        }

        return blocs;
    }

    /**
     * Processa cada bloc per construir les capçaleres i les seves comandes associades.
     * El idCapsalera i idComanda s'incrementen globalment per tot el missatge.
     */
    private ResultatParsejat llegirCapsaleres(long idMissatge, List<List<String>> blocs) {
        List<CapsaleraEdi> capsaleres = new ArrayList<>();
        List<ComandaEdi> totesLesComandes = new ArrayList<>();
        int idCapsalera = 0;
        int idComanda = 0;

        if (blocs.isEmpty()) throw new AppException("El missatge esta buit");

        for (List<String> bloc : blocs) {
            CapsaleraEdi capsalera = construirCapsalera(idMissatge, ++idCapsalera, bloc);
            capsaleres.add(capsalera);

            List<ComandaEdi> comandes = llegirComandesEdi(idMissatge, capsalera.idCapsalera(), idComanda, bloc);
            idComanda += comandes.size();
            //per a que no es reinici el contador de capsaleres que farem servir per assignar-li un ID agafem el size
            //de la comanda que s'esta tractant en aquell moment per inicialitzar l'id, per tant comença a contar a partir
            // d'aquell
            totesLesComandes.addAll(comandes);
            //afegim les comandes per a pasarles al record
        }

        return new ResultatParsejat(idMissatge, capsaleres, totesLesComandes);
    }

    /**
     * Construeix una CapsaleraEdi a partir de les línies del bloc.
     * Assigna cada línia al camp corresponent segons el seu prefix.
     */
    private CapsaleraEdi construirCapsalera(long idMissatge, long idCapsalera, List<String> bloc) {
        CapsaleraEdiImpl.Builder builder = CapsaleraEdiImpl.builder()
                .idMissatge(idMissatge)
                .idCapsalera(idCapsalera);
        bloc.forEach(linia -> assignarLiniaCapsalera(builder, linia));
        return builder.build();
    }

    /**
     * Construeix la llista de comandes d'un bloc.
     * Cada registre LA marca l'inici d'una nova comanda.
     * El idComanda continua el comptador global del missatge.
     */
    private List<ComandaEdi> llegirComandesEdi(long idMissatge, long idCapsalera, int idComandaOffset, List<String> bloc) {
        List<ComandaEdi> comandes = new ArrayList<>();
        ComandaEdiImpl.Builder builder = null;
        List<LiniaEdi> linies = new ArrayList<>();
        int idComanda = idComandaOffset;

        for (String linia : bloc) {
            if (linia.startsWith("LA")) {
                if (builder != null) {
                    comandes.add(builder
                            .linies(linies)
                            .estat(linies.isEmpty() ? Estat.ELIMINADA : Estat.PENDENT_LLIGAR)
                            .build());
                }
                builder = construirComanda(idMissatge, idCapsalera, ++idComanda);
                linies = new ArrayList<>();
            }
            if (linia.startsWith("DA")) {
                linies.addAll(parsejarLiniesEdi.executar(idComanda, linia));
            }
            if (builder != null) assignarLiniaComanda(builder, linia);
        }

        if (builder != null) {
            comandes.add(builder
                    .linies(linies)
                    .estat(linies.isEmpty() ? Estat.ELIMINADA : Estat.PENDENT_LLIGAR)
                    .build());
        }

        return comandes;
    }

    /**
     * Inicialitza el builder d'una ComandaEdi amb els identificadors corresponents.
     * artInt i cliCod s'assignaran en un procés posterior.
     */
    private ComandaEdiImpl.Builder construirComanda(long idMissatge, long idCapsalera, int idComanda) {
        return ComandaEdiImpl.builder()
                .idMissatge(idMissatge)
                .idCapsalera(idCapsalera)
                .idComanda(idComanda)
                .artInt(Optional.empty())
                .cliCod(Optional.empty())
                .estat(Estat.PENDENT_LLIGAR);
    }

    /**
     * Assigna la línia al camp corresponent de la capçalera segons el prefix del registre.
     */
    private void assignarLiniaCapsalera(CapsaleraEdiImpl.Builder builder, String linia) {
        if (linia.startsWith("CA"))      builder.ca(ParsejadorRegistreCA.parse(linia));
        else if (linia.startsWith("CB")) builder.cb(ParsejadorRegistreCB.parse(linia));
        else if (linia.startsWith("CC")) builder.cc(ParsejadorRegistreCC.parse(linia));
        else if (linia.startsWith("CD")) builder.cd(ParsejadorRegistreCD.parse(linia));
        else if (linia.startsWith("CI")) builder.ci(ParsejadorRegistreCI.parse(linia));
        else if (linia.startsWith("CP")) builder.cp(ParsejadorRegistreCP.parse(linia));
        else if (linia.startsWith("CQ")) builder.cq(ParsejadorRegistreCQ.parse(linia));
        else if (linia.startsWith("CF")) builder.cf(ParsejadorRegistreCF.parse(linia));
        else if (linia.startsWith("CT")) builder.addCt(ParsejadorRegistreCT.parse(linia));
    }

    /**
     * Assigna la línia al camp corresponent de la comanda segons el prefix del registre.
     */
    private void assignarLiniaComanda(ComandaEdiImpl.Builder builder, String linia) {
        if (linia.startsWith("LA"))      builder.la(ParsejadorRegistreLA.parse(linia));
        else if (linia.startsWith("LB")) builder.lb(ParsejadorRegistreLB.parse(linia));
        else if (linia.startsWith("LC")) builder.lc(ParsejadorRegistreLC.parse(linia));
        else if (linia.startsWith("LD")) builder.ld(ParsejadorRegistreLD.parse(linia));
        else if (linia.startsWith("LS")) builder.ls(ParsejadorRegistreLS.parse(linia));
        else if (linia.startsWith("LE")) builder.addLe(ParsejadorRegistreLE.parse(linia));
        else if (linia.startsWith("LT")) builder.addLt(ParsejadorRegistreLT.parse(linia));
        else if (linia.startsWith("LG")) builder.lg(ParsejadorRegistreLG.parse(linia));
        else if (linia.startsWith("LH")) builder.lh(ParsejadorRegistreLH.parse(linia));
        else if (linia.startsWith("LI")) builder.li(ParsejadorRegistreLI.parse(linia));
        else if (linia.startsWith("LL")) builder.addLl(ParsejadorRegistreLL.parse(linia));
        else if (linia.startsWith("LQ")) builder.addLq(ParsejadorRegistreLQ.parse(linia));
        else if (linia.startsWith("AA")) builder.addAa(ParsejadorRegistreAA.parse(linia));
    }
}