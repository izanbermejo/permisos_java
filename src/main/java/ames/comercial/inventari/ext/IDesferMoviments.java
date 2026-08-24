package ames.comercial.inventari.ext;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;

import java.util.List;

public interface IDesferMoviments {

    void executar(List<KeyLiniaAlbara> idLiniesAlbara);

}
