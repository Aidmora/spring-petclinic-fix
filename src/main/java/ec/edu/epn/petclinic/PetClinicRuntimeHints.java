

package ec.edu.epn.petclinic;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import ec.edu.epn.petclinic.model.BaseEntity;
import ec.edu.epn.petclinic.model.Person;
import ec.edu.epn.petclinic.vet.Vet;

public class PetClinicRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.resources().registerPattern("db/*");
		hints.resources().registerPattern("messages/*");
		hints.resources().registerPattern("mysql-default-conf");
		hints.serialization().registerType(BaseEntity.class);
		hints.serialization().registerType(Person.class);
		hints.serialization().registerType(Vet.class);
	}

}
