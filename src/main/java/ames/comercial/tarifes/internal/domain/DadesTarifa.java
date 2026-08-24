package ames.comercial.tarifes.internal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = DadesTarifaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesTarifa {

    static enum Divises {
        USD, EUR, MXN,CNY ;
        public static String totes() {
            StringBuilder sb = new StringBuilder();
            for (Divises divisa : Divises.values()) {
                sb.append(divisa).append(", ");
            }
            // Elimina la última coma y espacio
            return sb.substring(0, sb.length() - 2);
        }
    }

    static String ENUM_STATUS_DRAFT = "DRAFT";
    static String ENUM_STATUS_CLOSED = "CLOSED";

//    @JsonIgnore
    Optional<Long> codi();
    String nom();
    String divisa();
    Integer tram01();
    Integer tram02();
    Integer tram03();
    Integer tram04();
    Integer tram05();
    Integer tram06();
    Integer tram07();
    Integer tram08();
    Integer tram09();
    Integer tram10();
    Integer tram11();
    Integer tram12();
    LocalDateTime insertedAt();
    String insertedBy();
    Optional<LocalDateTime> updatedAt();
    Optional<String> updatedBy();
    Optional<String> updatedReason();
    Optional<LocalDateTime> deletedAt();
    Optional<String> deletedBy();
    Optional<String> deletedReason();
    Boolean deleted();
    String status();
    Boolean enabled();
    Optional<Long> vinculada();
    Optional<String> descripcio();
    Optional<String> tipusClient();

    public static <T extends Enum<T>> boolean isValidDivisa(String value) {
        try {
            Enum.valueOf(Divises.class, value);
            return true;
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }
}
