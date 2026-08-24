package ames.comercial.edi2.internal.application.query;

import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.ext.ProcessarLiniesComandaReqImpl;
import ames.comercial.comandes.internal.application.command.ProcessarLiniesComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GuardarInformacioEDI2 {

    @Autowired ProcessarLiniesComanda processarComandaEdi;
    @Autowired ComandaEdiRepository comandaEdiRepository;

    public void executar(String comandaSistema){
        List<ComandaEdi> comandes = comandaEdiRepository.obtenirComandabyNumComanda(comandaSistema);
        for (ComandaEdi comanda : comandes ){
            var comandaLinia = comandaEdiRepository.obtenirComanda(comanda.keyComandaEdi())
                    .orElseThrow(EDIException.ComandaNoTrobada::new);
            List<ProcessarLiniesComandaReq> reqLinies = comandaLinia.linies().stream()
                    .filter(liniaEdi -> liniaEdi.da().fechaInicial().isPresent())
                    .map(linia -> ProcessarLiniesComandaReqImpl.builder()
                            .clauLinia(Optional.empty()) // o lo que toque
                            .comandaClient(comandaLinia.numeroComanda()) // ejemplo
                            .quantitat(linia.quantitatNova())
                            .tipus(TipusLiniaComanda.FERM) // el que corresponda
                            .dataSolicitada(LocalDate.now())
                            .dataPrevistaSortida(LocalDate.now()) // si aplica
                            .dataPrevistaSortidaInterna(Optional.empty())
                            .build()).collect(Collectors.toUnmodifiableList());
            processarComandaEdi.saveInformacioEdi(comandaLinia.articleClient(), comandaLinia.keyComandaEdi(), reqLinies);
        }

    }
}
