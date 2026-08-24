package ames.comercial.ofs.internal.application.command;

import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.OFExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnularOrdreFabricacio {

    @Autowired OrdreFabricacioRepository ofRepo;

    @Transactional
    public void executar(long numero) {
        var optOf = ofRepo.get(numero);
        var ofAnterior = optOf.orElseThrow(()-> new OFExceptions.OFNoExisteixPerNumero(numero));
        var ofAnulada = ofAnterior.anular();
        ofRepo.save(ofAnulada);
    }
}