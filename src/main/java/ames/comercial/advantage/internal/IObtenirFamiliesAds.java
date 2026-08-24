package ames.comercial.advantage.internal;

import ames.comercial.comandes.service.Familia;

import java.util.Map;
import java.util.Set;

public interface IObtenirFamiliesAds {

    Map<String, Familia> query(Set<String> articles);

}
