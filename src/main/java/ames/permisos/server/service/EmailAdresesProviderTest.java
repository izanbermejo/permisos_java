package ames.permisos.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("test")
public class EmailAdresesProviderTest implements IEmailAdresesProvider {

    @Value("${enviament-email-adresa-proves}") String adresaComandes;

    @Override
    public String[] provide(List<String> listAdreses) {
        // Es netejen totes les adreces que no siguin d'AMES
        var listOnlyAmes = listAdreses.stream()
                .filter(a -> a.contains("@ames.group"))
                .collect(Collectors.toSet());
        // En cas que no quedi cap adreça d'AMES s'afegeix la configurada
        // al properties
        if (listOnlyAmes.isEmpty())
            listOnlyAmes.add(adresaComandes);
        return listOnlyAmes.toArray(new String[0]);
    }
}