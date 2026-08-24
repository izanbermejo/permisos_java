package ames.comercial.comandes.internal.infraestructure.variacio;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonDeserialize(builder = VariacioBuidaReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface VariacioBuidaReq {
    KeyLiniaComanda liniaComanda();
    LocalDateTime datareg();
    KeyArticleClient articleClient();
    Divisa divisa();
    LocalDate dataVariacio();
}
