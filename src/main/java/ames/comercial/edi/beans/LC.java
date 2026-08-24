package ames.comercial.edi.beans;

public class LC {

    public static final int i_inicio = 2;
    public static final int i_codigoConsignatario = 37;
    public static final int i_nombreConsignatario = 72;
    public static final int i_personaContactoConsignatario = 107;
    public static final int i_telefonoConsignatario = 142;

    private String inicio;
    private String codigoConsignatario; // codigo plantadestino
    private String nombreConsignatario;
    private String personaContactoConsignatario; // persona de contacto de plantadestino
    private String telefonoConsignatario;

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getCodigoConsignatario() {
        return codigoConsignatario;
    }

    public void setCodigoConsignatario(String codigoConsignatario) {
        this.codigoConsignatario = codigoConsignatario;
    }

    public String getNombreConsignatario() {
        return nombreConsignatario;
    }

    public void setNombreConsignatario(String nombreConsignatario) {
        this.nombreConsignatario = nombreConsignatario;
    }

    public String getPersonaContactoConsignatario() {
        return personaContactoConsignatario;
    }

    public void setPersonaContactoConsignatario(String personaContactoConsignatario) {
        this.personaContactoConsignatario = personaContactoConsignatario;
    }

    public String getTelefonoConsignatario() {
        return telefonoConsignatario;
    }

    public void setTelefonoConsignatario(String telefonoConsignatario) {
        this.telefonoConsignatario = telefonoConsignatario;
    }

    @Override
    public String toString() {
        return "LC{" +
                "inicio='" + inicio + '\'' +
                ", codigoConsignatario='" + codigoConsignatario + '\'' +
                ", nombreConsignatario='" + nombreConsignatario + '\'' +
                ", personaContactoConsignatario='" + personaContactoConsignatario + '\'' +
                ", telefonoConsignatario='" + telefonoConsignatario + '\'' +
                '}';
    }

    public LC() {
        super();
    }
}
