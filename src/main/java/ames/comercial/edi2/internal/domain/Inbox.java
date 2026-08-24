package ames.comercial.edi2.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = InboxImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Inbox {

    long id();
    String missatge();
    String path();
    Optional<String> pathPDF();
    LocalDateTime dataReg();
    Optional<LocalDateTime> dataProcessat();
    String contingut();
    Optional<String> error();
    Optional<String> nomPdf();

    default Inbox canviPdf(String nouPath, String nouNomPdf) {
        return InboxImpl.builder()
                .from(this)
                .pathPDF(nouPath)
                .nomPdf(nouNomPdf)
                .build();
    }

    default Inbox eliminarPath() {
        return InboxImpl.builder()
                .from(this)
                .pathPDF(Optional.empty())
                .build();
    }

}