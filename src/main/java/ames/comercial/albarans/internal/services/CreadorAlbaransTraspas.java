package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransTraspas.CalcularCreacioAlbaransTraspasResponse;
import ames.comercial.inventari.ext.CrearMovimentTraspasMagatzemEmpresaRequestImpl;
import ames.comercial.inventari.ext.ICrearMovimentTraspasMagatzemEmpresa;
import ames.comercial.inventari.ext.ICrearMovimentTraspasMagatzemEmpresa.CrearMovimentTraspasMagatzemEmpresaRequest;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Persisteix una proposta de creació d'albarans de traspàs ({@link CalcularCreacioAlbaransTraspas}): crea els
 * albarans nous, afegeix línies noves als albarans oberts aprofitats i incrementa les línies existents,
 * generant els moviments de traspàs corresponents. És la part comuna de la creació de traspàs, tant normal
 * com a plataforma; les diferències (capçalera i construcció de línia) es passen com a factories.
 */
@Service
public class CreadorAlbaransTraspas {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired ICrearMovimentTraspasMagatzemEmpresa crearMovimentTraspas;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;
    @Autowired RegistrarRetornPlataforma registrarRetornPlataforma;

    /** Crea la capçalera d'un albarà nou per a una empresa d'origen i de destí donades. */
    public interface CapsaleraFactory {
        Albara crear(String empresaOrigen, String empresaDesti);
    }

    /**
     * Construeix una línia d'albarà per a un article-client i quantitat, amb la clau de línia indicada.
     * Rep l'albarà on anirà la línia perquè, en un traspàs, si es factura o no ho diu la capçalera
     * ({@link Albara#isTraspasEmpresa()}): una mateixa proposta pot generar albarans amb canvi d'empresa
     * i sense, i els albarans oberts que s'aprofiten poden ser de qualsevol de les dues menes.
     */
    public interface LiniaFactory {
        LiniaAlbara crear(Albara albara, KeyLiniaAlbara id, KeyArticleClient articleClient, long quantitat);
    }

    /** Albarans afectats per la creació de traspàs: els creats de nou i els oberts aprofitats */
    public record ResultatCreacio(List<Albara> creats, List<Albara> aprofitats) {}

    public ResultatCreacio crear(CalcularCreacioAlbaransTraspasResponse proposta, CapsaleraFactory capsaleraFactory, LiniaFactory liniaFactory) {
        var creats = crearNousAlbarans(proposta, capsaleraFactory, liniaFactory);
        afegirLiniesAlbaransOberts(proposta, liniaFactory);
        incrementarLiniesExistents(proposta);
        return new ResultatCreacio(creats, albaransAprofitats(proposta));
    }

    /** Albarans oberts aprofitats: els que reben una nova línia i/o un increment d'una línia existent */
    private List<Albara> albaransAprofitats(CalcularCreacioAlbaransTraspasResponse proposta) {
        var ids = new LinkedHashSet<KeyAlbara>();
        ids.addAll(proposta.albaransAprofitables().keySet());
        proposta.liniesAprofitables().keySet().forEach(k -> ids.add(k.idAlbara()));
        return ids.stream()
                .map(id -> albaraRepository.find(id).orElseThrow())
                .toList();
    }

    private List<Albara> crearNousAlbarans(CalcularCreacioAlbaransTraspasResponse proposta, CapsaleraFactory capsaleraFactory, LiniaFactory liniaFactory) {
        var creats = new ArrayList<Albara>();
        for (var nou : proposta.creacioAlbarans()) {
            var capsalera = capsaleraFactory.crear(nou.empresaOrigen(), nou.empresaDesti());
            albaraRepository.save(capsalera);
            creats.add(capsalera);
            ReplicaAdvantage.instance().addAlbaraInsert(capsalera);

            long numLinia = 1;
            for (var linia : nou.linies()) {
                var liniaAlbara = liniaFactory.crear(capsalera, KeyLiniaAlbara.of(capsalera.id(), numLinia++), linia.articleClient(), linia.quantitatServir());
                persistirLiniaNova(capsalera, liniaAlbara);
            }
        }
        return creats;
    }

