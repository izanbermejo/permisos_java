package ames.comercial.comandes.internal.service;

import ames.comercial.comandes.internal.domain.comanda.Servible;
import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CalcularServible implements ICalcularServible {

    @Override
    public Servible calcula(List<LiniaComanda> linies) {
        // Stream de les línies pendents
        Supplier<Stream<LiniaComanda>> liniesPendents = () -> linies.stream()
                .filter(Predicate.not(LiniaComanda::servida));
        var numLiniesPendents = liniesPendents.get().count();

        // Agrupació de les línes segons estat de la reserva
        var mapCount = liniesPendents.get()
                .map(LiniaComanda::reserva)
                .flatMap(Optional::stream)
                .filter(r -> !Reservable.NO_APLICA.equals(r.estat()))
                .collect(Collectors.groupingBy(InformacioReserva::estat, Collectors.counting()));

        // En cas que el map d'agrupacions estigui buit vol dir que només havien línies de no
        // aplica i llavors es retorna no aplica com a resposta
        if (mapCount.entrySet().isEmpty())
            return Servible.NO_APLICA;

        // Totes reservables
        if (mapCount.getOrDefault(Reservable.TOT, 0L) == numLiniesPendents) return Servible.TOT;
        else if (mapCount.getOrDefault(Reservable.RES, 0L) == numLiniesPendents) return Servible.RES;
        else if (mapCount.getOrDefault(Reservable.NO, 0L) == numLiniesPendents) return Servible.NO;
        else return Servible.PARCIAL;
    }

}
