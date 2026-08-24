package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.services.CrearCapsaleraAlbaraConsum;
import ames.comercial.shared.Empresa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Crea la capçalera d'un albarà de consum buida (oberta, sense línies). El client i l'empresa els
 * tria l'usuari en començar el consum, entre els que tenen stock a la plataforma
 * ({@link ames.comercial.albarans.internal.application.query.ObtenirClientsPlataforma}), i ja no
 * canvien: {@link AfegirLiniaConsum} valida que cada peça afegida sigui d'aquest client.
 * <p>
 * Es crea abans d'afegir cap línia perquè el número d'albarà especial i la data quedin informats
 * (i comprovats) des del principi. Un consum que es quedi sense línies s'elimina des del detall.
 */
@Service
public class CrearCapsaleraConsum {

    @Autowired CrearCapsaleraAlbaraConsum crearCapsaleraAlbaraConsum;
    @Autowired AlbaraRepository albaraRepository;
    @Autowired IObtenirClientAds obtenirClientAds;

    @Transactional
    public KeyAlbara executar(CrearCapsaleraConsumRequest request) {
        // L'albarà de consum és autofacturable si el client ho és a Advantage (cli6.autofac = 'S').
        // Les línies de consum mai tenen preu fixat ni comanda blanca, per tant només depèn del client.
        boolean facturacioAutomatica = obtenirClientAds.get(request.client())
                .map(ClientAds::isFacturacioAutomatica)
                .orElse(false);

        var albaraEspecial = request.albaraEspecial()
                .map(String::trim)
                .filter(v -> !v.isEmpty());

        var capsalera = crearCapsaleraAlbaraConsum.executar(
                Empresa.getByClau(request.empresa()), request.client(), request.dataConsum(),
                request.magatzemPlataforma(), albaraEspecial, facturacioAutomatica);
        albaraRepository.save(capsalera);
        ReplicaAdvantage.instance().addAlbaraInsert(capsalera);

        return capsalera.id();
    }

    @JsonDeserialize(builder = CrearCapsaleraConsumRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearCapsaleraConsumRequest {
        /** Empresa propietària de l'stock a la plataforma; és la del numerador d'albarans */
        String empresa();
        String client();
        LocalDate dataConsum();
        String magatzemPlataforma();
        /** Número d'albarà especial opcional, informat per l'usuari */
        Optional<String> albaraEspecial();
    }

}
