package ames.comercial.edi.internal.domain;

import java.time.LocalDateTime;

public record DadesMissatgeEDIPerArticle(
        Long codi,
        String document,
        LocalDateTime data,
        String missatgeNumero,
        String numeroEnviament,
        String pathEDI
) {}
