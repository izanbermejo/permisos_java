package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.ext.ICrearMovimentTraspasMagatzemEmpresa;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasEmpresa;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasEmpresa.CrearMovimentTraspasEmpresaRequest;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasEmpresaRequestImpl;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasMagatzem;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasMagatzem.CrearMovimentTraspasMagatzemRequest;
import ames.comercial.inventari.internal.domain.service.CrearMovimentTraspasMagatzemRequestImpl;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrearMovimentTraspasMagatzemEmpresa implements ICrearMovimentTraspasMagatzemEmpresa {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    // Un moviment de traspàs genera dos moviments: una sortida i una entrada
    // I en el cas que sigui un traspàs que afecti a empresa i magatzem generarà quatre moviments
    @Override
    public void executar(CrearMovimentTraspasMagatzemEmpresaRequest request) {
        // Si les empreses no són les mateixes, implica un traspàs d'empresa
        var isTraspasEmpresa = !request.empresaOrigen().equals(request.empresaDesti());
        // Si els magatzems no són els mateixos, implica un traspàs de magatzem
        var isTraspasMagatzem = !request.magatzemOrigen().equals(request.magatzemDesti());

        // Si hi ha traspàs de magatzem, generar moviment del magatzem origen (negatiu) i moviment del magatzem destí (positiu)
        if (isTraspasMagatzem) {
            // Moviment del magatzem origen (negatiu) - Creació del request i del moviment a través del servei de creació
            var reqMovimentOrigen = buildRequestMagatzem(request, true, isTraspasEmpresa);
            var movimentOrigen = new CrearMovimentTraspasMagatzem().executar(reqMovimentOrigen);
            movimentRepository.save(movimentOrigen);
            actualitzarFitxa.executar(movimentOrigen);
            // Moviment del magatzem destí (positiu) - Creació del request i del moviment a través del servei de creació
            var reqMovimentDesti = buildRequestMagatzem(request, false, isTraspasEmpresa);
            var movimentDesti = new CrearMovimentTraspasMagatzem().executar(reqMovimentDesti);
            movimentRepository.save(movimentDesti);
            actualitzarFitxa.executar(movimentDesti);
        }

        // Si hi ha traspàs d'empresa, generar moviment de sortida a l'empresa origen (negatiu) i moviment d'entrada a l'empresa destí (positiu)
        if (isTraspasEmpresa) {
            // Moviment de l'empresa origen (negatiu) - Creació del request i del moviment a través del servei de creació
            var reqMovimentOrigen = buildRequestEmpresa(request, true, isTraspasMagatzem);
            var movimentOrigen = new CrearMovimentTraspasEmpresa().executar(reqMovimentOrigen);
            movimentRepository.save(movimentOrigen);
            actualitzarFitxa.executar(movimentOrigen);
            // Moviment de l'empresa destí (positiu) - Creació del request i del moviment a través del servei de creació
            var reqMovimentDesti = buildRequestEmpresa(request, false, isTraspasMagatzem);
            var movimentDesti = new CrearMovimentTraspasEmpresa().executar(reqMovimentDesti);
            movimentRepository.save(movimentDesti);
            actualitzarFitxa.executar(movimentDesti);
        }
    }

    private CrearMovimentTraspasMagatzemRequest buildRequestMagatzem(CrearMovimentTraspasMagatzemEmpresaRequest request, boolean isOrigen,
                                                                     boolean isTraspasEmpresa) {
        return CrearMovimentTraspasMagatzemRequestImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresaOrigen().clau()) // En el cas de traspàs de magatzem, l'empresa associada al moviment serà la de l'origen
                .magatzem(isOrigen ? request.magatzemOrigen() : request.magatzemDesti())
                .liniaAlbara(request.idLiniaAlbara())
                .data(request.data())
                .quantitat(isOrigen ? -request.quantitat() : request.quantitat())
                .magatzemReceptor(isOrigen ? request.magatzemDesti() : request.magatzemOrigen())
                .isVaImplicarTraspasEmpresa(isTraspasEmpresa)
                .build();
    }

    private CrearMovimentTraspasEmpresaRequest buildRequestEmpresa(CrearMovimentTraspasMagatzemEmpresaRequest request, boolean isOrigen,
                                                                   boolean isTraspasMagatzem) {
        return CrearMovimentTraspasEmpresaRequestImpl.builder()
                .articleClient(request.articleClient())
                .empresa(isOrigen ? request.empresaOrigen().clau() : request.empresaDesti().clau())
                .magatzem(request.magatzemDesti()) // En el cas de traspàs d'empresa, el magatzem associat al moviment d'empresa serà el del destí
                .data(request.data())
                .quantitat(isOrigen ? -request.quantitat() : request.quantitat())
                .empresaReceptora(isOrigen ? request.empresaDesti().clau() : request.empresaOrigen().clau())
                .liniaAlbara(request.idLiniaAlbara())
                .isVaImplicarTraspasMagatzem(isTraspasMagatzem) // El traspàs d'empresa no implica necessàriament un traspàs de magatzem
                .build();
    }

}
