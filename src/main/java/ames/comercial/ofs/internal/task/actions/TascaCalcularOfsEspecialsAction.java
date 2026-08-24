package ames.comercial.ofs.internal.task.actions;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.ofs.internal.application.command.CalcularOfEspecial;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TascaCalcularOfsEspecialsAction {

    static final Logger log = LogManager.getLogger(TascaCalcularOfsEspecialsAction.class.getName());

    @Autowired CalcularOfEspecial calcul;

    public void executar() {
        // Obtenció dels articles i recorregut per cadascun d'ells
        var articles = obtenirArticlesClient();
        for (var article : articles) {
            try {
                calcul.executar(article);
            } catch (Exception e) {
                var missatge = "OF_ESPECIAL Error calculant l'OF especial de l'articleclient " + article.aclfab() + article.clicod() + "(artint: " + article.artint() + ")";
                log.error(missatge, e);
            }
        }
    }

    public List<Artcli> obtenirArticlesClient() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						SELECT ac.*, art.artperfab
						FROM comundb.artcli ac
						LEFT JOIN comundb.art art ON ac.artint = art.artint
						WHERE
						    clicod <> '000000'
						    AND ac.aclflg = 'A'
						    AND (tipus = 'E' OR tipus = 'F' OR tipus = 'I')
						""");
        AdvantageDao.ResultSetAction<List<Artcli>> rsAction = rs -> {
            var result = new ArrayList<Artcli>();
            while (rs.next()) {
                result.add(ArtcliImpl.builder()
                                .clau(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                                .empresa(Empresa.getByClau(rs.getString("empcod")))
                                .aclfab(rs.getString("aclfab"))
                                .aclden(rs.getString("aclden"))
                                .aclref(rs.getString("aclref"))
                                .codiFabrica(rs.getString("codfab"))
                                .stockSeguretat(rs.getLong("aclsts"))
                                .lotOptim(rs.getLong("acllmin"))
                                .periodeFabricacio(MapperUtils.readOptionalLong(rs, "artperfab").filter(p -> p>0))  // Descarta els períodes 0
                        .build());
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = ArtcliImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface Artcli {
        KeyArticleClient clau();
        Empresa empresa();
        String aclfab();
        String aclden();
        String aclref();
        String codiFabrica();
        long stockSeguretat();
        long lotOptim();
        Optional<Long> periodeFabricacio();
        @Derived default String artint() { return clau().artint(); }
        @Derived default String clicod() { return clau().clicod(); }

    }

}
