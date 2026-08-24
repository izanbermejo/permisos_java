package ames.comercial.entrades.internal.application.command;

import ames.comercial.advantage.internal.ObtenirEmailResponsableMagatzemAds;
import ames.comercial.entrades.EntradesException.ErrorEnviantCorreuErrorEntrada;
import ames.comercial.entrades.internal.infraestructure.AdresesErrorEntrada;
import ames.comercial.server.service.EmailValidation;
import ames.comercial.server.service.IEmailAdresesProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class EnviarNotificacioEntradaMagatzemError {

    final AdresesErrorEntrada adresesError;
    final EmailValidation emailValidation;
    final ObtenirEmailResponsableMagatzemAds obtenirEmailResponsableMagatzemAds;
    final IEmailAdresesProvider adresesProvider;
    final JavaMailSender mailSender;
    @Value("${enviament-email-entrades-error}") boolean enviarEmail;

    final static String emailFrom = "inputs@ames.group";
    final static String assumpte = "Error a entrada del magatzem {magatzem} / Warehouse {magatzem} entry error";
    final static String contingut = """
            S'ha detectar un error en processar l'entrada {idEntrada} del magatzem {magatzem}.
            Entra a l'aplicació de comercial per revisar l'entrada i solucionar l'error.
            
            ---
            
            An error was detected while processing the entry {idEntrada} from the warehouse {magatzem}.
            Please log in to the commercial application to review the entry and resolve the error.
            """;

    public EnviarNotificacioEntradaMagatzemError(AdresesErrorEntrada adresesError, IEmailAdresesProvider adresesProvider,
                                                 ObtenirEmailResponsableMagatzemAds obtenirEmailResponsableMagatzemAds,
                                                 EmailValidation emailValidation, JavaMailSender mailSender) {
        this.adresesError = adresesError;
        this.emailValidation = emailValidation;
        this.obtenirEmailResponsableMagatzemAds = obtenirEmailResponsableMagatzemAds;
        this.adresesProvider = adresesProvider;
        this.mailSender = mailSender;
    }

    public void executar (String idEntrada, String magatzem) {

        // Comprovació de les adreces d'email
        var listTo = emailValidation.splitAndCheck(adresesError.obtenirMagatzem());

        // Afegir responsable del magatzem als destinataris del correu
        var emailResponsableMagatzem = obtenirEmailResponsableMagatzemAds.query(magatzem);
        emailResponsableMagatzem.ifPresent(listTo::add);

        // Creació del missatge
        try {
            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true);
            // From
            helper.setFrom(emailFrom);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            var assumpteCorreu = assumpte.replace("{magatzem}", magatzem);
            helper.setSubject(assumpteCorreu);
            // Body
            var cosCorreu = contingut.replace("{idEntrada}", idEntrada);
            cosCorreu = cosCorreu.replace("{magatzem}", magatzem);
            helper.setText(cosCorreu);

            if (enviarEmail)
                mailSender.send(missatge);
        } catch (Exception err) {
            throw new ErrorEnviantCorreuErrorEntrada(err);
        }
    }

}
