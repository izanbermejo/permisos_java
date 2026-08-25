package ames.permisos.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("default")
public class EmailAdresesProviderLocal implements IEmailAdresesProvider {

    @Value("${enviament-email-adresa-proves}") String adresaComandes;

    @Override
    public String[] provide(List<String> listAdreses) {
        return new String[] { adresaComandes };
    }
}