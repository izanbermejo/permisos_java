package ames.comercial.inventari.ext;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

public interface ICrearMovimentTraspasMagatzemEmpresa {

    void executar(CrearMovimentTraspasMagatzemEmpresaRequest request);

    @JsonDeserialize(builder = CrearMovimentTraspasMagatzemEmpresaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface CrearMovimentTraspasMagatzemEmpresaRequest {
        KeyArticleClient articleClient();
        KeyLiniaAlbara idLiniaAlbara();
        LocalDate data();
        long quantitat();
        Empresa empresaOrigen();
        String magatzemOrigen();
        Empresa empresaDesti();
        String magatzemDesti();
    }

}
