package ames.comercial.edi2.internal.domain.comanda;

import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.comanda.bloc.*;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ComandaEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaEdi {

    long idMissatge();
    long idCapsalera();
    long idComanda();
    Optional<String> artInt();
    Optional<String> cliCod();
    LA la();
    Optional<LB> lb();
    LC lc();
    Optional<LD> ld();
    Optional<LS> ls();
    List<LE> le();
    List<LT> lt();
    Optional<LG> lg();
    Optional<LH> lh();
    Optional<LI> li();
    List<LL> ll();
    List<LQ> lq();
    List<AA> aa();
    List<LiniaEdi> linies();
    Estat estat();
    Optional<String> error();

    enum Estat { PENDENT_LLIGAR, ERROR, PENDENT_PROCESSAR, PROCESSADA, ELIMINADA }

    @Derived
    default String nad02() {
        return lc().codigoConsignatario();
    }

    @Derived
    default String idArticleComprador() {
        return la().idArticuloComprador();
    }

    @Derived
    default String llocEntrega() {
        return la().lugarEntrega().orElse(null);
    }

    @Derived
    default String numeroComanda() {
        //Agafem la dada del camp numeroContratoPedido, ja que la majoria dels cops vindra declarat, pero si la comanda
        // ve de Nissan, alguns cops no ens dona aquesta dada i l'anem a agafar a les linies de la comanda.
        return lg().map(LG::numeroContratoPedido)
                .orElseGet(() -> linies().stream()
                        .map(LiniaEdi::numeroComanda)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElse(null));
    }

    @Derived
    default Optional<Long> numeroPecesEndarrerides() {
        long total = lq().stream()
            .flatMap(q -> q.cantidadAtraso().stream())
            .mapToLong(Long::longValue).sum();
        return total > 0 ? Optional.of(total) : Optional.empty();
    }

    @Derived
    default Long acumulatRebut() {
        return lq().stream()
            .flatMap(q -> q.cantidadAcumuladaRecibida().stream())
            .findFirst().orElse(0L);
    }

    @Derived
    default String referenciaAlbara() {
        //Alguns clients envien mes d'un albara a l'edi, per aquest motiu cal compara-los i agafar el mes recent per data.
        return aa().stream()
                .filter(a -> a.fechaAlbaran().isPresent())
                .max(Comparator.comparing(a -> a.fechaAlbaran().get()))
                .flatMap(AA::referenciaAlbaranEntrada)
                .orElse("");
    }

    @Derived
    default boolean isConsiderarAcumulats() {
        return lq().stream().anyMatch(q -> q.cantidadAcumuladaRecibida().isPresent());
    }

    default KeyArticleClient articleClient(){
        return KeyArticleClient.of(
            artInt().orElseThrow(() -> new IllegalStateException("artInt no informat")),
            cliCod().orElseThrow(() -> new IllegalStateException("cliCod no informat"))
        );
    }

    default KeyComandaEdi keyComandaEdi(){return KeyComandaEdi.of(idMissatge(), idComanda());}

    /**
     * Recupera el DA associat a la comanda EDI per a una data determinada.
     * En cas que no existeixi cap línia de la comanda EDI amb la data sol·licitada, retorna un Optional buit.
     * @param data Data sol·licitada per a la qual es vol recuperar el DA associat. Aquesta data es compara amb la data sol·licitada de les línies de la comanda EDI.
     * @return {@link Optional<DA>} que conté el DA associat a la comanda EDI per a la data sol·licitada, o un Optional buit si no existeix cap línia de la comanda EDI amb aquesta data sol·licitada.
     */

    default Optional<DA> da(LocalDate data, boolean isDuesDates) {
        return linies().stream()
                .filter(linia -> data.equals(linia.dataSolicitada(isDuesDates)))
                .findFirst()
                .map(LiniaEdi::da);
    }

    default ComandaEdi lligar(KeyArticleClient keyArticleClient) {
        if (!potLligar()) throw new EDIException.ComandaNoEsPotLligar(idComanda());
        return ComandaEdiImpl.builder()
                .from(this)
                .artInt(keyArticleClient.artint())
                .cliCod(keyArticleClient.clicod())
                .estat(Estat.PENDENT_PROCESSAR)
                .error(Optional.empty())
                .build();
    }

    default boolean potLligar() {
        return estat() != Estat.PROCESSADA;
    }

    default boolean potProcessarComanda(){ return estat().equals(Estat.PENDENT_PROCESSAR); }

    default ComandaEdi marcarError(String missatge, Optional<String> clicod) {
        return ComandaEdiImpl.builder().from(this)
                .estat(Estat.ERROR)
                .cliCod(clicod)
                .error(missatge)
                .build();
    }

    default ComandaEdi marcaComandaProcessada(){
        return ComandaEdiImpl.builder()
            .from(this)
            .estat(Estat.PROCESSADA)
            .error(Optional.empty())
            .build();
    }

    default ComandaEdi marcaComandaEsborrada(){
        return ComandaEdiImpl.builder()
                .from(this)
                .estat(Estat.ELIMINADA)
                .error(Optional.empty())
                .build();
    }

    default ComandaEdi canviarEdiboxNad02(String edibox, String nad02) {
        return ComandaEdiImpl.builder().from(this).build();
    }

}
