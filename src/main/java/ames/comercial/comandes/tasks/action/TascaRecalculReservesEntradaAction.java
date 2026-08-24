package ames.comercial.comandes.tasks.action;

import ames.comercial.comandes.internal.domain.service.RecalculReservesArticleclient;
import ames.comercial.comandes.internal.infraestructure.entradanormalitzat.EntradaNormalitzatRepository;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TascaRecalculReservesEntradaAction {

    @Autowired RecalculReservesArticleclient recalculReservesArticleclient;
    @Autowired EntradaNormalitzatRepository entradaNormalitzatRepo;

    @Transactional
    public void run(KeyArticleClient articleClient, Empresa empresa) {
        entradaNormalitzatRepo.delete(articleClient, empresa);
        recalculReservesArticleclient.executar(articleClient, empresa);
    }

}
