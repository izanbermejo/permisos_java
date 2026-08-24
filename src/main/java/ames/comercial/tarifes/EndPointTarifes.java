package ames.comercial.tarifes;

import ames.comercial.server.BeanUtils;
import ames.comercial.server.RequestThread;
import ames.comercial.tarifes.beans.Preu;
import ames.comercial.tarifes.beans.Tarifa;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import ames.comercial.tarifes.service.CalculadoraTarifes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Path("/tarifes")
public class EndPointTarifes {

    static final Logger log = LogManager.getLogger(EndPointTarifes.class.getName());

    @Autowired
    private TarifesService tarifesService;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DadesTarifa> list(@QueryParam("nom") String nom, @QueryParam("divisa") String divisa, @QueryParam("any") Integer any, @QueryParam("estat") String estat) {
        log.trace("CALL Llistat Tarifes"); //Exemple de traça envi al log
        return tarifesService.findTarifes(nom, divisa, any, estat);
    }

    @POST
    @Path("/newOld")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response newTarifaOld(@FormDataParam("file") InputStream inputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("nom") String nom,
                                 @FormDataParam("divisa") String divisa, @FormDataParam("vinculada") Long vinculada) {

        String user = "DEV_PENDING_USER_FROM_CREDENTIALS";
        if (RequestThread.nomUsuari()!=null)
            user = RequestThread.nomUsuari();
        String divisaAlta = null;

        // Validació parametres requerits
        if (nom == null || nom.isEmpty())
            return Response.serverError().entity("La tarifa no es pot donar d'alta. El nom es obligatori").build();

        Optional<DadesTarifa> tarifaVinculada = null;
        if (vinculada != null) {
            if (divisa != null && !divisa.isEmpty())
                return Response.serverError().entity("Dades incompatibles, no es pot informar la divisa quan a l'hora vinculem una tarifa").build();
            tarifaVinculada = tarifesService.findByCodi(vinculada);
            if (tarifaVinculada == null || tarifaVinculada.isEmpty() || !tarifaVinculada.get().status().equals(DadesTarifa.ENUM_STATUS_CLOSED))
                return Response.serverError().entity("La tarifa no es pot donar d'alta. La tarifa vinculada seleccionada no existeix o bé no esta tancada").build();
            else
                divisaAlta = tarifaVinculada.get().divisa();
        } else if (divisa == null && !divisa.isEmpty()) {
            return Response.serverError().entity("La tarifa no es pot donar d'alta. La divisa ha d'estar informada o bé vincular la tarifa").build();
        } else {
            if (!DadesTarifa.isValidDivisa(divisa))
                return Response.serverError().entity("La tarifa no es pot donar d'alta. La divisa no esta entre les admeses (" + DadesTarifa.Divises.totes() + ")").build();
            divisaAlta = divisa;
        }

        if (tarifesService.exists(nom))
            return Response.serverError().entity("La tarifa no es pot donar d'alta. Ja exiteix en el sistema amb el mateix nom").build();

        Tarifa oTarifa = null;
        try {
            oTarifa = tarifesService.getPreusFromXLSOld(inputStream, nom, divisaAlta);
            if (!oTarifa.getValidationResult().isValid())
                return Response.serverError().entity(oTarifa.getValidationResult().getDescription()).build();
            else {
                tarifesService.newTarifaOld(oTarifa, user, vinculada);
                return Response.ok().entity("").build();
            }
        } catch (IOException e) {
            log.error(e.getMessage()); //Exemple de error enviat al log
            throw new RuntimeException(e);
        }
    }


