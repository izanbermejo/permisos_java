package ames.comercial.calculadorareserves.service;

import ames.comercial.calculadorareserves.request.CalculadoraReservesReq;
import ames.comercial.calculadorareserves.request.CalculadoraReservesReq.LiniaCalculReservesReq;
import ames.comercial.calculadorareserves.response.CalculadoraReservesResp;
import ames.comercial.calculadorareserves.response.CalculadoraReservesResp.LiniaCalculReservesResp;
import ames.comercial.calculadorareserves.response.CalculadoraReservesRespImpl;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CalculadoraReserves {

    long stock;
    List<LiniaCalculReservesReq> liniesPendents;
    Optional<LiniaCalculReservesReq> liniaPreferent;

    public static LiniaCalculReservesReq buildRequest(LiniaComanda l) {
        return LiniaCalculReservesReq.of(l.id(), l.quantitatPendent(), l.dataCreacio());
    }

    public CalculadoraReserves(CalculadoraReservesReq req) {
        this.stock = req.stock();
        this.liniesPendents = req.linies();
        this.liniaPreferent = req.liniaPreferent();
    }

    public CalculadoraReservesResp executar() {
        // Resultat del càlcul
        var resultat = new ArrayList<LiniaCalculReservesResp>();

        // Creació de la llista de línies que es calcularan
        List<LiniaCalculReservesReq> liniesCalcular = new ArrayList<>();
        // En cas que s'hagi passat una línia como a preferent s'afegeix la primera
        liniaPreferent.ifPresent(liniesCalcular::add);

        // S'afegeix per ordre les alres línies que no son la preferent
        liniesPendents.stream()
                .filter(l -> liniaPreferent.isEmpty() || !liniaPreferent.get().equals(l))
                .sorted(Comparator.comparing(LiniaCalculReservesReq::dataSolicitudReserva)   // Per data en la que es va sol·licitar la reserva
                        .thenComparingLong(l -> l.clauLinia().comanda()) // En cas de mateixa data (la comanda creada abans)
                        .thenComparingLong(l -> l.clauLinia().numero())) // En cas de mateixa data i mateixa comanda (la línia creada abans)
                .forEach(liniesCalcular::add);
        for (var l : liniesCalcular) {
            // Es reserven el màxim de peces possibles
            var stockReservat = Math.min(l.quantitat(), stock);
            resultat.add(LiniaCalculReservesResp.of(l.clauLinia(), stockReservat));
            stock -= stockReservat;
        }

        return CalculadoraReservesRespImpl.builder()
                .linies(resultat)
                .build();
    }

}
