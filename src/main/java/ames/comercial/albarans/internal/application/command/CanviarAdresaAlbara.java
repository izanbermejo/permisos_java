package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.request.CanviarAdresaAlbaraRequest;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.AdresaImpl;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Canvia les adreces d'un albarà (adreça d'enviament principal, adreça del bróker i adreça de la factura
 * proforma). Segueix el mateix patró que el canvi d'adreça de comanda, però sense recalcular el cost de
 * transport i bloquejant l'operació si l'albarà està tancat (validat al domini a {@link Albara#checkPotCanviarAdresa()}).
 */
@Service
public class CanviarAdresaAlbara {

    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara, CanviarAdresaAlbaraRequest req) {
        var albara = find(idAlbara);
        albaraRepository.save(albara.canviarAdresa(buildAdresa(req), buildInfoEnviament(req)));
    }

    @Transactional
    public void executarBroker(KeyAlbara idAlbara, Adresa adresaBroker) {
        var albara = find(idAlbara);
        albaraRepository.save(albara.canviarAdresaBroker(Optional.of(adresaBroker)));
    }

    @Transactional
    public void executarProforma(KeyAlbara idAlbara, Adresa adresaFacturaProforma) {
        var albara = find(idAlbara);
        albaraRepository.save(albara.canviarAdresaFacturaProforma(Optional.of(adresaFacturaProforma)));
    }

    private Albara find(KeyAlbara idAlbara) {
        return albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
    }

    private Adresa buildAdresa(CanviarAdresaAlbaraRequest req) {
        return AdresaImpl.builder()
                .destinatari(req.destinatari())
                .adresa(req.adresa())
                .poblacio(req.poblacio())
                .codiPostal(req.codiPostal())
                .pais(req.pais())
                .build();
    }

    private InformacioEnviament buildInfoEnviament(CanviarAdresaAlbaraRequest req) {
        return InformacioEnviamentImpl.builder()
                .formaEnviament(req.formaEnviament())
                .incoterm(req.incoterm())
                .desti(req.desti().orElse(""))
                .transportista(req.transportista())
                .zonaTransport(req.zonaTransport())
                .build();
    }

}
