package ames.comercial.ofs.internal.task.actions;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.ofs.internal.application.command.CalcularOfMedical;
import ames.comercial.server.MapperUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TascaCalcularOfsMedicalAction {

    static final Logger log = LogManager.getLogger(TascaCalcularOfsMedicalAction.class.getName());

    @Autowired CalcularOfMedical calcul;

    @Transactional
    public void executar() {
        // Obtenció dels articles de Medical i càlcul per cadascun d'ells
        var articles = obtenirArticlesClient();
        for (var article : articles) {
            try {
                calcul.executar(article);
            } catch (Exception e) {
                var missatge = "OF_MEDICAL Error calculant l'OF de Medical de l'articleclient " + article.aclfab() + "(artint: " + article.artint() + ")";
                log.error(missatge, e);
            }
        }
    }

    private List<ArtcliMedical> obtenirArticlesClient() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						SELECT ac.*, art.artfam, art.artperfab
						FROM comundb.artcli ac
						LEFT JOIN comundb.art art ON ac.artint = art.artint
						WHERE ac.aclflg = 'A' AND tipus = 'M'
						""");
        AdvantageDao.ResultSetAction<List<ArtcliMedical>> rsAction = rs -> {
            var result = new ArrayList<ArtcliMedical>();
            while (rs.next()) {
                result.add(ArtcliMedicalImpl.builder()
                                .artint(rs.getString("artint"))
                                .aclfab(rs.getString("aclfab"))
                                .aclden(rs.getString("aclden"))
                                .aclref(rs.getString("aclref"))
                                .codiFabrica(rs.getString("codfab"))
                                .stockMinim(rs.getLong("aclsts"))
                                .lotMinim(rs.getLong("acllmin"))
                                .periodeFabricacio(MapperUtils.readOptionalLong(rs, "artperfab").filter(p -> p>0))  // Descarta els períodes 0
                        .build());
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = ArtcliMedicalImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ArtcliMedical {
        String artint();
        String aclfab();
        String aclden();
        String aclref();
        String codiFabrica();
        long stockMinim();
        long lotMinim();
        Optional<Long> periodeFabricacio();
    }

}
