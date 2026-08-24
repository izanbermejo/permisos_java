package ames.comercial.edi2.internal.application.query;

import ames.comercial.edi2.internal.application.query.ObtenirUsuariEDI.ObtenirUsuariEDIResponse;

import java.util.List;

public interface IObtenirUsuarisEDI {
    List<ObtenirUsuariEDIResponse> all();
}
