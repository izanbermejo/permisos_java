package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.util.Optional;

@JsonDeserialize(builder = QueryFacturesLiniesMovimentResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryFacturesLiniesMovimentResponse {

    long albara();
    String artint();
    String empcod();
    String fcctip();
    String fccnum();
    long fcllin();
    String tiplin();
    String matriu();
    String codcli();
    String fclcom();
    long fclq();
    BigDecimal fclpre();
    BigDecimal fclimp();
    BigDecimal fcldtonor();
    String divimplin();
    String fcldiv();
    Optional<String> fclfrannt();

    @Derived
    default String albaraFormatat() {
        return String.format("%07d", albara());
    }

    @Derived
    default String divisaDisplay() {
        return fcldiv().equals(divimplin()) ? fcldiv() : divimplin() + " " + fcldiv();
    }

}
