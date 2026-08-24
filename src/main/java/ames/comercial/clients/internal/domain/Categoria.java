package ames.comercial.clients.internal.domain;

public enum Categoria {

    COMANDES (1),
    INFORMACIO (2);

    private final int idCategoria;

    Categoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public int idCategoria () {
        return idCategoria;
    }

    public static Categoria getById(Long id) {
        for (Categoria categoria : values()) {
            if (categoria.idCategoria == id) {
                return categoria;
            }
        }

        return Categoria.INFORMACIO;
    }
}
