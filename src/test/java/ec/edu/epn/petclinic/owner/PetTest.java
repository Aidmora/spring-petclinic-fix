package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la entidad Pet.
 * Valida la lógica de negocio relacionada con la gestión de visitas.
 */
class PetTest {

	private Pet pet;

	@BeforeEach
	void setUp() {
		pet = new Pet();
		pet.setId(1);
		pet.setName("Max");
		pet.setBirthDate(LocalDate.of(2020, 5, 15));

		PetType tipo = new PetType();
		tipo.setId(1);
		tipo.setName("dog");
		pet.setType(tipo);
	}

	@Nested
	@DisplayName("addVisit() - Agregar visitas")
	class AddVisitTests {

		@Test
		@DisplayName("Debería agregar visita correctamente")
		void addVisit_ShouldAddVisit() {
			// Arrange
			Visit visita = new Visit();
			visita.setDescription("Vacunación anual");
			visita.setDate(LocalDate.of(2024, 1, 15));
			// Act
			pet.addVisit(visita);
			// Assert
			assertThat(pet.getVisits()).hasSize(1);
			assertThat(pet.getVisits()).contains(visita);
		}

		@Test
		@DisplayName("Debería agregar múltiples visitas")
		void addVisit_ShouldAddMultipleVisits() {
			// Arrange
			Visit visita1 = new Visit();
			visita1.setDescription("Vacunación");
			visita1.setDate(LocalDate.of(2024, 1, 15));

			Visit visita2 = new Visit();
			visita2.setDescription("Revisión general");
			visita2.setDate(LocalDate.of(2024, 3, 20));

			Visit visita3 = new Visit();
			visita3.setDescription("Control de peso");
			visita3.setDate(LocalDate.of(2024, 6, 10));
			// Act
			pet.addVisit(visita1);
			pet.addVisit(visita2);
			pet.addVisit(visita3);
			// Assert
			assertThat(pet.getVisits()).hasSize(3);
			assertThat(pet.getVisits()).containsExactly(visita1, visita2, visita3);
		}

		@Test
		@DisplayName("Debería mantener el orden de inserción de visitas")
		void addVisit_ShouldMaintainInsertionOrder() {
			// Arrange
			Visit visita1 = new Visit();
			visita1.setId(1);
			visita1.setDescription("Primera visita");

			Visit visita2 = new Visit();
			visita2.setId(2);
			visita2.setDescription("Segunda visita");

			Visit visita3 = new Visit();
			visita3.setId(3);
			visita3.setDescription("Tercera visita");
			// Act
			pet.addVisit(visita1);
			pet.addVisit(visita2);
			pet.addVisit(visita3);
			// Assert
			assertThat(pet.getVisits()).containsExactly(visita1, visita2, visita3);
		}

		@Test
		@DisplayName("No debería permitir visitas duplicadas (Set behavior)")
		void addVisit_ShouldNotAllowDuplicates() {
			// Arrange
			Visit visita = new Visit();
			visita.setId(1);
			visita.setDescription("Vacunación");
			// Act
			pet.addVisit(visita);
			pet.addVisit(visita); // Intentar agregar la misma visita nuevamente
			// Assert
			assertThat(pet.getVisits()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("getVisits() - Obtener colección de visitas")
	class GetVisitsTests {

		@Test
		@DisplayName("Debería retornar colección vacía cuando no hay visitas")
		void getVisits_ShouldReturnEmptyCollection_WhenNoVisits() {
			// Act & Assert
			assertThat(pet.getVisits()).isEmpty();
		}

		@Test
		@DisplayName("Debería retornar todas las visitas agregadas")
		void getVisits_ShouldReturnAllVisits() {
			// Arrange
			Visit visita1 = new Visit();
			visita1.setDescription("Vacunación");
			Visit visita2 = new Visit();
			visita2.setDescription("Revisión");
			pet.addVisit(visita1);
			pet.addVisit(visita2);
			// Act & Assert
			assertThat(pet.getVisits()).hasSize(2);
			assertThat(pet.getVisits()).contains(visita1, visita2);
		}

		@Test
		@DisplayName("Debería retornar colección no nula al crear nueva mascota")
		void getVisits_ShouldReturnNonNullCollection_ForNewPet() {
			// Arrange
			Pet nuevaMascota = new Pet();
			// Act & Assert
			assertThat(nuevaMascota.getVisits()).isNotNull();
			assertThat(nuevaMascota.getVisits()).isEmpty();
		}
	}

	@Nested
	@DisplayName("Getters y Setters - Propiedades básicas")
	class GettersSettersTests {

		@Test
		@DisplayName("setBirthDate y getBirthDate - Deberían funcionar correctamente")
		void birthDate_ShouldWorkCorrectly() {
			// Arrange
			LocalDate fechaNacimiento = LocalDate.of(2021, 8, 20);
			// Act
			pet.setBirthDate(fechaNacimiento);
			// Assert
			assertThat(pet.getBirthDate()).isEqualTo(fechaNacimiento);
		}

		@Test
		@DisplayName("setType y getType - Deberían funcionar correctamente")
		void type_ShouldWorkCorrectly() {
			// Arrange
			PetType tipoCat = new PetType();
			tipoCat.setId(2);
			tipoCat.setName("cat");
			// Act
			pet.setType(tipoCat);
			// Assert
			assertThat(pet.getType()).isEqualTo(tipoCat);
			assertThat(pet.getType().getName()).isEqualTo("cat");
		}

		@Test
		@DisplayName("setBirthDate con null debería permitirse")
		void setBirthDate_ShouldAllowNull() {
			// Act
			pet.setBirthDate(null);
			// Assert
			assertThat(pet.getBirthDate()).isNull();
		}
	}

}
