package ames.comercial.comandes.internal.domain.service;

import ames.comercial.comandes.ComandesException.NovaQuantitatInferiorServida;
import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.comandes.service.IProviderDiesReserva;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

public class ServeiEditarLiniaComandaNormalitzat {

    IProviderDiesReserva providerDiesReserva;
    LiniaComanda liniaComanda;

    public ServeiEditarLiniaComandaNormalitzat(IProviderDiesReserva providerDiesReserva, LiniaComanda liniaComanda) {
        this.providerDiesReserva = providerDiesReserva;
        this.liniaComanda = liniaComanda;
    }

    public LiniaComanda executar(ServeiEditarLiniaComandaNormalitzatRequest req) {
        // La nova quantitat no pot ser inferior a les peces ja servides
        if (req.novaQuantitat() < liniaComanda.quantitatServida())
            throw new NovaQuantitatInferiorServida(req.novaQuantitat(), liniaComanda.quantitatServida());

        // En cas que la nova data sol·licitada sigui superior a la data màxima de reserva, s'elimina la reserva
        var dataMaxReserva = providerDiesReserva.dataMaxReserva();
        if (req.dataSolicitada().isAfter(dataMaxReserva))
            return senseReserva(liniaComanda, req);

        // En cas que la nova data sol·licitada sigui igual o anterior a la data màxima de reserva, es recalcula la reserva tenint en compte la nova quantitat i la quantitat reservable disponible
        return ambActualitzacioReserva(liniaComanda, req);
    }

    private LiniaComanda senseReserva(LiniaComanda linia, ServeiEditarLiniaComandaNormalitzatRequest req) {
        // No es reserva cap quantitat
        var reserva = InformacioReservaImpl.builder()
                .estat(Reservable.NO)
                .quantitat(0)
                .build();
        return LiniaComandaImpl.builder()
                .from(linia)
                .quantitat(req.novaQuantitat())
                .reserva(reserva)
                .preu(req.nouPreu())
                .dataSolicitada(req.dataSolicitada())
                .dataPrevistaSortida(req.dataPrevistaSortida())
                .dadesCalcul(linia.dadesCalcul().orElseThrow().novaQuantitatCalcul(req.quantitatCalcul()))
                .build();
    }

    private LiniaComanda ambActualitzacioReserva(LiniaComanda linia, ServeiEditarLiniaComandaNormalitzatRequest req) {
        // Diferència de quantitat
        var difQtat = req.novaQuantitat() - linia.quantitat();
        LiniaComanda newLinia = LiniaComandaImpl.builder()
                .from(linia)
                .quantitat(req.novaQuantitat())
                .build();
        // En cas que es demanim mes peces o que es demanin les mateixes peces però encara hi hagi quantitat reservable disponible, s'intenta incrementar la reserva
        if (difQtat > 0 || (difQtat == 0 && req.quantitatReservableDisponible()>0)) {
            newLinia = newLinia.afegirReserva(Math.min(difQtat + linia.quantitatPendentReservar(), req.quantitatReservableDisponible()));
        } else {
            // En cas que es demanin menys peces es decrementa la reserva
            // entre màxim 0 i la diferència entre la nova quantitat i la quantitat ja reservada
            newLinia = newLinia.treureReserva(Math.max(0, linia.quantitatReservada()-req.novaQuantitat()));
        }
        return LiniaComandaImpl.builder()
                .from(newLinia)
                .preu(req.nouPreu())
                .dataSolicitada(req.dataSolicitada())
                .dataPrevistaSortida(req.dataPrevistaSortida())
                .dadesCalcul(linia.dadesCalcul().orElseThrow().novaQuantitatCalcul(req.quantitatCalcul()))
                .build();
    }

    @JsonDeserialize(builder = ServeiEditarLiniaComandaNormalitzatRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ServeiEditarLiniaComandaNormalitzatRequest {
        long novaQuantitat();
        long quantitatReservableDisponible();
        Preu nouPreu();
        long quantitatCalcul();
        LocalDate dataSolicitada();
        LocalDate dataPrevistaSortida();
    }

}
