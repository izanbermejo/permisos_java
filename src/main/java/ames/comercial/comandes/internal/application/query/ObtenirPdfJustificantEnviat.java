package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.JustificantEncaraNoEnviat;
import ames.comercial.comandes.ComandesException.JustificantEnviatNoTrobat;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepository;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ObtenirPdfJustificantEnviat {

    @Autowired ComandaRepository comandaRepository;
    final AdjuntComandaRepository adjuntComandaRepository;

    public ObtenirPdfJustificantEnviat (ComandaRepository comandaRepository, AdjuntComandaRepository adjuntComandaRepository) {
        this.comandaRepository = comandaRepository;
        this.adjuntComandaRepository = adjuntComandaRepository;
    }

    public File run(long codiComanda) {
        // Obtenció de la comanda
        var comanda = comandaRepository.find(codiComanda).orElseThrow(ComandaNoExisteix::new);

        // Comprovació que no s'hagi enviat el justificant prèviament
        if (comanda.dadesEnviamentJustificant().isEmpty()) {
            throw new JustificantEncaraNoEnviat();
        }

        return adjuntComandaRepository.find(codiComanda, "_JustificantIntern.pdf").orElseThrow(JustificantEnviatNoTrobat::new);

    }

}
