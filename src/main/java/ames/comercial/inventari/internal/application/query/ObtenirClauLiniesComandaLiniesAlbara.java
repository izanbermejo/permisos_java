package ames.comercial.inventari.internal.application.query;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.inventari.internal.domain.moviment.InformacioSortida;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObtenirClauLiniesComandaLiniesAlbara {

    @Autowired MovimentRepository movimentRepository;

    public List<KeyLiniaComanda> executar (KeyLiniaAlbara liniaAlbara) {
        return movimentRepository.findByLiniaAlbara(liniaAlbara).stream()
                .map(Moviment::sortida)
                .flatMap(Optional::stream)
                .map(InformacioSortida::liniaComanda)
                .toList();
    }

}
