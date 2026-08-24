package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.PropostaModificada;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbaraImpl;
import ames.comercial.albarans.internal.services.CreadorAlbaransTraspas;
import ames.comercial.albarans.internal.services.CrearCapsaleraAlbaraTraspas;
import ames.comercial.albarans.internal.services.PrepararPropostaAlbaraTraspas;
import ames.comercial.albarans.internal.services.ProviderTarifesTraspas;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrearAlbaraTraspas {

    @Autowired PrepararPropostaAlbaraTraspas prepararPropostaAlbaraTraspas;
    @Autowired CreadorAlbaransTraspas creadorAlbaransTraspas;
    @Autowired CrearCapsaleraAlbaraTraspas crearCapsaleraAlbara;
    @Autowired ProviderTarifesTraspas providerTarifesTraspas;

    @Transactional
    public ResultatCreacioAlbaransResponse executar(CrearAlbaraTraspasRequest request) {
        // Quantitats a servir per article-client
        var quantitats = request.pecesServir().stream()
                .collect(Collectors.toMap(PesaServirRequest::articleClient, PesaServirRequest::quantitat, Long::sum));

        // Es prepara la proposta a partir de l'estat actual (variant traspàs normal)
        var preparacio = prepararPropostaAlbaraTraspas.preparar(
                request.magatzem(), request.magatzemDesti(), request.dataAlbara(), quantitats, false,
                request.aprofitarAlbaransOberts(), request.albaransNoAprofitar(), request.agruparPerClient());

        // Detecció de canvis: si la signatura de l'estat actual difereix de la que es va acceptar, s'avorta
        if (!preparacio.signatura().equals(request.signatura())) {
            throw new PropostaModificada();
        }

        var magatzemDesti = preparacio.magatzemDesti();
        var adresa = magatzemDesti.adresa();
        var informacioEnviament = magatzemDesti.informacioEnviament().orElse(InformacioEnviament.desconegut());
        var info = preparacio.info();

        // Tarifes segons si el traspàs es factura. Són les mateixes que ha mostrat la previsualització de
        // la proposta, que carrega aquest mateix proveïdor.
        var tarifes = providerTarifesTraspas.provide(quantitats.keySet());

        // Capçalera: el tipus de traspàs es dedueix de l'empresa/magatzem d'origen i destí. El magatzem
        // receptor és el destí físic (magatzemDesti), que pot ser un magatzem intermig si el destí final en té.
        CreadorAlbaransTraspas.CapsaleraFactory capsaleraFactory = (empresaOrigen, empresaDesti) ->
                crearCapsaleraAlbara.executar(Empresa.getByClau(empresaOrigen), adresa, informacioEnviament,
                        request.dataAlbara(), request.magatzem(), magatzemDesti.codi(), Empresa.getByClau(empresaDesti),
                        request.tancarAlbaransNous());

        // Línia de traspàs normal: es factura només si l'albarà on va creua empreses i no és abonable, i
        // aleshores neix amb tota la quantitat pendent de facturar i valorada amb la tarifa AMES
        CreadorAlbaransTraspas.LiniaFactory liniaFactory = (albara, id, articleClient, quantitat) -> {
            var infoArticle = info.get(articleClient);
            boolean isFacturable = albara.isTraspasFacturable();
            return LiniaAlbaraImpl.builder()
                    .id(id)
                    .articleClient(infoArticle.articleClient())
                    .informacioPesa(infoArticle.toInformacioPesa())
                    .quantitat(quantitat)
                    .preu(tarifes.preu(isFacturable, articleClient))
                    .isPreuFixat(false)
                    .quantitatPendentConsumir(0)
                    .quantitatPendentFacturar(isFacturable ? quantitat : 0)
                    .build();
        };

        var resultat = creadorAlbaransTraspas.crear(preparacio.proposta(), capsaleraFactory, liniaFactory);
        return ResultatCreacioAlbaransResponse.de(resultat.creats(), resultat.aprofitats());
    }

    @JsonDeserialize(builder = CrearAlbaraTraspasRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearAlbaraTraspasRequest {
        LocalDate dataAlbara();
        String magatzem();
        String magatzemDesti();
        List<PesaServirRequest> pecesServir();
        /** Signatura de la proposta acceptada (retornada per {@code POST /albara/traspas/calcular}) */
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

    @JsonDeserialize(builder = PesaServirRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PesaServirRequest {
        KeyArticleClient articleClient();
        long quantitat();
    }

}
