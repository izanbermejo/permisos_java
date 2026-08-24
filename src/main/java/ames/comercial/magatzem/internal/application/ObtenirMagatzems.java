package ames.comercial.magatzem.internal.application;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.magatzem.internal.domain.Magatzem;
import ames.comercial.magatzem.internal.infraestructure.MagatzemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObtenirMagatzems implements IObtenirMagatzems {

    @Autowired MagatzemRepository magatzemRepository;

    @Override
    public Optional<Magatzem> get(String codi) {
        return magatzemRepository.find(codi);
    }

    @Override
    public List<Magatzem> all() {
        return magatzemRepository.findAll();
    }

    @Override
    public Optional<Magatzem> obtenirMagatzemIntermig(String magatzemInicial, String magatzemFinal) {
        return magatzemRepository.findCodiIntermig(magatzemInicial, magatzemFinal)
                .flatMap(magatzemRepository::find);
    }

}
