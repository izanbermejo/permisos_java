package ames.comercial.edi2.response;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = CapsaleraProcessarEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CapsaleraProcessarEDIResponse {

    String artInt();
    String article();
    String codiClient();
    Empresa empresa();
    String denominacio();
    String nivellTecnic();
    String fabrica();
    long unitatsEmbalatge();
    long caixesPalet();
    String magatzemEntrada();
    String magatzemEntradaDesc();
    String empresaFacturacio();
    FormaEnviament formaEnviament();
    Incoterm incoterm();
    String desti();
    Long stockTotal();
    long diesTransit();
    String transportista();
    String diesSortida();
    String magatzemSortida();
    String magatzemSortidaDesc();

    @Value.Derived
    default String articleClient() {
        return article() + codiClient();
    }

}
