package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.advantage.internal.response.QueryClientEDIResponse;

import java.util.List;
import java.util.Optional;

public interface IObtenirClientEDIAds {
    Optional<ObtenirClientEDIAds.ClientEDIAds> get(String codcli, String document, String buzonDeEntrada);
}
