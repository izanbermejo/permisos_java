package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.comandes.internal.application.query.ObtenirComandaEspecial.ObtenirComandaEspecialResponse;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.shared.Empresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirComandaEspecialPerComandaClient {

    @Autowired ComandaRepository comandaRepo;
    @Autowired ObtenirComandaEspecial obtenirComandaEspecial;
    @Autowired IObtenirClientAds obtenirClientAds;

    public Optional<ObtenirComandaEspecialResponse> executar (String codiClient, String comandaClient) {
        var client = obtenirClientAds.get(codiClient).orElseThrow();
        var optComanda = comandaRepo.findByComandaClient(codiClient, comandaClient, Empresa.getByClau(client.empresa()));
        return optComanda.map(comanda -> obtenirComandaEspecial.executar(comanda.codi()));
    }

}
