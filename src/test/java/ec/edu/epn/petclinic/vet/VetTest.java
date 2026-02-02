package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la entidad Vet.
 */
class VetTest {

	private Vet vet;

	@BeforeEach
	void setUp() {
		vet = new Vet();
		vet.setId(1);
		vet.setFirstName("James");
		vet.setLastName("Carter");
	}

	@Nested
	@DisplayName("addSpecialty() - Agregar especialidades")
	class AddSpecialtyTests {

		@Test
		@DisplayName("Debería agregar especialidad correctamente")
		void addSpecialty_ShouldAddSpecialty() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");
			// Act
			vet.addSpecialty(radiology);
			// Assert
			assertThat(vet.getSpecialties()).hasSize(1);
			assertThat(vet.getSpecialties()).extracting(Specialty::getName).contains("radiology");
		}

		@Test
		@DisplayName("Debería agregar múltiples especialidades")
		void addSpecialty_ShouldAddMultipleSpecialties() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");

			Specialty surgery = new Specialty();
			surgery.setId(2);
			surgery.setName("surgery");

			Specialty dentistry = new Specialty();
			dentistry.setId(3);
			dentistry.setName("dentistry");
			// Act
			vet.addSpecialty(radiology);
			vet.addSpecialty(surgery);
			vet.addSpecialty(dentistry);
			// Assert
			assertThat(vet.getSpecialties()).hasSize(3);
			assertThat(vet.getSpecialties()).extracting(Specialty::getName).containsExactlyInAnyOrder("radiology",
					"surgery", "dentistry");
		}

		@Test
		@DisplayName("No debería permitir especialidades duplicadas (Set behavior)")
		void addSpecialty_ShouldNotAllowDuplicates() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");
			// Act
			vet.addSpecialty(radiology);
			vet.addSpecialty(radiology); 
			// Assert
			assertThat(vet.getSpecialties()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("getSpecialties() - Obtener lista de especialidades")
	class GetSpecialtiesTests {

		@Test
		@DisplayName("Debería retornar lista vacía cuando no hay especialidades")
		void getSpecialties_ShouldReturnEmptyList_WhenNoSpecialties() {
			// Act
			List<Specialty> specialties = vet.getSpecialties();
			// Assert
			assertThat(specialties).isEmpty();
		}

		@Test
		@DisplayName("Debería retornar todas las especialidades agregadas")
		void getSpecialties_ShouldReturnAllSpecialties() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");

			Specialty surgery = new Specialty();
			surgery.setId(2);
			surgery.setName("surgery");

			vet.addSpecialty(radiology);
			vet.addSpecialty(surgery);
			// Act
			List<Specialty> specialties = vet.getSpecialties();
			// Assert
			assertThat(specialties).hasSize(2);
			assertThat(specialties).extracting(Specialty::getName).containsExactlyInAnyOrder("radiology", "surgery");
		}

		@Test
		@DisplayName("Debería retornar especialidades ordenadas alfabéticamente por nombre")
		void getSpecialties_ShouldReturnSortedByName() {
			// Arrange
			Specialty surgery = new Specialty();
			surgery.setId(1);
			surgery.setName("surgery");

			Specialty dentistry = new Specialty();
			dentistry.setId(2);
			dentistry.setName("dentistry");

			Specialty radiology = new Specialty();
			radiology.setId(3);
			radiology.setName("radiology");

			// Agregar en orden diferente al alfabético
			vet.addSpecialty(surgery);
			vet.addSpecialty(dentistry);
			vet.addSpecialty(radiology);
			// Act
			List<Specialty> specialties = vet.getSpecialties();
			// Assert
			assertThat(specialties).hasSize(3);
			assertThat(specialties).extracting(Specialty::getName).containsExactly("dentistry", "radiology",
					"surgery");
		}

		@Test
		@DisplayName("Debería retornar lista nueva en cada llamada (no Set interno)")
		void getSpecialties_ShouldReturnNewListEachTime() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");
			vet.addSpecialty(radiology);
			// Act
			List<Specialty> lista1 = vet.getSpecialties();
			List<Specialty> lista2 = vet.getSpecialties();
			// Assert
			assertThat(lista1).isNotSameAs(lista2);
			assertThat(lista1).containsExactlyElementsOf(lista2);
		}

		@Test
		@DisplayName("Debería mantener consistencia en orden con múltiples especialidades")
		void getSpecialties_ShouldMaintainConsistentOrder() {
			// Arrange
			Specialty cardiology = new Specialty();
			cardiology.setName("cardiology");
			Specialty neurology = new Specialty();
			neurology.setName("neurology");
			Specialty oncology = new Specialty();
			oncology.setName("oncology");
			Specialty anesthesiology = new Specialty();
			anesthesiology.setName("anesthesiology");

			vet.addSpecialty(cardiology);
			vet.addSpecialty(neurology);
			vet.addSpecialty(oncology);
			vet.addSpecialty(anesthesiology);
			// Act
			List<Specialty> specialties = vet.getSpecialties();
			// Assert
			assertThat(specialties).extracting(Specialty::getName).containsExactly("anesthesiology", "cardiology",
					"neurology", "oncology");
		}
	}

	@Nested
	@DisplayName("getNrOfSpecialties() - Obtener número de especialidades")
	class GetNrOfSpecialtiesTests {

		@Test
		@DisplayName("Debería retornar 0 cuando no hay especialidades")
		void getNrOfSpecialties_ShouldReturnZero_WhenNoSpecialties() {
			// Act
			int count = vet.getNrOfSpecialties();
			// Assert
			assertThat(count).isZero();
		}

		@Test
		@DisplayName("Debería retornar 1 cuando hay una especialidad")
		void getNrOfSpecialties_ShouldReturnOne_WhenOneSpecialty() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setName("radiology");
			vet.addSpecialty(radiology);
			// Act
			int count = vet.getNrOfSpecialties();
			// Assert
			assertThat(count).isEqualTo(1);
		}

		@Test
		@DisplayName("Debería retornar el número correcto de especialidades")
		void getNrOfSpecialties_ShouldReturnCorrectCount() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");

			Specialty surgery = new Specialty();
			surgery.setId(2);
			surgery.setName("surgery");

			Specialty dentistry = new Specialty();
			dentistry.setId(3);
			dentistry.setName("dentistry");

			vet.addSpecialty(radiology);
			vet.addSpecialty(surgery);
			vet.addSpecialty(dentistry);
			// Act
			int count = vet.getNrOfSpecialties();
			// Assert
			assertThat(count).isEqualTo(3);
		}

		@Test
		@DisplayName("Debería coincidir con el tamaño de la lista de especialidades")
		void getNrOfSpecialties_ShouldMatchListSize() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setName("radiology");
			Specialty surgery = new Specialty();
			surgery.setName("surgery");

			vet.addSpecialty(radiology);
			vet.addSpecialty(surgery);
			// Act
			int nrOfSpecialties = vet.getNrOfSpecialties();
			int listSize = vet.getSpecialties().size();
			// Assert
			assertThat(nrOfSpecialties).isEqualTo(listSize);
		}

		@Test
		@DisplayName("Debería actualizar correctamente al agregar especialidades")
		void getNrOfSpecialties_ShouldUpdateWhenAddingSpecialties() {
			// Arrange
			Specialty radiology = new Specialty();
			radiology.setName("radiology");
			Specialty surgery = new Specialty();
			surgery.setName("surgery");
			// Act & Assert
			assertThat(vet.getNrOfSpecialties()).isZero();

			vet.addSpecialty(radiology);
			assertThat(vet.getNrOfSpecialties()).isEqualTo(1);

			vet.addSpecialty(surgery);
			assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
		}
	}

	@Nested
	@DisplayName("Comportamiento completo de Vet")
	class CompleteVetTests {

		@Test
		@DisplayName("Debería crear veterinario sin especialidades")
		void vet_ShouldCreateVetWithoutSpecialties() {
			// Arrange & Act
			Vet nuevoVet = new Vet();
			nuevoVet.setFirstName("Helen");
			nuevoVet.setLastName("Leary");
			// Assert
			assertThat(nuevoVet.getSpecialties()).isEmpty();
			assertThat(nuevoVet.getNrOfSpecialties()).isZero();
		}

		@Test
		@DisplayName("Debería crear veterinario especialista completo")
		void vet_ShouldCreateSpecialistVet() {
			// Arrange
			Vet especialista = new Vet();
			especialista.setId(3);
			especialista.setFirstName("Linda");
			especialista.setLastName("Douglas");

			Specialty radiology = new Specialty();
			radiology.setId(1);
			radiology.setName("radiology");

			Specialty surgery = new Specialty();
			surgery.setId(2);
			surgery.setName("surgery");
			// Act
			especialista.addSpecialty(radiology);
			especialista.addSpecialty(surgery);
			// Assert
			assertThat(especialista.getFirstName()).isEqualTo("Linda");
			assertThat(especialista.getLastName()).isEqualTo("Douglas");
			assertThat(especialista.getNrOfSpecialties()).isEqualTo(2);
			assertThat(especialista.getSpecialties()).hasSize(2);
			assertThat(especialista.getSpecialties()).extracting(Specialty::getName).containsExactly("radiology",
					"surgery");
		}
	}

}
