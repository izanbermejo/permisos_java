package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.PropostaModificada;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbaraImpl;
import ames.comercial.albarans.internal.services.ComprovarMagatzemSII;
import ames.comercial.albarans.internal.services.CreadorAlbaransTraspas;
import ames.comercial.albarans.internal.services.CrearCapsaleraAlbaraTraspasPlataforma;
import ames.comercial.albarans.internal.services.PrepararPropostaAlbaraTraspas;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CrearAlbaraTraspasPlataforma {

    @Autowired PrepararPropostaAlbaraTraspas prepararPropostaAlbaraTraspas;
    @Autowired CreadorAlbaransTraspas creadorAlbaransTraspas;
    @Autowired CrearCapsaleraAlbaraTraspasPlataforma crearCapsaleraAlbara;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;

    @Transactional
    public ResultatCreacioAlbaransResponse executar(CrearAlbaraTraspasPlataformaRequest request) {
        // Quantitats a servir per article-client
        var quantitats = request.pecesServir().stream()
                .collect(Collectors.toMap(PesaServirRequestPlataforma::articleClient, PesaServirRequestPlataforma::quantitat, Long::sum));

        // Informació de comanda de la primera línia a servir de cada article-client (si el frontend l'ha enviat)
        Map<KeyArticleClient, InformacioComanda> infoComandes = request.pecesServir().stream()
                .filter(p -> p.infoComanda().isPresent())
                .collect(Collectors.toMap(PesaServirRequestPlataforma::articleClient, p -> p.infoComanda().get(), (a, b) -> a));

        // Es prepara la proposta a partir de l'estat actual (variant plataforma)
        var preparacio = prepararPropostaAlbaraTraspas.preparar(
                request.magatzem(), request.magatzemDesti(), request.dataAlbara(), quantitats, true,
                request.aprofitarAlbaransOberts(), request.albaransNoAprofitar(), request.agruparPerClient());

        // Detecció de canvis: si la signatura de l'estat actual difereix de la que es va acceptar, s'avorta
        if (!preparacio.signatura().equals(request.signatura())) {
            throw new PropostaModificada();
        }

        var magatzemDesti = preparacio.magatzemDesti();
        var adresa = magatzemDesti.adresa();
        var informacioEnviament = magatzemDesti.informacioEnviament().orElseThrow();
        var info = preparacio.info();

        // Capçalera: en un traspàs a plataforma l'empresa receptora és la mateixa d'origen
        CreadorAlbaransTraspas.CapsaleraFactory capsaleraFactory = (empresaOrigen, empresaDesti) ->
                crearCapsaleraAlbara.executar(Empresa.getByClau(empresaOrigen), adresa, informacioEnviament,
                        request.dataAlbara(), request.magatzem(), request.magatzemDesti(), request.tancarAlbaransNous());

        // El pendent de consumir només existeix a les plataformes marcades per al SII: és el que després
        // descompten els consums i els retorns per deixar-ne apunt a albarans.sortides_plataforma
        boolean isSii = comprovarMagatzemSII.executar(request.magatzemDesti());

        // Línia de traspàs a plataforma: preu de l'article, sense preu fixat, tota la quantitat pendent de
        // consumir (si la plataforma és del SII) i res pendent de facturar (a plataforma l'empresa
        // receptora és la mateixa d'origen, o sigui que l'albarà mai no creua empreses i no es factura)
        CreadorAlbaransTraspas.LiniaFactory liniaFactory = (albara, id, articleClient, quantitat) -> {
            var infoArticle = info.get(articleClient);
            var linia = LiniaAlbaraImpl.builder()
                    .id(id)
                    .articleClient(infoArticle.articleClient())
                    .informacioPesa(infoArticle.toInformacioPesa())
                    .quantitat(quantitat)
                    .preu(infoArticle.preu())
                    .isPreuFixat(false)
                    .quantitatPendentConsumir(isSii ? quantitat : 0)
                    .quantitatPendentFacturar(0);
            // Info de comanda de la primera línia a servir de l'article-client (només a les línies noves)
            Optional.ofNullable(infoComandes.get(articleClient)).ifPresent(linia::infoComanda);
            return linia.build();
        };

        var resultat = creadorAlbaransTraspas.crear(preparacio.proposta(), capsaleraFactory, liniaFactory);
        return ResultatCreacioAlbaransResponse.de(resultat.creats(), resultat.aprofitats());
    }

    @JsonDeserialize(builder = CrearAlbaraTraspasPlataformaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearAlbaraTraspasPlataformaRequest {
        LocalDate dataAlbara();
        String magatzem();
        String magatzemDesti();
        List<PesaServirRequestPlataforma> pecesServir();
        /** Signatura de la proposta acceptada (retornada per {@code POST /albara/plataforma/calcular}) */
        String signatura();
        /** Si s'han d'aprofitar els albarans de traspàs oberts (per defecte sí) */
        @Value.Default default boolean aprofitarAlbaransOberts() { return true; }
        /** Albarans oberts que l'usuari ha descartat aprofitar */
        List<KeyAlbara> albaransNoAprofitar();
        /** Si es vol crear un albarà independent per cada client (per defecte no) */
        @Value.Default default boolean agruparPerClient() { return false; }
        /** Si els albarans que es crein nous han de quedar tancats (per defecte sí) */
        @Value.Default default boolean tancarAlbaransNous() { return true; }
    }

    @JsonDeserialize(builder = PesaServirRequestPlataformaImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PesaServirRequestPlataforma {
        KeyArticleClient articleClient();
        long quantitat();
        /** Info de comanda de la primera línia a servir del grup; s'assigna a la línia nova de l'albarà */
        Optional<InformacioComanda> infoComanda();
    }

}
