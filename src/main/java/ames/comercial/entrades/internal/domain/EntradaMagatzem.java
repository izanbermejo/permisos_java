package ames.comercial.entrades.internal.domain;

import ames.comercial.entrades.internal.infraestructure.magatzem.ErrorEntradaMagatzemRepository;
import ames.comercial.server.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JsonDeserialize(builder = EntradaMagatzemImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EntradaMagatzem {

    // Per defecte es genera l'ID
    @Default default String id() { return UUID.randomUUID().toString(); }
    String idEntradaFabrica();
    String articleFabrica();
    String client();
    String magatzem();
    String fabrica();
    long quantitat();
    long quantitatCaixa();
    LocalDate dataEtiqueta();
    String lot();
    long of();
    long etiquetaCaixa();
    long etiquetaPalet();
    String nivellTecnic();
    String codiSeguretat();
    String codiCal();
    LocalDate dataEntrada();
    BigDecimal pesPremsat();
    BigDecimal pesFinal();
    @Default default List<EntradaMagatzemDetall> detalls() { return List.of(); }
    @Default default List<EntradaMagatzemEmbalatge> embalatges() { return List.of(); }
    // Per defecte la data d'alta és la data d'avui
    @Default default LocalDateTime dataAlta() { return LocalDateTime.now(); }
    // Opcional la data de processat
    Optional<LocalDateTime> dataProcessat();
    Optional<String> error();

    /**
     * En cas que l'entrada de magatzem tingui detalls vol dir que és un palet homogeni.
     * Els palets mixtes venen en diferents entrades de magatzem (una línia de capçalera per cada
     * caixa dins del palet, que tenen diferent etiqueta de caixa i de palet) i les caixes sueltes
     * també representent una entrada de magatzem (una línia de capçalera però en aquest cas
     * amb la mateixa etiqueta de caixa i de palet)
     *
     * @return true es tracta d'una palet homogeni, false altrament
     *
     */
    @Derived default boolean isPaletHomogeni() {
        return !detalls().isEmpty();
    }

    /**
     * Marca la capçalera com a processada
     *
     * @return Entrada processada
     */
    default EntradaMagatzem processa(){
        return EntradaMagatzemImpl.builder()
                .from(this)
                .dataProcessat(LocalDateTime.now())
                .build();
    }

    default EntradaMagatzem processa (List<EntradaMagatzemDetall> detallsProcessats) {
        var hiHanErrors = detallsProcessats.stream()
                .anyMatch(d -> d.error().isPresent());
        var entradaProcessada = processa();
        return EntradaMagatzemImpl.builder()
                .from(entradaProcessada)
                .detalls(detallsProcessats)
                .error(hiHanErrors ? Optional.of("ERROR_ALGUNA_CAIXA_DINS") : Optional.empty())
                .build();
    }

    /**
     * Quan hi ha un error de tota la capçalera es marca com error la capçalera i tots els seus detalls
     *
     * @return EntradaMagatzem marcada com a error i amb tots els detalls marcats com a error
     */
    default EntradaMagatzem processaError(String error) {
        var entradaProcessada = processa();
        var nousDetalls = entradaProcessada.detalls().stream()
                .map(d -> d.processaError(error))
                .toList();
        return EntradaMagatzemImpl.builder()
                .from(entradaProcessada)
                .error(error)
                .detalls(nousDetalls)
                .build();
    }

    @JsonDeserialize(builder = EntradaMagatzemDetallImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface EntradaMagatzemDetall {
        long etiquetaCaixa();
        long quantitat();
        LocalDate dataEtiqueta();
        String lot();
        long of();
        Optional<String> error();

        @Derived default EntradaMagatzemDetall processaError(String error) {
            return EntradaMagatzemDetallImpl.builder()
                    .from(this)
                    .error(error)
                    .build();
        }
    }

    @JsonDeserialize(builder = EntradaMagatzemEmbalatgeImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface EntradaMagatzemEmbalatge {
        String article();
        String client();
        String codiElement();
        Optional<Long> etiquetaCaixa();
        long etiquetaPalet();
        String descripcio();
        long quantitat();
        @Derived default long etiqueta() {
            return etiquetaCaixa().orElse(etiquetaPalet());
        }
    }

}
