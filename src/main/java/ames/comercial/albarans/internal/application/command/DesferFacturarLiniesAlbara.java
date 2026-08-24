package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.LiniaAlbaraNoExisteix;
import ames.comercial.albarans.internal.infraestructure.facturacio.FacturacioLiniaAlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesferFacturarLiniesAlbara {

    @Autowired FacturacioLiniaAlbaraRepository facturacioLiniaAlbaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired CanviarFacturatAlbara canviarFacturatAlbara;

    @Transactional
    public void executar(String empresa, String codiFactura) {
        // Recuperació de les línies d'albarà facturades associades a la factura
        var liniesFacturacio = facturacioLiniaAlbaraRepository.findByFactura(empresa, codiFactura);
        for (var liniaFacturacio : liniesFacturacio) {
            // Recuperació de la línia d'albarà a desfer facturar
            var liniaAlbara = liniaAlbaraRepository.find(liniaFacturacio.liniaAlbara())
                    .orElseThrow(() -> new LiniaAlbaraNoExisteix(liniaFacturacio.liniaAlbara()));
            // Desfer facturació de la línia
            var liniaAlbaraDesfacturada = liniaAlbara.desferFacturar(liniaFacturacio.quantitat());
            liniaAlbaraRepository.save(liniaAlbaraDesfacturada);
            ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(liniaAlbaraDesfacturada);
        }
        // Eliminar les anotacions de facturació de les línies d'albarà associades a la factura
        facturacioLiniaAlbaraRepository.deleteByFactura(empresa, codiFactura);
        // Es calcula el nou estat de facturació dels albarans associats a les línies d'albarà desfetes i es marquen com a no facturats si escau
        liniesFacturacio.stream()
                .map(liniaFacturacio -> liniaFacturacio.liniaAlbara().idAlbara())
                .distinct()
                .forEach(codiAlbara -> {
                    var isFacturat = liniaAlbaraRepository.isHiHaLiniesFacturades(codiAlbara);
                    if (!isFacturat) {
                        canviarFacturatAlbara.executar(codiAlbara, false);
                    }
                });
    }

}
