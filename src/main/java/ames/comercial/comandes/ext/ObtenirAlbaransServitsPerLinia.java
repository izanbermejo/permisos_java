package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.query.QueryRepository;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ObtenirAlbaransServitsPerLinia implements IObtenirAlbaransServitsPerLinia {

    private final QueryRepository queryRepo;

    public ObtenirAlbaransServitsPerLinia(QueryRepository queryRepo) {
        this.queryRepo = queryRepo;
    }

    @Override
    public Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> executar(KeyArticleClient articleClient) {
        return queryRepo.searchAlbaransServitsPerLinia(articleClient);
    }

}