    @POST
    @Path("/new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void newTarifa(@FormDataParam("file") InputStream inputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {

        String user = "DEV_PENDING_USER_FROM_CREDENTIALS";
        if (RequestThread.nomUsuari()!=null)
            user = RequestThread.nomUsuari();
        String divisaAlta = null;

        // Validació parametres requerits
//        if (nom == null || nom.isEmpty())
//            return Response.serverError().entity("La tarifa no es pot donar d'alta. El nom es obligatori").build();
//
//        Optional<DadesTarifa> tarifaVinculada = null;
//        if (vinculada != null) {
//            if (divisa != null && !divisa.isEmpty())
//                return Response.serverError().entity("Dades incompatibles, no es pot informar la divisa quan a l'hora vinculem una tarifa").build();
//            tarifaVinculada = tarifesService.findByCodi(vinculada);
//            if (tarifaVinculada == null || tarifaVinculada.isEmpty() || !tarifaVinculada.get().status().equals(DadesTarifa.ENUM_STATUS_CLOSED))
//                return Response.serverError().entity("La tarifa no es pot donar d'alta. La tarifa vinculada seleccionada no existeix o bé no esta tancada").build();
//            else
//                divisaAlta = tarifaVinculada.get().divisa();
//        } else if (divisa == null && !divisa.isEmpty()) {
//            return Response.serverError().entity("La tarifa no es pot donar d'alta. La divisa ha d'estar informada o bé vincular la tarifa").build();
//        } else {
//            if (!DadesTarifa.isValidDivisa(divisa))
//                return Response.serverError().entity("La tarifa no es pot donar d'alta. La divisa no esta entre les admeses (" + DadesTarifa.Divises.totes() + ")").build();
//            divisaAlta = divisa;
//        }
//
//        if (tarifesService.exists(nom))
//            return Response.serverError().entity("La tarifa no es pot donar d'alta. Ja exiteix en el sistema amb el mateix nom").build();

        Tarifa oTarifa = null;
        try {
            oTarifa = tarifesService.getPreusFromXLS(inputStream);

            System.out.println("Nom:" + oTarifa.getNom());
            System.out.println("Divisa:" + oTarifa.getDivisa());

            if (oTarifa.getValidationResult().isValid()) {
//                return Response.serverError().entity(oTarifa.getValidationResult().getDescription()).build();
//            else {
                tarifesService.newTarifa(oTarifa, user);
//                return Response.ok().entity("").build();
            }
            else {
                throw new TarifesException.TarifaNoValida(oTarifa.getValidationResult().getDescription());
            }

        } catch (IOException e) {
            log.error(e.getMessage()); //Exemple de error enviat al log
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("download/{codi}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadExcel(@PathParam("codi") Long codi) {
//		Optional<DadesTarifa> oTarifa = tarifesService.findByNom(nom);
        Optional<DadesTarifa> oTarifa = tarifesService.findByCodi(codi);
        if (oTarifa.isEmpty())
            return Response.serverError().entity("No es possible descarregar la tarifa. No existeix").build();
        else {
            if (oTarifa.get().deleted()) {
                return Response.serverError().entity("No es possible descarregar la tarifa. No existeix. Va ser esborrada @"
                        + oTarifa.get().deletedAt() + " per " + oTarifa.get().deletedBy()).build();
            }
            List<Preu> preus = tarifesService.findPreus(oTarifa.get().codi().get());
            if (preus != null && preus.size() > 0) {
                try {
                    StreamingOutput resource = tarifesService.downloadXLS(oTarifa.get(), preus);
                    return Response.ok(resource)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=" + oTarifa.get().nom() + ".xls")
                            .header(HttpHeaders.CONTENT_TYPE,
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                    return Response.serverError().entity("Excepcion IO. Detalles: " + e.getMessage()).build();
                }
            } else
                return Response.noContent().build();
        }
    }

    @PUT
    @Path("/update/{codi}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response updateTarifa(@FormDataParam("file") InputStream inputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileDetail, @PathParam("codi") Long codi, @FormDataParam("nom") String nom, @FormDataParam("divisa") String divisa,
                                 @FormDataParam("vinculada") Long vinculada, @FormDataParam("reason") String reason) {
        String user = "DEV_PENDING_USER_FROM_CREDENTIALS";
        if (RequestThread.nomUsuari()!=null)
            user = RequestThread.nomUsuari();
        String novaDivisa = null;
        if (nom == null || nom.isEmpty() || reason == null || reason.isEmpty())
            return Response.serverError().entity("La tarifa no es pot actualitzar. El nom i el motiu del canvi son obligatoris").build();

        Optional<DadesTarifa> tarifaOld = tarifesService.findByCodi(codi);
//		if (tarifa==null || tarifa.isEmpty())
        if (!tarifesService.exists(codi))
            return Response.serverError().entity("La tarifa no es pot actualitzar. No existeix").build();
        if (tarifesService.exists(nom) && !tarifaOld.get().nom().equals(nom))
            return Response.serverError().entity("La tarifa no es pot actualitzar. Ja existeix una amb el mateix nom").build();


        Optional<DadesTarifa> tarifaVinculada = null;
        if (vinculada != null) {
            if (divisa != null)
                return Response.serverError().entity("Dades incompatibles, no es pot informar la divisa quan a l'hora vinculem una tarifa").build();
            tarifaVinculada = tarifesService.findByCodi(vinculada);
            if (tarifaVinculada == null || tarifaVinculada.isEmpty() || !tarifaVinculada.get().status().equals(DadesTarifa.ENUM_STATUS_CLOSED))
                return Response.serverError().entity("La tarifa no es pot actualitzar. La tarifa vinculada seleccionada no existeix o bé no esta tancada").build();
            else
                novaDivisa
                        = tarifaVinculada.get().divisa();
        } else if (divisa == null) {
            return Response.serverError().entity("La tarifa no es pot actualitzar. La divisa ha d'estar informada o bé vincular la tarifa").build();
        } else {
            if (!DadesTarifa.isValidDivisa(divisa))
                return Response.serverError().entity("La tarifa no es pot donar d'alta. La divisa no esta entre les admeses (" + DadesTarifa.Divises.totes() + ")").build();
            novaDivisa = divisa;
        }

        // Es vol actualitzar els preus
        if (inputStream != null) {
            try {
                Tarifa tarifa = tarifesService.getPreusFromXLSOld(inputStream, nom, divisa);
                tarifesService.updateTarifa(codi, nom, novaDivisa, vinculada, user, reason, tarifa.getPreus());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            tarifesService.updateTarifa(codi, nom, novaDivisa, vinculada, user, reason);
        }

        return Response.ok().entity("").build();
    }

    @PUT
    @Path("close/{codi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeStatusTarifa(@PathParam("codi") Long codi) {
        String user = "DEV_PENDING_USER_FROM_CREDENTIALS";
        if (RequestThread.nomUsuari()!=null)
            user = RequestThread.nomUsuari();
        Optional<DadesTarifa> existingTarifa = tarifesService.findByCodi(codi);
        if (existingTarifa.isEmpty())
            return Response.serverError().entity("Tarifa no existent").build();
        else if (existingTarifa.get().status().equals(DadesTarifa.ENUM_STATUS_CLOSED))
            return Response.serverError().entity("Tarifa ja tancada, no es pot tancar de nou").build();
        else {
            tarifesService.tancarTarifa(existingTarifa.get().codi().get(), user);
            return Response.ok().build();
        }
    }

    @DELETE
    @Path("delete/{codi}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTarifa(@PathParam("codi") Long codi, @RequestBody ReasonRequest reason) {
        String user = "DEV_PENDING_USER_FROM_CREDENTIALS";
        if (RequestThread.nomUsuari()!=null)
            user = RequestThread.nomUsuari();
        Optional<DadesTarifa> existingTarifa = tarifesService.findByCodi(codi);
        if (existingTarifa.isEmpty())
            return Response.serverError().entity("Tarifa no trobada").build();
        if (existingTarifa.get().status().equals(DadesTarifa.ENUM_STATUS_CLOSED))
            return Response.serverError().entity("No es pot borrar la tarifa. El seu estat es \"tancat\"").build();
//		else if (existingTarifa.get().deleted())
//			return Response.serverError().entity("Tarifa ja borrada, no es pot borrar de nou").build();
        else {
            return Response.ok().entity("Registres actualitzats=" + tarifesService.delete(existingTarifa.get().codi().get(), user)).build();
        }
    }

    @GET
    @Path("/rangs/{artint}/{clicod}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CalculadoraTarifes.RangPreuResp> obtenirTarifesByArtint(@PathParam("artint") String artint, @PathParam("clicod") String clicod) {
        return BeanUtils.getBean(CalculadoraTarifes.class).calcul(artint, clicod);
    }
}