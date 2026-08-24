package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.application.service.parser.linia.ParsejadorRegistreDA;
import ames.comercial.edi2.internal.application.service.parser.linia.ParsejadorRegistreDR;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdiImpl;
import ames.comercial.edi2.internal.infraestructure.linia.LiniaEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParsejarLiniesEdi {

    @Autowired LiniaEdiRepository liniaEdiRepository;

    public List<LiniaEdi> executar(long idComanda, String linia) {
        LiniaEdiImpl.Builder builder = LiniaEdiImpl.builder();
        assignarLiniaEdi(builder, linia);
        return List.of(builder.idComanda(idComanda).idLinia(liniaEdiRepository.nextID()).build());
    }

    private void assignarLiniaEdi(LiniaEdiImpl.Builder builder, String linia) {
        if (linia.startsWith("DA"))      builder.da(ParsejadorRegistreDA.parse(linia));
        else if (linia.startsWith("DR")) builder.dr(ParsejadorRegistreDR.parse(linia));
    }
}
