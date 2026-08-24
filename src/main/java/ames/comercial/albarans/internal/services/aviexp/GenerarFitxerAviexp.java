package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.advantage.IObtenirEmpresesAds.EmpresaAds;
import ames.comercial.advantage.IObtenirPaisosAds;
import ames.comercial.advantage.IObtenirTransportistesAds;
import ames.comercial.advantage.internal.ObtenirPaisosAds.PaisAds;
import ames.comercial.advantage.internal.ObtenirTransportistesAds;
import ames.comercial.advantage.internal.ObtenirTransportistesAds.TransportistaAds;
import ames.comercial.albarans.internal.application.query.ObtenirPesNetAlbara;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenerarFitxerAviexp {

    @Autowired IObtenirPaisosAds obtenirPaisosAds;
    @Autowired IObtenirTransportistesAds obtenirTransportistesAds;
    @Autowired ObtenirPesNetAlbara obtenirPesNetAlbara;

    public String generar(Albara albara, CapsaleraEdi capsaleraEdi, ComandaEdi comandaEdi, EmpresaAds empresaAds) {
        // Obtenció del pais de destinació a través del codi de país del destinatari de l'albarà
        var paisDestinacio = obtenirPaisosAds.get(albara.adresa().pais())
                .map(PaisAds::iso)
                .orElse("XX");

        // Obtenció de la descripció del transportista
        var descTransportista = albara.informacioEnviament()
                .transportista()
                .flatMap(codiTransp -> obtenirTransportistesAds.get(codiTransp))
                .map(TransportistaAds::descripcio)
                .orElse("");

        // Obtenció del pes net de l'albarà
        var pesNetAlbara = obtenirPesNetAlbara.executar(albara.id());

        var sb = new StringBuilder();
        // CA
        sb.append(new GenerarCA(albara, capsaleraEdi.ca(), comandaEdi.la()).generar());
        // CB
        sb.append(new GenerarCB(albara, comandaEdi.lc(), empresaAds.dunsEnviament(), paisDestinacio).generar());
        // CC
        sb.append(new GenerarCC(capsaleraEdi.ci(), descTransportista).generar());
        // CD
        sb.append(new GenerarCD(albara, comandaEdi.la(), pesNetAlbara).generar());
        // CE
        sb.append(new GenerarCE(empresaAds).generar());
        // CO
        sb.append(new GenerarCO(albara, paisDestinacio).generar());
        // CP
        sb.append(new GenerarCP(empresaAds).generar());
        // CT
        sb.append(new GenerarCT(albara, comandaEdi.li(), descTransportista).generar());
        // CX
        sb.append(new GenerarCX(albara).generar());
        return sb.toString();
    }

}
