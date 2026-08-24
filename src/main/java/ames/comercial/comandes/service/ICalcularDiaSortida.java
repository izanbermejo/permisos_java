package ames.comercial.comandes.service;

import java.time.LocalDate;

public interface ICalcularDiaSortida {

    LocalDate executar (LocalDate dataSolicitada, long diesTransit, String diesServei);
    LocalDate executar (LocalDate dataSolicitada, long diesTransit);
}