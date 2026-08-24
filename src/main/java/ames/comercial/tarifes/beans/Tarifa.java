package ames.comercial.tarifes.beans;

import ames.comercial.shared.ValidationResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class Tarifa {

    ValidationResult validationResult=new ValidationResult();

    public static final int i_tram1 = 6;
    public static final int i_tram2 = 7;
    public static final int i_tram3 = 8;
    public static final int i_tram4 = 9;
    public static final int i_tram5 = 10;
    public static final int i_tram6 = 11;
    public static final int i_tram7 = 12;
    public static final int i_tram8 = 13;
    public static final int i_tram9 = 14;
    public static final int i_tram10 = 15;
    public static final int i_tram11 = 16;
    public static final int i_tram12 = 17;

    private Long codi;
    @NotNull (message="El campo nombre es requerido")
    private String nom;
    @NotNull (message="El campo divisa es requerido")
    private String divisa;
    private Integer tram1;
    private Integer tram2;
    private Integer tram3;
    private Integer tram4;
    private Integer tram5;
    private Integer tram6;
    private Integer tram7;
    private Integer tram8;
    private Integer tram9;
    private Integer tram10;
    private Integer tram11;
    private Integer tram12;

    @Valid
    private List<Preu> preus;

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public Long getCodi() {
        return codi;
    }

    public void setCodi(Long codi) {
        this.codi = codi;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDivisa() {
        return divisa;
    }

    public void setDivisa(String divisa) {
        this.divisa = divisa;
    }

    public List<Preu> getPreus() {
        return preus;
    }

    public void setPreus(List<Preu> preus) {
        this.preus = preus;
    }

    public Integer getTram1() {
        return tram1;
    }

    public void setTram1(Integer tram1) {
        this.tram1 = tram1;
    }

    public Integer getTram2() {
        return tram2;
    }

    public void setTram2(Integer tram2) {
        this.tram2 = tram2;
    }

    public Integer getTram3() {
        return tram3;
    }

    public void setTram3(Integer tram3) {
        this.tram3 = tram3;
    }

    public Integer getTram4() {
        return tram4;
    }

    public void setTram4(Integer tram4) {
        this.tram4 = tram4;
    }

    public Integer getTram5() {
        return tram5;
    }

    public void setTram5(Integer tram5) {
        this.tram5 = tram5;
    }

    public Integer getTram6() {
        return tram6;
    }

    public void setTram6(Integer tram6) {
        this.tram6 = tram6;
    }

    public Integer getTram7() {
        return tram7;
    }

    public void setTram7(Integer tram7) {
        this.tram7 = tram7;
    }

    public Integer getTram8() {
        return tram8;
    }

    public void setTram8(Integer tram8) {
        this.tram8 = tram8;
    }

    public Integer getTram9() {
        return tram9;
    }

    public void setTram9(Integer tram9) {
        this.tram9 = tram9;
    }

    public Integer getTram10() {
        return tram10;
    }

    public void setTram10(Integer tram10) {
        this.tram10 = tram10;
    }

    public Integer getTram11() {
        return tram11;
    }

    public void setTram11(Integer tram11) {
        this.tram11 = tram11;
    }

    public Integer getTram12() {
        return tram12;
    }

    public void setTram12(Integer tram12) {
        this.tram12 = tram12;
    }

    @Override
    public String toString() {
        return "Tarifa{" +
                "validationResult=" + validationResult +
                ", codi=" + codi +
                ", nom='" + nom + '\'' +
                ", divisa='" + divisa + '\'' +
                ", tram1=" + tram1 +
                ", tram2=" + tram2 +
                ", tram3=" + tram3 +
                ", tram4=" + tram4 +
                ", tram5=" + tram5 +
                ", tram6=" + tram6 +
                ", tram7=" + tram7 +
                ", tram8=" + tram8 +
                ", tram9=" + tram9 +
                ", tram10=" + tram10 +
                ", tram11=" + tram11 +
                ", tram12=" + tram12 +
                ", preus=" + preus +
                '}';
    }
}
