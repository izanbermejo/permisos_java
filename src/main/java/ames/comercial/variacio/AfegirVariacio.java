package ames.comercial.variacio;

import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.infraestructure.variacio.VariacioComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AfegirVariacio {

    @Autowired VariacioComandaRepository variacioRepo;

    @Transactional
    public void registraAmbAnterior(LiniaComanda liniaComanda, LocalDateTime datareg, LiniaComanda liniaComandaAnterior, LocalDateTime dataregAnterior) {
        variacioRepo.insertNegatiu(liniaComandaAnterior, dataregAnterior, datareg.toLocalDate());
        variacioRepo.insert(liniaComanda, datareg, datareg.toLocalDate());
    }

    @Transactional
    public void registraSenseAnterior(LiniaComanda liniaComanda, LocalDateTime datareg) {
        variacioRepo.insert(liniaComanda, datareg, datareg.toLocalDate());
    }

}
