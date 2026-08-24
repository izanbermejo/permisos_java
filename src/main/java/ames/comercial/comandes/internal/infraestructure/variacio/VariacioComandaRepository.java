package ames.comercial.comandes.internal.infraestructure.variacio;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VariacioComandaRepository {

    LocalDateTime maxDatareg();
    void insert(LiniaComanda liniaComanda, LocalDateTime datareg, LocalDate dataVariacio);
    void insertNegatiu(LiniaComanda liniaComanda, LocalDateTime datareg, LocalDate dataVariacio);

    List<VariacioBuidaReq> getPendentsComplementar();
    void complementa(KeyLiniaComanda liniaComanda, LocalDateTime datareg, VariacioComplReq complRecord);

    long variacio(KeyArticleClient articleClient, LocalDate dataVariacio, LocalDate dataSolicitadaLimit);
}
