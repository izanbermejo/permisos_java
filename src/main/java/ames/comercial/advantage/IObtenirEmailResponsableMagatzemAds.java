package ames.comercial.advantage;

import java.util.Optional;

public interface IObtenirEmailResponsableMagatzemAds {
    Optional<String> query(String magatzem);
}
