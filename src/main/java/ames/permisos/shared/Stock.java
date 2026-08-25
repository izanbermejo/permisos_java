package ames.permisos.shared;

public record Stock(long stock, long reservat) {

    public static Stock empty() {
        return new Stock(0, 0);
    }

    public long stockDisponible() {
        return stock-reservat;
    }

}
