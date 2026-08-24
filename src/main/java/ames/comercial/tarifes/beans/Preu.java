package ames.comercial.tarifes.beans;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Preu {

    public static final int i_artInt = 4;
    public static final int i_aclFab = 5;
    public static final int i_aclRef = 6;
    public static final int i_pr01 = 7;
    public static final int i_pr02 = 8;
    public static final int i_pr03 = 9;
    public static final int i_pr04 = 10;
    public static final int i_pr05 = 11;
    public static final int i_pr06 = 12;
    public static final int i_pr07 = 13;
    public static final int i_pr08 = 14;
    public static final int i_pr09 = 15;
    public static final int i_pr10 = 16;
    public static final int i_pr11 = 17;
    public static final int i_pr12 = 18;
    public static final int i_unsBos = 19;
    public static final int i_unsCai = 20;

    private Long codi_tarifa;
    @NotNull(message = "El campo artInt es requerido")
    @NotBlank(message = "El campo artInt es requerido")
    private String artInt;
    @NotNull(message = "El campo artInt es requerido")
    private String aclFab;
    @NotNull(message = "El campo artInt es requerido")
    private String aclRef;
    @NotNull(message = "El campo pr01 es requerido")
    private Double pr01;
    @NotNull(message = "El campo pr02 es requerido")
    private Double pr02;
    @NotNull(message = "El campo pr03 es requerido")
    private Double pr03;
    @NotNull(message = "El campo pr04 es requerido")
    private Double pr04;
    @NotNull(message = "El campo pr05 es requerido")
    private Double pr05;
    @NotNull(message = "El campo pr06 es requerido")
    private Double pr06;
    @NotNull(message = "El campo pr07 es requerido")
    private Double pr07;
    @NotNull(message = "El campo pr08 es requerido")
    private Double pr08;
    @NotNull(message = "El campo pr09 es requerido")
    private Double pr09;
    @NotNull(message = "El campo pr10 es requerido")
    private Double pr10;
    @NotNull(message = "El campo pr11 es requerido")
    private Double pr11;
    @NotNull(message = "El campo pr12 es requerido")
    private Double pr12;

    public Long getCodi_tarifa() {
        return codi_tarifa;
    }

    public void setCodi_tarifa(Long codi_tarifa) {
        this.codi_tarifa = codi_tarifa;
    }

    public String getArtInt() {
        return artInt;
    }

    public void setArtInt(String artInt) {
        this.artInt = artInt;
    }

    public Double getPr01() {
        return pr01;
    }

    public void setPr01(Double pr01) {
        this.pr01 = pr01;
    }

    public Double getPr02() {
        return pr02;
    }

    public void setPr02(Double pr02) {
        this.pr02 = pr02;
    }

    public Double getPr03() {
        return pr03;
    }

    public void setPr03(Double pr03) {
        this.pr03 = pr03;
    }

    public Double getPr04() {
        return pr04;
    }

    public void setPr04(Double pr04) {
        this.pr04 = pr04;
    }

    public Double getPr05() {
        return pr05;
    }

    public void setPr05(Double pr05) {
        this.pr05 = pr05;
    }

    public Double getPr06() {
        return pr06;
    }

    public void setPr06(Double pr06) {
        this.pr06 = pr06;
    }

    public Double getPr07() {
        return pr07;
    }

    public void setPr07(Double pr07) {
        this.pr07 = pr07;
    }

    public Double getPr08() {
        return pr08;
    }

    public void setPr08(Double pr08) {
        this.pr08 = pr08;
    }

    public Double getPr09() {
        return pr09;
    }

    public void setPr09(Double pr09) {
        this.pr09 = pr09;
    }

    public Double getPr10() {
        return pr10;
    }

    public void setPr10(Double pr10) {
        this.pr10 = pr10;
    }

    public Double getPr11() {
        return pr11;
    }

    public void setPr11(Double pr11) {
        this.pr11 = pr11;
    }

    public Double getPr12() {
        return pr12;
    }

    public void setPr12(Double pr12) {
        this.pr12 = pr12;
    }

    public String getAclFab() {
        return aclFab;
    }

    public void setAclFab(String aclFab) {
        this.aclFab = aclFab;
    }

    public String getAclRef() {
        return aclRef;
    }

    public void setAclRef(String aclRef) {
        this.aclRef = aclRef;
    }

//    public Integer getUnsBos() {
//        return unsBos;
//    }
//
//    public void setUnsBos(Integer unsBos) {
//        this.unsBos = unsBos;
//    }
//
//    public Integer getUnsCai() {
//        return unsCai;
//    }
//
//    public void setUnsCai(Integer unsCai) {
//        this.unsCai = unsCai;
//    }

}
