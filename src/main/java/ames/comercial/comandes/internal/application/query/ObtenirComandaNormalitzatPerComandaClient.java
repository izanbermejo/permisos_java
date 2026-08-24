package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.internal.application.query.ObtenirComandaNormalitzat.ObtenirComandaNormalitzatResponse;
import ames.comercial.comandes.internal.domain.comanda.TipusComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.shared.Empresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirComandaNormalitzatPerComandaClient {

    @Autowired ComandaRepository comandaRepo;
    @Autowired ObtenirComandaNormalitzat obtenirComandaNormalitzat;

    public Optional<ObtenirComandaNormalitzatResponse> executar (String client, String comandaClient, Empresa empresa) {
        var optComanda = comandaRepo.findByComandaClient(client, comandaClient, empresa);
        // En cas que existeixi només cal buscar la comanda si es tracta d'una comanda de normalitzats
        return optComanda
                .filter(c -> c.tipus().equals(TipusComanda.NORMALITZAT))
                .map(comanda -> obtenirComandaNormalitzat.executar(comanda.codi()));
    }

}
