package ames.comercial.entrades.internal.domain;

import ames.comercial.entrades.EntradesException;
import ames.comercial.entrades.internal.infraestructure.comercial.ErrorEntradaComercialRepository;
import ames.comercial.server.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@JsonDeserialize(builder = EntradaComercialImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EntradaComercial {

    // Per defecte es genera l'ID
    @Default default String id() { return UUID.randomUUID().toString(); }
    String idEntradaFabrica();
    String articleFabrica();
    String client();
    String magatzem();
    String fabrica();
    long quantitat();
    long quantitatCaixa();
    long of();
    LocalDate dataEntrada();
    BigDecimal pesPremsat();
    BigDecimal pesFinal();
    @Default default LocalDateTime dataAlta() { return LocalDateTime.now(); }
    // Opcional la data de processat
    Optional<LocalDateTime> dataProcessat();
    Optional<String> error();

    @Derived
    default boolean potReprocessar() {
        if (error().isPresent() || (error().isEmpty() && dataProcessat().isEmpty())) {
            return true;
        }
        return false;
    }

    default void checkPotReprocessar() {
        if (!potReprocessar()) {
            throw new EntradesException.NoPotReprocessar();
        }
    }

    default EntradaComercial canviArticleClient(String cliCod, String artCod) {
        if (!potReprocessar()) {
            throw new EntradesException.NoPotReprocessar();
        }
        return EntradaComercialImpl.builder()
                .from(this)
                .client(cliCod)
                .articleFabrica(artCod)
                .error(Optional.empty())
            .build();
    }

    default EntradaComercial canviOf(long of) {
        if (!potReprocessar()) {
            throw new EntradesException.NoPotReprocessar();
        }
        return EntradaComercialImpl.builder()
                .from(this)
                .of(of)
                .error(Optional.empty())
                .build();
    }

    static EntradaComercial of(EntradaMagatzem e, long quantitat) {
        return EntradaComercialImpl.builder()
                .idEntradaFabrica(e.idEntradaFabrica())
                .articleFabrica(e.articleFabrica())
                .client(e.client())
                .magatzem(e.magatzem())
                .fabrica(e.fabrica())
                .quantitat(quantitat)
                .quantitatCaixa(e.quantitatCaixa())
                .of(e.of())
                .dataEntrada(e.dataEntrada())
                .pesPremsat(e.pesPremsat())
                .pesFinal(e.pesFinal())
                .dataAlta(e.dataAlta())
                .build();
    }

    /**
     * Marca l'entrada comercial com a processada
     *
     * @return EntradaComercial marcada com a processada
     */
    default EntradaComercial processa() {
        return EntradaComercialImpl.builder()
                .from(this)
                .dataProcessat(LocalDateTime.now())
                .error(Optional.empty())
                .build();
    }

    /**
     * Marca l'entrada comercial com a error
     *
     * @return EntradaComercial marcada com a error amb el detall de l'error
     */
    default EntradaComercial processaError(String error) {
        var entradaProcessada = processa();
        return EntradaComercialImpl.builder()
                .from(entradaProcessada)
                .error(error)
                .build();
    }

}
