package ames.comercial.entrades.internal.application;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.response.QueryArticleClientResponse;
import ames.comercial.entrades.internal.application.service.*;
import ames.comercial.entrades.internal.domain.ConfigFabricaEntrades;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.entrades.internal.infraestructure.ConfigArticleClientEntradesRepository;
import ames.comercial.entrades.internal.infraestructure.ConfigFabricaEntradesRepository;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.entrades.internal.infraestructure.magatzem.EntradaMagatzemRepository;
import ames.comercial.entrades.internal.infraestructure.magatzem.ErrorEntradaMagatzemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessaEntradaMagatzem {

    static final String ETIQUETA_JA_EXISTEIX = "ETIQUETA_JA_EXISTEIX";

    @Autowired EntradaMagatzemRepository entradaMagatzemRepo;
    @Autowired ErrorEntradaMagatzemRepository errorEntradaMagatzemRepo;
    @Autowired ConfigFabricaEntradesRepository configFabricaRepo;
    @Autowired ConfigArticleClientEntradesRepository configArticleClientEntradesRepo;

    @Transactional
    public void processar(EntradaMagatzem entradaMagatzem) {
        // Buscar l'articleclient (per obtenir l'artint)
        var optArticleClient = new ObtenirArticleClientAds().query(entradaMagatzem.articleFabrica(), entradaMagatzem.client());
        var artint = optArticleClient.map(QueryArticleClientResponse::artInt).orElse("");
        // Obtenció de l'empresa (en el parseig ja s'ha comprovat que exisiteix la configuració de la fàbrica, per tant
        // això no hauria de fallar mai i no es veurien bloquejades les entrades a magatzem)
        var empresaBase = configFabricaRepo.get(entradaMagatzem.fabrica())
                .map(ConfigFabricaEntrades::empresa)
                .orElseThrow();
        // Resolució de l'empresa: override per articleclient+fàbrica si existeix, sinó la de config_fabrica
        // Això es deixa preparat per casos on l'empresa d'entrada sigui diferent per articleclient+fàbrica, com
        // quan existia AMES PORE o similar, que una peça es feia en una fàbrica i entrava per una empresa diferent
        // a la que hi ha ara al config_fabrica, però això no permetia que un articleclient fabricat a dos fàbriques difernents
        // es pogués entrar per dos empreses diferents, ja que l'articleclient només pot tenir una empresa.
        // Per això es deixa la possibilitat de fer override per articleclient+fàbrica.
        var empresa = configArticleClientEntradesRepo
                .getEmpresa(KeyArticleClient.of(artint, entradaMagatzem.client()), entradaMagatzem.fabrica())
                .orElse(empresaBase);
        // Comprovació si l'entrada a processar és un palet (mateixa etiqueta de palet i caixa) o una caixa
        var isPaletHomogeni = entradaMagatzem.isPaletHomogeni();
        // El tipus de procés és diferent quan es tracta d'un palet (s'ha d'ubicar totes les caixes que hi han dins)
        // a quan és una caixa
        var entradaMagatzemProcessada = isPaletHomogeni ? processaPaletHomogeni(entradaMagatzem) : processaCaixa(entradaMagatzem);
        if (entradaMagatzemProcessada.error().isPresent()){
            errorEntradaMagatzemRepo.guardarError(entradaMagatzem.idEntradaFabrica(), entradaMagatzem.magatzem());
        }
        entradaMagatzemRepo.save(entradaMagatzemProcessada);

        // INSERTS a l'Advantage
        List<PreparedStatementProvider> statements = new ArrayList<>();
        // INSERTS al LOCALIT
        var optPrepLocalit = new GeneraInsertsLocalit(entradaMagatzemProcessada, artint).genera();
        optPrepLocalit.ifPresent(statements::add);
        // Generació dels TRAZAT
        var prepsTrazat = new GeneraInsertsTrazat(entradaMagatzemProcessada, artint, empresa).genera();
        statements.addAll(prepsTrazat);
        // Generaciós dels ELEMBA (Elements d'embalatge que no existeixin prèviament)
        var prepsEmba = new GeneraInsertsEmba(entradaMagatzemProcessada).genera();
        statements.add(prepsEmba);
        // Generació dels ETIEMBA (Etiquetes d'embalatge que venen a les entrades)
        var prepsEtiemba = new GeneraInsertsEtiemba(entradaMagatzemProcessada).genera();
        statements.add(prepsEtiemba);
        // Generació dels HISEMBA (Moviments a l'històric per tipus de moviment de les entrades)
        var prepsHisEmba = new GeneraInsertsHisemba(entradaMagatzemProcessada).genera();
        statements.addAll(prepsHisEmba);
        new AdvantageDao().executeUpdate(AdvantageDao.connectionMag, statements);
    }

    private EntradaMagatzem processaPaletHomogeni(EntradaMagatzem ent) {
        // Cal buscar si existeix l'etiqueta de palet
        var optEstantPalet = existeixPalet(ent.magatzem(), ent.etiquetaPalet());
        if (optEstantPalet.isPresent()) {
            var estantPalet = optEstantPalet.get();
            // TODO Anotació al log
            return ent.processaError(ETIQUETA_JA_EXISTEIX);
        }
        // Cal processar cadascuna de les caixes que conté el palet
        List<EntradaMagatzemDetall> nousDetalls = new ArrayList<>();
        for (var detall : ent.detalls()) {
            var nouDetall = detall;
            // Comprovar si la caixa existeix al magatzem
            var optEstantCaixa = existeixCaixa(ent.magatzem(), detall.etiquetaCaixa());
            if (optEstantCaixa.isPresent()) {
                var estantCaixa = optEstantCaixa.get();
                nouDetall = detall.processaError(ETIQUETA_JA_EXISTEIX);
                // TODO Anotació al log
            }
            nousDetalls.add(nouDetall);
        }
        return ent.processa(nousDetalls);
    }

    private EntradaMagatzem processaCaixa(EntradaMagatzem ent) {
        // Cal buscar si existeix l'etiqueta de caixa
        var optEstantCaixa = existeixCaixa(ent.magatzem(), ent.etiquetaCaixa());
        if (optEstantCaixa.isPresent()) {
            var estantCaixa = optEstantCaixa.get();
            // TODO Anotació al log
            return ent.processaError(ETIQUETA_JA_EXISTEIX);
        }
        return ent.processa();
    }

    /**
     * Comprova si existeix una etiqueta de caixa al magatzem
     *
     * @param etiqueta Etiqueta a buscar a les localitzacions del magatzem
     *
     * @return true si existeix, false altrament
     */
    private Optional<String> existeixCaixa(String magatzem, long etiqueta) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select TOP 1 estante
						from localit
						where magcod = ? AND eticaja = ?
					""");
            statement.setString(1, magatzem);
            statement.setLong(2, etiqueta);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<String>> rsAction = rs -> rs.next() ? Optional.of(rs.getString("estante")) : Optional.empty();
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

    /**
     * Comprova si existeix una etiqueta de palet al magatzem
     *
     * @param etiqueta Etiqueta a buscar a les localitzacions del magatzem
     *
     * @return true si existeix, false altrament
     */
    private Optional<String> existeixPalet(String magatzem, long etiqueta) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select TOP 1 estante
						from localit
						where magcod = ? AND etipale = ?
					""");
            statement.setString(1, magatzem);
            statement.setLong(2, etiqueta);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<String>> rsAction = rs -> rs.next() ? Optional.of(rs.getString("estante")) : Optional.empty();
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

}
