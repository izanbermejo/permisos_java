package ames.comercial.server.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("produccio")
public class EmailAdresesProviderProduccio implements IEmailAdresesProvider {

    @Override
    public String[] provide(List<String> listAdreses) {
        return listAdreses.toArray(new String[0]);
    }

}
