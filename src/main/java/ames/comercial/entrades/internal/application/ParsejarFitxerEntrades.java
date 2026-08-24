package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.infraestructure.ConfigFabricaEntradesRepository;
import ames.comercial.entrades.internal.domain.service.GenerarLlistaEntradesComercial;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import ames.comercial.entrades.internal.infraestructure.magatzem.EntradaMagatzemRepository;
import ames.comercial.server.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ParsejarFitxerEntrades {

    @Autowired EntradaMagatzemRepository entradaMagatzemRepository;
    @Autowired EntradaComercialRepository entradaComercialRepository;
    @Autowired ConfigFabricaEntradesRepository configFabricaRepo;
    @Autowired ObjectMapper jsonMapper;
    @Autowired InboxEntrades inboxEntrades;
    Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Transactional
    public void executar (String idEntrada, String contingut) {
        // Parseig del fitxer a entrades de magatzem
        var listEntradesMagatzem = new EntradaMagatzemParser(configFabricaRepo, json).parse(idEntrada, contingut);
        // Les entrades de Reese no es donen d'alta al magatzem
        if (!isEntradesReese(listEntradesMagatzem))
            entradaMagatzemRepository.save(listEntradesMagatzem);
        // Creació de les entrades de comercial
        var listEntradesComercial = new GenerarLlistaEntradesComercial(listEntradesMagatzem).generar();
        entradaComercialRepository.save(listEntradesComercial);
        // Marcar l'entrada com a parsejada a l'inbox
        inboxEntrades.marcaProcessat(idEntrada);
    }

    private boolean isEntradesReese(List<EntradaMagatzem> listEntrades) {
        return !listEntrades.isEmpty() && "07".equals(listEntrades.get(0).fabrica());
    }

}
