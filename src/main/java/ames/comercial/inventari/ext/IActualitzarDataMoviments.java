package ames.comercial.inventari.ext;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;

import java.time.LocalDate;
import java.util.List;

public interface IActualitzarDataMoviments {

    void executar(List<KeyLiniaAlbara> idLiniesAlbara, LocalDate data);

}
