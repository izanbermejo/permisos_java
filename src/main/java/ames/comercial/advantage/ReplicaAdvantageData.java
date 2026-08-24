package ames.comercial.advantage;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;

import java.util.ArrayList;
import java.util.List;

public class ReplicaAdvantageData {

    private final List<Albara> albaraDelete = new ArrayList<>();
    private final List<Albara> albaraInsert = new ArrayList<>();
    private final List<Albara> updateFacturatAlbara = new ArrayList<>();
    private final List<Albara> updateFacturacioAutomaticaAlbara = new ArrayList<>();
    private final List<LiniaAlbara> liniaAlbaraDelete = new ArrayList<>();
    private final List<LiniaAlbara> liniaAlbaraInsert = new ArrayList<>();
    private final List<RegistreAbonament> penAboDelete = new ArrayList<>();
    private final List<RegistreAbonament> penAboInsert = new ArrayList<>();
    private final List<RegistreAbonament> penAboIncrement = new ArrayList<>();

    public ReplicaAdvantageData() {}

    public List<Albara> albaraDelete() {
        return albaraDelete;
    }

    public void addAlbaraDelete(Albara a) {
        albaraDelete.add(a);
    }

    public List<Albara> albaraInsert() {
        return albaraInsert;
    }

    public void addAlbaraInsert(Albara a) {
        albaraInsert.add(a);
    }

    public void addAlbaraDeleteInsert(Albara albara) {
        addAlbaraDelete(albara);
        addAlbaraInsert(albara);
    }

    public List<Albara> updateFacturatAlbara() {
        return updateFacturatAlbara;
    }

    public void addUpdateFacturatAlbara(Albara albara) {
        updateFacturatAlbara.add(albara);
    }

    public List<Albara> updateFacturacioAutomaticaAlbara() {
        return updateFacturacioAutomaticaAlbara;
    }

    public void addUpdateFacturacioAutomaticaAlbara(Albara albara) {
        updateFacturacioAutomaticaAlbara.add(albara);
    }

    public List<LiniaAlbara> liniaAlbaraDelete() {
        return liniaAlbaraDelete;
    }

    public void addLiniaAlbaraDelete(LiniaAlbara la) {
        liniaAlbaraDelete.add(la);
    }

    public List<LiniaAlbara> liniaAlbaraInsert() {
        return liniaAlbaraInsert;
    }

    public void addLiniaAlbaraInsert(LiniaAlbara la) {
        liniaAlbaraInsert.add(la);
    }

    public void addLiniaAlbaraDeleteInsert(LiniaAlbara liniaAlbara) {
        addLiniaAlbaraDelete(liniaAlbara);
        addLiniaAlbaraInsert(liniaAlbara);
    }

    // --- Pendents d'abonar dels traspassos abonables (penabo) ---

    /**
     * Un pendent d'abonar d'una línia de traspàs abonable. Porta la capçalera perquè la clau a
     * {@code penabo} es forma amb el número d'albarà, l'empresa de destí i la data de l'albarà, i la
     * quantitat a part de la línia perquè en un increment el que cal sumar és el delta, no el total.
     */
    public record RegistreAbonament(Albara albara, LiniaAlbara linia, long quantitat) {}

    public List<RegistreAbonament> penAboDelete() {
        return penAboDelete;
    }

    public void addPenAboDelete(Albara albara, LiniaAlbara linia) {
        penAboDelete.add(new RegistreAbonament(albara, linia, linia.quantitat()));
    }

    public List<RegistreAbonament> penAboInsert() {
        return penAboInsert;
    }

    public void addPenAboInsert(Albara albara, LiniaAlbara linia) {
        penAboInsert.add(new RegistreAbonament(albara, linia, linia.quantitat()));
    }

    public List<RegistreAbonament> penAboIncrement() {
        return penAboIncrement;
    }

    public void addPenAboIncrement(Albara albara, LiniaAlbara linia, long quantitat) {
        penAboIncrement.add(new RegistreAbonament(albara, linia, quantitat));
    }

}
