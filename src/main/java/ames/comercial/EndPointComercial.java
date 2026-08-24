package ames.comercial;

import ames.comercial.advantage.*;
import ames.comercial.albarans.ext.IObtenirUsuarisCreadorsAlbarans;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.edi2.internal.application.query.IObtenirUsuarisEDI;
import ames.comercial.cache.clients.IObtenirResponsablesLogistica;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.SimpleItem;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Path("/")
public class EndPointComercial {

    @GET
    @Path("metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Metadata obtenirMetadata() {
        return MetadataImpl.builder()
                .paisos(buildPaisos())
                .transportistes(buildTransportistes())
                .formesEnviament(buildFormesEnviament())
                .incoterms(buildIncoterms())
                .destinsTransport(buildDestinsTransport())
                .empreses(buildEmpreses())
                .responsablesLogistica(buildResponsablesLogistica())
                .magatzems(buildMagatzems())                .fabriques(buildFabriques())
                .usuarisEDI(buildUsuarisEDI())
                .usuarisCreadorsAlbarans(buildUsuarisCreadorsAlbarans())
                .build();
    }

    private List<SimpleItem> buildPaisos() {
        return BeanUtils.getBean(IObtenirPaisosAds.class).all().stream()
                .map(p -> SimpleItem.of(p.codi(), p.nom()))
                .sorted(Comparator.comparing(SimpleItem::nom))
                .toList();
    }

    private List<SimpleItem> buildTransportistes() {
        return BeanUtils.getBean(IObtenirTransportistesAds.class).all().stream()
                .map(t -> SimpleItem.of(t.codi(), t.descripcio()))
                .sorted(Comparator.comparing(SimpleItem::nom))
                .toList();
    }

    private List<SimpleItem> buildFormesEnviament() {
        return Arrays.stream(FormaEnviament.values())
                .map(f -> SimpleItem.of(f.name(), f.name()))
                .toList();
    }

    private List<SimpleItem> buildIncoterms() {
        return Arrays.stream(Incoterm.values())
                .map(f -> SimpleItem.of(f.name(), f.name()))
                .toList();
    }

    private List<SimpleItem> buildDestinsTransport() {
        return BeanUtils.getBean(IObtenirDestinsTransport.class).all().stream()
                .map(d -> SimpleItem.of(d.codi(), d.nom()))
                .sorted(Comparator.comparing(SimpleItem::nom))
                .toList();
    }

    private List<SimpleItem> buildEmpreses() {
        return BeanUtils.getBean(IObtenirEmpresesAds.class).all().stream()
                .map(e -> SimpleItem.of(e.codi(), e.descripcio()))
                .sorted(Comparator.comparing(SimpleItem::codi))
                .toList();
    }

    private List<SimpleItem> buildResponsablesLogistica() {
        return BeanUtils.getBean(IObtenirResponsablesLogistica.class).executar().stream()
                .map(r -> SimpleItem.of(r, r))
                .sorted(Comparator.comparing(SimpleItem::nom))
                .toList();
    }

    private List<SimpleItem> buildMagatzems() {
        return BeanUtils.getBean(IObtenirMagatzems.class).all().stream()
                .map(m -> SimpleItem.of(m.codi(), m.descripcio()))
                .sorted(Comparator.comparing(SimpleItem::codi))
                .toList();
    }

    private List<SimpleItem> buildFabriques() {
        return BeanUtils.getBean(IObtenirFabricaAds.class).all().stream()
                .map(m -> SimpleItem.of(m.codi(), m.descripcio()))
                .sorted(Comparator.comparing(SimpleItem::codi))
                .toList();
    }

    private List<SimpleItem> buildUsuarisEDI() {
        return BeanUtils.getBean(IObtenirUsuarisEDI.class).all().stream()
                .map(m -> SimpleItem.of(m.idResponsable(), m.responsable()))
                .sorted(Comparator.comparing(SimpleItem::codi))
                .toList();
    }

    /** Usuaris que han creat albarans en els darrers dos anys (codi = usufab, nom = nom i cognoms) */
    private List<SimpleItem> buildUsuarisCreadorsAlbarans() {
        return BeanUtils.getBean(IObtenirUsuarisCreadorsAlbarans.class).all().stream()
                .sorted(Comparator.comparing(SimpleItem::nom))
                .toList();
    }

    @JsonDeserialize(builder = MetadataImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface Metadata {
        List<SimpleItem> paisos();
        List<SimpleItem> transportistes();
        List<SimpleItem> formesEnviament();
        List<SimpleItem> incoterms();
        List<SimpleItem> destinsTransport();
        List<SimpleItem> empreses();
        List<SimpleItem> responsablesLogistica();
        List<SimpleItem> magatzems();
        List<SimpleItem> fabriques();
        List<SimpleItem> usuarisEDI();
        List<SimpleItem> usuarisCreadorsAlbarans();
    }

}
