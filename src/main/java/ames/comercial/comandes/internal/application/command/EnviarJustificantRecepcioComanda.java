package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.ErrorEnviantCorreuComanda;
import ames.comercial.comandes.internal.application.query.GenerarPdfJustificantComandaNormalitzat;
import ames.comercial.comandes.internal.domain.comanda.DadesEnviamentJustificant;
import ames.comercial.comandes.internal.domain.comanda.DadesEnviamentJustificantImpl;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepository;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.request.EnviarJustificantRecepecioRequest;
import ames.comercial.server.RequestThread;
import ames.comercial.server.service.EmailValidation;
import ames.comercial.server.service.IEmailAdresesProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class EnviarJustificantRecepcioComanda {

    final ComandaRepository comandaRepository;
    final GenerarPdfJustificantComandaNormalitzat generarPdfJustificant;
    final AdjuntComandaRepository adjuntComandaRepository;
    final EmailValidation emailValidation;
    final IEmailAdresesProvider adresesProvider;
    final JavaMailSender mailSender;
    @Value("${enviament-email-comandes}") boolean enviarEmail;

    public EnviarJustificantRecepcioComanda (ComandaRepository comandaRepository, GenerarPdfJustificantComandaNormalitzat generarPdfJustificant,
            AdjuntComandaRepository adjuntComandaRepository, IEmailAdresesProvider adresesProvider,
            EmailValidation emailValidation, JavaMailSender mailSender) {
        this.comandaRepository = comandaRepository;
        this.generarPdfJustificant = generarPdfJustificant;
        this.adjuntComandaRepository = adjuntComandaRepository;
        this.emailValidation = emailValidation;
        this.adresesProvider = adresesProvider;
        this.mailSender = mailSender;
    }

    public void executar (long codiComanda, EnviarJustificantRecepecioRequest request, Map<String, InputStream> fitxersAdjunts) {
        // Obtenció de la comanda
        var comanda = comandaRepository.find(codiComanda).orElseThrow(ComandaNoExisteix::new);

        // Comprovació de les adreces d'email
        var listTo = emailValidation.splitAndCheck(request.to());
        var listCc = emailValidation.splitAndCheck(request.cc());

        // Generació del PDF de justificant
        var pdfJustificant = generarPdfJustificant.run(codiComanda, request.isFormatDistribuidor(), request.idioma(), request.formatNumeric());

        // Emmagatzematge del justificant (en cas que hagi error a l'enviar el correu o a BBDD el justificant quedarà
        // penjat, però no es problema ja que a la propera execució es farà el replace)
        adjuntComandaRepository.add(codiComanda, new ByteArrayInputStream(pdfJustificant), "_JustificantIntern.pdf");

        // Informació de l'enviament
        DadesEnviamentJustificant dadesEnviament = DadesEnviamentJustificantImpl.builder()
                .to(request.to())
                .cc(request.cc())
                .assumpte(request.assumpte())
                .missatge(request.missatge())
                .data(LocalDateTime.now().withNano(0))
                .usuari(RequestThread.nomUsuari())
                .adjunts(fitxersAdjunts.keySet())
                .build();
        comanda.afegirDadesEnviamentJustificant(dadesEnviament);
        comandaRepository.save(comanda);

        // Creació del missatge
        try {
            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true);
            // From
            helper.setFrom(RequestThread.email());
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // CC
            if (!listCc.isEmpty())
                helper.setCc(adresesProvider.provide(listCc));
            // Subject
            helper.setSubject(request.assumpte());
            // Body
            helper.setText(request.missatge());
            // Adjunts
            for (String fitxer : fitxersAdjunts.keySet()) {
                helper.addAttachment(fitxer, new ByteArrayResource(IOUtils.toByteArray(fitxersAdjunts.get(fitxer))));
            }
            // PDF justificant
            helper.addAttachment(comanda.informacioClient().identificador() + ".pdf", new ByteArrayResource(pdfJustificant));
            if (enviarEmail)
                mailSender.send(missatge);
        } catch (Exception err) {
            throw new ErrorEnviantCorreuComanda(err);
        }
    }

}
