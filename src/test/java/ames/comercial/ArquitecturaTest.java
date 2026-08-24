package ames.comercial;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ArquitecturaTest {

	ApplicationModules modules = ApplicationModules.of(Application.class);

	@Test
	void verificacioDependenciaModuls() {
		modules.verify();
	}
	
	@Test
	void generaDocumentacio() {
		new Documenter(modules)
			.writeModulesAsPlantUml()
			.writeIndividualModulesAsPlantUml();
	}
	
}
