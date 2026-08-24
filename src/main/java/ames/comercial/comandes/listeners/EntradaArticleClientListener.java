package ames.comercial.comandes.listeners;

import ames.comercial.comandes.internal.infraestructure.entradanormalitzat.EntradaNormalitzatRepository;
import ames.comercial.entrades.events.EntradaArticleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EntradaArticleClientListener {

    @Autowired EntradaNormalitzatRepository entradaNormalitzatRepo;

    @EventListener
    public void onApplicationEvent(EntradaArticleEvent event) {
        // En cas que sigui normalitzat cal afegir-ho a l'entrada de normalitzats de comandes
        // per fer el recàlcul de reserves
        if (event.articleClient().isNormalitzat())
            entradaNormalitzatRepo.save(event.articleClient(), event.empresa());
    }

}
