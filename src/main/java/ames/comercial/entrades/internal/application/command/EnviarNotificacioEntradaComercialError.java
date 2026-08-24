package ames.comercial.entrades.internal.application.command;

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
public class EnviarNotificacioEntradaComercialError {

    final AdresesErrorEntrada adresesError;
    final EmailValidation emailValidation;
    final IEmailAdresesProvider adresesProvider;
    final JavaMailSender mailSender;
    @Value("${enviament-email-entrades-error}") boolean enviarEmail;

    final String emailFrom = "inputs@ames.group";
    final String assumpte = "Error a entrada comercial / Error in commercial entry";
    final String contingut = """
            S'ha detectar un error en processar l'entrada {idEntrada}.
            Entra a l'aplicació de comercial per revisar l'entrada i solucionar l'error.
            
            ---
            
            An error was detected while processing the entry {idEntrada}.
            Please log in to the commercial application to review the entry and resolve the error.
            """;

    public EnviarNotificacioEntradaComercialError(AdresesErrorEntrada adresesError, IEmailAdresesProvider adresesProvider,
                                                  EmailValidation emailValidation, JavaMailSender mailSender) {
        this.adresesError = adresesError;
        this.emailValidation = emailValidation;
        this.adresesProvider = adresesProvider;
        this.mailSender = mailSender;
    }

    public void executar (String idEntrada) {

        // Comprovació de les adreces d'email
        var listTo = emailValidation.splitAndCheck(adresesError.obtenirComercial());

        // Creació del missatge
        try {
            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true);
            // From
            helper.setFrom(emailFrom);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            helper.setSubject(assumpte);
            // Body
            String cosCorreu = contingut.replace("{idEntrada}", idEntrada);
            helper.setText(cosCorreu);

            if (enviarEmail)
                mailSender.send(missatge);
        } catch (Exception err) {
            throw new ErrorEnviantCorreuErrorEntrada(err);
        }
    }

}
