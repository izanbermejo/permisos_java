package ames.comercial.inventari.internal.application.command;

import ames.comercial.entrades.internal.application.ObtenirDataMinimaMovimentEntrades;
import ames.comercial.inventari.ext.ICrearMovimentEntrada;
import ames.comercial.inventari.internal.domain.service.CrearMovimentEntradaRequestImpl;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentEntrada;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentEntrada.CrearMovimentEntradaRequest;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CrearMovimentEntrada implements ICrearMovimentEntrada {

    @Autowired ObtenirDataMinimaMovimentEntrades obtenirDataMinimaMovimentEntrades;
    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

     @Override
     public void executar(CrearMovimentEntradaRequest request) {
         // Correcció de la data d'entrada en cas que sigui anterior a la data mínima per a moviments d'entrada
         // que s'actualitza amb l'inventari
         var requestCorregit = correcioDataMinima(request);
         // Creació del moviment d'entrada (amb la data d'entrada corregida si calia)
         var moviment = new FactoryMovimentEntrada().executar(requestCorregit);
         // Es guarda el moviment a l'històric
         movimentRepository.save(moviment);
         // Actualització de la fitxa d'inventari
         actualitzarFitxa.executar(moviment);
     }

     public CrearMovimentEntradaRequest correcioDataMinima (CrearMovimentEntradaRequest request) {
         var dataEntradaMinima = obtenirDataMinimaMovimentEntrades.executar();
         var dataEntrada = request.data().isBefore(dataEntradaMinima) ? LocalDate.now() : request.data();
         return CrearMovimentEntradaRequestImpl.builder()
                 .from(request)
                 .data(dataEntrada)
                 .build();
     }

}