    private void afegirLiniesAlbaransOberts(CalcularCreacioAlbaransTraspasResponse proposta, LiniaFactory liniaFactory) {
        for (var entry : proposta.albaransAprofitables().entrySet()) {
            var albara = albaraRepository.find(entry.getKey()).orElseThrow();
            long numLinia = liniaAlbaraRepository.nextNumero(albara.id());
            for (var linia : entry.getValue()) {
                var liniaAlbara = liniaFactory.crear(albara, KeyLiniaAlbara.of(albara.id(), numLinia++), linia.articleClient(), linia.quantitatServir());
                persistirLiniaNova(albara, liniaAlbara);
            }
        }
    }

    private void incrementarLiniesExistents(CalcularCreacioAlbaransTraspasResponse proposta) {
        for (var entry : proposta.liniesAprofitables().entrySet()) {
            var albara = albaraRepository.find(entry.getKey().idAlbara()).orElseThrow();
            // El pendent de facturar només creix si l'albarà es factura, i el de consumir només si el
            // traspàs va a una plataforma del SII, igual que a les línies noves
            var liniaAlbara = liniaAlbaraRepository.find(entry.getKey()).orElseThrow()
                    .incrementarQuantitat(entry.getValue(), albara.isTraspasFacturable(), isTraspasPlataformaSII(albara));
            liniaAlbaraRepository.save(liniaAlbara);
            crearMovimentTraspas.executar(buildMoviment(albara, liniaAlbara, entry.getValue()));
            // Si el traspàs treu mercaderia d'una plataforma del SII, l'increment també n'és una sortida
            registrarRetornPlataforma.registrar(albara, liniaAlbara.id(), liniaAlbara.articleClient(), entry.getValue());
            // Si s'ha aprofitat un albarà abonable, el pendent d'abonar de la línia creix amb el mateix
            // increment. No es pot refer el registre de zero: es perdria el que ja s'hagi abonat.
            if (albara.isTraspasAbonable()) {
                ReplicaAdvantage.instance().addPenAboIncrement(albara, liniaAlbara, entry.getValue());
            }
        }
    }

    private void persistirLiniaNova(Albara albara, LiniaAlbara liniaAlbara) {
        liniaAlbaraRepository.save(liniaAlbara);
        crearMovimentTraspas.executar(buildMoviment(albara, liniaAlbara, liniaAlbara.quantitat()));
        ReplicaAdvantage.instance().addLiniaAlbaraInsert(liniaAlbara);
        // Si el traspàs treu mercaderia d'una plataforma del SII, la línia n'és una sortida i s'ha de
        // descomptar el pendent de consumir dels traspassos que hi havien entrat
        registrarRetornPlataforma.registrar(albara, liniaAlbara.id(), liniaAlbara.articleClient(), liniaAlbara.quantitat());
        // Els albarans que crea la proposta no són mai abonables, però sí que pot aprofitar-ne un de
        // manual que ho sigui i afegir-hi línies noves
        if (albara.isTraspasAbonable()) {
            ReplicaAdvantage.instance().addPenAboInsert(albara, liniaAlbara);
        }
    }

    /** Cert si el traspàs va a una plataforma marcada per al SII, les úniques amb pendent de consumir. */
    private boolean isTraspasPlataformaSII(Albara albara) {
        return albara.tipus() == TipusAlbara.TRASPAS_PLATAFORMA
                && comprovarMagatzemSII.executar(albara.magatzemTraspas());
    }

    private CrearMovimentTraspasMagatzemEmpresaRequest buildMoviment(Albara albara, LiniaAlbara liniaAlbara, long quantitat) {
        return CrearMovimentTraspasMagatzemEmpresaRequestImpl.builder()
                .articleClient(liniaAlbara.articleClient())
                .idLiniaAlbara(liniaAlbara.id())
                .data(albara.data())
                .quantitat(quantitat)
                .empresaOrigen(Empresa.getByClau(albara.id().empresa()))
                .magatzemOrigen(albara.magatzem())
                .empresaDesti(Empresa.getByClau(albara.empresaTraspas()))
                .magatzemDesti(albara.magatzemTraspas())
                .build();
    }

}
