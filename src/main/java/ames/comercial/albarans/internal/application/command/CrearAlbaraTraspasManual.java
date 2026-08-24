package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.TraspasNoAbonable;
import ames.comercial.albarans.AlbaransException.TraspasSenseMoviment;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.services.CrearCapsaleraAlbaraTraspas;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.SharedExceptions.MagatzemNoExisteix;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Crea la capçalera d'un albarà de traspàs <b>buit</b> (sense línies) a partir de l'empresa i el magatzem
 * d'origen i de destí que l'usuari ha escollit manualment. És el primer pas de l'alta manual d'un traspàs:
 * un cop creada la capçalera, les línies s'hi van afegint d'una en una amb {@link AfegirLiniaTraspas}.
 * <p>
 * A diferència de {@link CrearAlbaraTraspas}, que dedueix les empreses d'origen i destí de l'article-client
 * de cada peça, aquí les imposa l'usuari; per això {@link AfegirLiniaTraspas} rebutja després les peces que
 * no siguin de l'empresa d'origen de l'albarà.
 * <p>
 * L'albarà es crea obert (cal poder-hi afegir línies) i, com a la resta de traspassos, si entre l'origen i
 * el destí hi ha definit un magatzem intermig (relleu) el receptor físic de l'albarà és l'intermig.
 */
@Service
public class CrearAlbaraTraspasManual {

    @Autowired IObtenirMagatzems obtenirMagatzems;
    @Autowired CrearCapsaleraAlbaraTraspas crearCapsaleraAlbaraTraspas;
    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public CrearAlbaraTraspasManualResponse executar(CrearAlbaraTraspasManualRequest request) {
        var empresaOrigen = Empresa.getByClau(request.empresaOrigen());
        // L'empresa de destí només cal informar-la quan hi ha canvi d'empresa
        var empresaDesti = Empresa.getByClau(request.empresaDesti().orElse(request.empresaOrigen()));

        var magatzemOrigen = obtenirMagatzems.get(request.magatzem())
                .orElseThrow(() -> new MagatzemNoExisteix(request.magatzem()));
        var magatzemDesti = obtenirMagatzems.get(request.magatzemDesti())
                .orElseThrow(() -> new MagatzemNoExisteix(request.magatzemDesti()));

        // Un traspàs ha de moure alguna cosa: o canvia de magatzem o canvia d'empresa
        if (magatzemOrigen.codi().equals(magatzemDesti.codi()) && empresaOrigen.clau().equals(empresaDesti.clau())) {
            throw new TraspasSenseMoviment();
        }
        // L'abonable substitueix la facturació, així que només té sentit si el traspàs es facturaria
        boolean isTraspasAbonable = request.isTraspasAbonable();
        if (isTraspasAbonable && empresaOrigen.clau().equals(empresaDesti.clau())) {
            throw new TraspasNoAbonable();
        }

        // Si el destí té un magatzem intermig (relleu) per a l'origen donat, el traspàs físic hi va a parar;
        // l'adreça i la informació d'enviament de l'albarà són les del magatzem que rep físicament la mercaderia.
        var magatzemReceptor = obtenirMagatzems.obtenirMagatzemIntermig(magatzemOrigen.codi(), magatzemDesti.codi())
                .orElse(magatzemDesti);

        var albara = crearCapsaleraAlbaraTraspas.executar(
                empresaOrigen,
                magatzemReceptor.adresa(),
                magatzemReceptor.informacioEnviament().orElse(InformacioEnviament.desconegut()),
                request.dataAlbara(),
                magatzemOrigen.codi(),
                magatzemReceptor.codi(),
                empresaDesti,
                false,
                isTraspasAbonable);

        albaraRepository.save(albara);
        ReplicaAdvantage.instance().addAlbaraInsert(albara);

        return new CrearAlbaraTraspasManualResponse(albara.id(),
                magatzemReceptor.codi().equals(magatzemDesti.codi())
                        ? Optional.empty()
                        : Optional.of(magatzemReceptor.codi()));
    }

    @JsonDeserialize(builder = CrearAlbaraTraspasManualRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearAlbaraTraspasManualRequest {
        /** Data de l'albarà; és també la data dels moviments d'inventari de les línies que s'hi afegeixin */
        LocalDate dataAlbara();
        String empresaOrigen();
        /** Empresa de destí; buida si no hi ha canvi d'empresa (aleshores s'agafa la d'origen) */
        Optional<String> empresaDesti();
        String magatzem();
        String magatzemDesti();
        /**
         * Cert si el traspàs s'abona en comptes de facturar-se. Requereix canvi d'empresa: sense canvi
         * d'empresa el traspàs no es factura i per tant tampoc no s'abona.
         */
        @Value.Default default boolean isTraspasAbonable() { return false; }
    }

    public record CrearAlbaraTraspasManualResponse(
            KeyAlbara idAlbara,
            /** Magatzem intermig (relleu) pel qual passarà físicament el traspàs; buit si no n'hi ha */
            Optional<String> magatzemIntermig
    ) {}

}
