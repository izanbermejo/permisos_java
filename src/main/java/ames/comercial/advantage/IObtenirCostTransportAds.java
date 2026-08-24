package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import com.google.common.collect.TreeRangeMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface IObtenirCostTransportAds {
    Optional<TreeRangeMap<BigDecimal, BigDecimal>> get(String pais);
}
