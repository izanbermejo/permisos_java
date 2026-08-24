package ames.comercial.comandes.ext;

import ames.comercial.aviexp.internal.services.agrupacio.ClauAgrupacioAviExp;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;

import java.time.LocalDate;
import java.util.Map;

public interface IObtenirClausComandesEdi {

    Map<ClauAgrupacioAviExp, DataSolicitadaComandaEdi> executar(Iterable<KeyLiniaComanda> clausLiniesComanda);

    record DataSolicitadaComandaEdi(LocalDate dataSolicitada, KeyComandaEdi keyComandaEdi) {}

}
