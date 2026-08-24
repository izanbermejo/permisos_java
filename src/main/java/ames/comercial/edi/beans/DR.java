package ames.comercial.edi.beans;

public class DR {
    public static final int i_inicio = 2;
    public static final int i_fechaEntradaEnlinea = 12;
    public static final int i_horaEntradaEnlinea = 17;

    private String inicio;
    private String fechaEntradaEnLinea;
    private String horaEntradaEnlinea;

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFechaEntradaEnLinea() {
        return fechaEntradaEnLinea;
    }

    public void setFechaEntradaEnLinea(String fechaEntradaEnLinea) {
        this.fechaEntradaEnLinea = fechaEntradaEnLinea;
    }

    public String getHoraEntradaEnlinea() {
        return horaEntradaEnlinea;
    }

    public void setHoraEntradaEnlinea(String horaEntradaEnlinea) {
        this.horaEntradaEnlinea = horaEntradaEnlinea;
    }

    @Override
    public String toString() {
        return "DR{" +
                "inicio='" + inicio + '\'' +
                ", fechaEntradaEnLinea='" + fechaEntradaEnLinea + '\'' +
                ", horaEntradaEnlinea='" + horaEntradaEnlinea + '\'' +
                '}';
    }
}
