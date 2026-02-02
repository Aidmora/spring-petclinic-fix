package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la entidad Owner.
 * Valida la lógica de negocio relacionada con la gestión de mascotas y visitas.
 */
class OwnerTest {

	private Owner owner;

	@BeforeEach
	void setUp() {
		owner = new Owner();
		owner.setId(1);
		owner.setFirstName("John");
		owner.setLastName("Doe");
		owner.setAddress("123 Main St");
		owner.setCity("Springfield");
		owner.setTelephone("1234567890");
	}

	@Nested
	@DisplayName("addPet() - Agregar mascotas")
	class AddPetTests {

		@Test
		@DisplayName("Debería agregar mascota nueva correctamente")
		void addPet_ShouldAddPet_WhenPetIsNew() {
			// Arrange
			Pet nuevaMascota = new Pet();
			nuevaMascota.setName("Max");
			nuevaMascota.setBirthDate(LocalDate.of(2020, 5, 15));
			// Act
			owner.addPet(nuevaMascota);
			// Assert
			assertThat(owner.getPets()).hasSize(1);
			assertThat(owner.getPets()).contains(nuevaMascota);
			assertThat(owner.getPets().get(0).getName()).isEqualTo("Max");
		}

		@Test
		@DisplayName("No debería agregar mascota que ya tiene ID (no es nueva)")
		void addPet_ShouldNotAddPet_WhenPetIsNotNew() {
			// Arrange
			Pet mascotaExistente = new Pet();
			mascotaExistente.setId(10);
			mascotaExistente.setName("Bella");
			// Act
			owner.addPet(mascotaExistente);
			// Assert
			assertThat(owner.getPets()).isEmpty();
		}

		@Test
		@DisplayName("Debería agregar múltiples mascotas nuevas")
		void addPet_ShouldAddMultiplePets_WhenAllAreNew() {
			// Arrange
			Pet mascota1 = new Pet();
			mascota1.setName("Max");
			Pet mascota2 = new Pet();
			mascota2.setName("Bella");
			Pet mascota3 = new Pet();
			mascota3.setName("Charlie");
			// Act
			owner.addPet(mascota1);
			owner.addPet(mascota2);
			owner.addPet(mascota3);
			// Assert
			assertThat(owner.getPets()).hasSize(3);
			assertThat(owner.getPets()).extracting(Pet::getName).containsExactly("Max", "Bella", "Charlie");
		}
	}

	@Nested
	@DisplayName("getPet(String name) - Buscar mascota por nombre")
	class GetPetByNameTests {

		@Test
		@DisplayName("Debería retornar mascota cuando el nombre coincide exactamente")
		void getPet_ShouldReturnPet_WhenNameMatches() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setName("Max");
			owner.addPet(mascota);
			// Act
			Pet encontrada = owner.getPet("Max");
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getName()).isEqualTo("Max");
		}

		@Test
		@DisplayName("Debería retornar mascota ignorando mayúsculas/minúsculas")
		void getPet_ShouldReturnPet_WhenNameMatchesIgnoreCase() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setName("Max");
			owner.addPet(mascota);
			// Act
			Pet encontrada = owner.getPet("max");
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getName()).isEqualTo("Max");
		}

		@Test
		@DisplayName("Debería retornar null cuando el nombre no existe")
		void getPet_ShouldReturnNull_WhenNameDoesNotExist() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setName("Max");
			owner.addPet(mascota);
			// Act
			Pet noEncontrada = owner.getPet("Bella");
			// Assert
			assertThat(noEncontrada).isNull();
		}

		@Test
		@DisplayName("Debería retornar null cuando no hay mascotas")
		void getPet_ShouldReturnNull_WhenNoPets() {
			// Act
			Pet noEncontrada = owner.getPet("Max");
			// Assert
			assertThat(noEncontrada).isNull();
		}

		@Test
		@DisplayName("Debería retornar mascota nueva por defecto (ignoreNew=false)")
		void getPet_ShouldReturnNewPet_ByDefault() {
			// Arrange
			Pet mascotaNueva = new Pet();
			mascotaNueva.setName("Max");
			owner.addPet(mascotaNueva);
			// Act
			Pet encontrada = owner.getPet("Max");
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.isNew()).isTrue();
		}
	}

	@Nested
	@DisplayName("getPet(Integer id) - Buscar mascota por ID")
	class GetPetByIdTests {

		@Test
		@DisplayName("Debería retornar mascota cuando el ID coincide")
		void getPet_ShouldReturnPet_WhenIdMatches() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setId(5);
			mascota.setName("Max");
			owner.getPets().add(mascota); // Agregar directamente sin validación isNew
			// Act
			Pet encontrada = owner.getPet(5);
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getId()).isEqualTo(5);
			assertThat(encontrada.getName()).isEqualTo("Max");
		}

		@Test
		@DisplayName("Debería retornar null cuando el ID no existe")
		void getPet_ShouldReturnNull_WhenIdDoesNotExist() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setId(5);
			mascota.setName("Max");
			owner.getPets().add(mascota);
			// Act
			Pet noEncontrada = owner.getPet(10);
			// Assert
			assertThat(noEncontrada).isNull();
		}

		@Test
		@DisplayName("Debería retornar null cuando la mascota es nueva (sin ID)")
		void getPet_ShouldReturnNull_WhenPetIsNew() {
			// Arrange
			Pet mascotaNueva = new Pet();
			mascotaNueva.setName("Max");
			owner.addPet(mascotaNueva);
			// Act
			Pet noEncontrada = owner.getPet(1);
			// Assert
			assertThat(noEncontrada).isNull();
		}

		@Test
		@DisplayName("Debería retornar null cuando no hay mascotas")
		void getPet_ShouldReturnNull_WhenNoPets() {
			// Act
			Pet noEncontrada = owner.getPet(1);
			// Assert
			assertThat(noEncontrada).isNull();
		}
	}

	@Nested
	@DisplayName("getPet(String name, boolean ignoreNew) - Buscar con opción ignoreNew")
	class GetPetByNameIgnoreNewTests {

		@Test
		@DisplayName("Debería retornar mascota nueva cuando ignoreNew=false")
		void getPet_ShouldReturnNewPet_WhenIgnoreNewIsFalse() {
			// Arrange
			Pet mascotaNueva = new Pet();
			mascotaNueva.setName("Max");
			owner.addPet(mascotaNueva);
			// Act
			Pet encontrada = owner.getPet("Max", false);
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getName()).isEqualTo("Max");
			assertThat(encontrada.isNew()).isTrue();
		}

		@Test
		@DisplayName("No debería retornar mascota nueva cuando ignoreNew=true")
		void getPet_ShouldNotReturnNewPet_WhenIgnoreNewIsTrue() {
			// Arrange
			Pet mascotaNueva = new Pet();
			mascotaNueva.setName("Max");
			owner.addPet(mascotaNueva);
			// Act
			Pet noEncontrada = owner.getPet("Max", true);
			// Assert
			assertThat(noEncontrada).isNull();
		}

		@Test
		@DisplayName("Debería retornar mascota existente cuando ignoreNew=true")
		void getPet_ShouldReturnExistingPet_WhenIgnoreNewIsTrue() {
			// Arrange
			Pet mascotaExistente = new Pet();
			mascotaExistente.setId(5);
			mascotaExistente.setName("Max");
			owner.getPets().add(mascotaExistente);
			// Act
			Pet encontrada = owner.getPet("Max", true);
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getName()).isEqualTo("Max");
			assertThat(encontrada.isNew()).isFalse();
		}

		@Test
		@DisplayName("Debería retornar mascota existente cuando ignoreNew=false")
		void getPet_ShouldReturnExistingPet_WhenIgnoreNewIsFalse() {
			// Arrange
			Pet mascotaExistente = new Pet();
			mascotaExistente.setId(5);
			mascotaExistente.setName("Max");
			owner.getPets().add(mascotaExistente);
			// Act
			Pet encontrada = owner.getPet("Max", false);
			// Assert
			assertThat(encontrada).isNotNull();
			assertThat(encontrada.getName()).isEqualTo("Max");
			assertThat(encontrada.isNew()).isFalse();
		}
	}

	@Nested
	@DisplayName("addVisit() - Agregar visita a mascota")
	class AddVisitTests {

		@Test
		@DisplayName("Debería agregar visita correctamente cuando mascota existe")
		void addVisit_ShouldAddVisit_WhenPetExists() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setId(5);
			mascota.setName("Max");
			owner.getPets().add(mascota);

			Visit visita = new Visit();
			visita.setDescription("Vacunación anual");
			// Act
			owner.addVisit(5, visita);
			// Assert
			assertThat(mascota.getVisits()).hasSize(1);
			assertThat(mascota.getVisits()).contains(visita);
		}

		@Test
		@DisplayName("Debería lanzar excepción cuando petId es null")
		void addVisit_ShouldThrowException_WhenPetIdIsNull() {
			// Arrange
			Visit visita = new Visit();
			visita.setDescription("Consulta general");
			// Act & Assert
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> owner.addVisit(null, visita));
			assertThat(exception.getMessage()).contains("Pet identifier must not be null");
		}

		@Test
		@DisplayName("Debería lanzar excepción cuando visit es null")
		void addVisit_ShouldThrowException_WhenVisitIsNull() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setId(5);
			owner.getPets().add(mascota);
			// Act & Assert
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> owner.addVisit(5, null));
			assertThat(exception.getMessage()).contains("Visit must not be null");
		}

		@Test
		@DisplayName("Debería lanzar excepción cuando mascota no existe")
		void addVisit_ShouldThrowException_WhenPetDoesNotExist() {
			// Arrange
			Visit visita = new Visit();
			visita.setDescription("Consulta");
			// Act & Assert
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> owner.addVisit(999, visita));
			assertThat(exception.getMessage()).contains("Invalid Pet identifier");
		}

		@Test
		@DisplayName("Debería agregar múltiples visitas a la misma mascota")
		void addVisit_ShouldAddMultipleVisits_ToSamePet() {
			// Arrange
			Pet mascota = new Pet();
			mascota.setId(5);
			mascota.setName("Max");
			owner.getPets().add(mascota);

			Visit visita1 = new Visit();
			visita1.setDescription("Vacunación");
			Visit visita2 = new Visit();
			visita2.setDescription("Revisión general");
			Visit visita3 = new Visit();
			visita3.setDescription("Control de peso");
			// Act
			owner.addVisit(5, visita1);
			owner.addVisit(5, visita2);
			owner.addVisit(5, visita3);
			// Assert
			assertThat(mascota.getVisits()).hasSize(3);
			assertThat(mascota.getVisits()).extracting(Visit::getDescription).containsExactly("Vacunación",
					"Revisión general", "Control de peso");
		}
	}

	@Nested
	@DisplayName("getPets() - Obtener lista de mascotas")
	class GetPetsTests {

		@Test
		@DisplayName("Debería retornar lista vacía cuando no hay mascotas")
		void getPets_ShouldReturnEmptyList_WhenNoPets() {
			// Act & Assert
			assertThat(owner.getPets()).isEmpty();
		}

		@Test
		@DisplayName("Debería retornar lista con todas las mascotas agregadas")
		void getPets_ShouldReturnAllPets() {
			// Arrange
			Pet mascota1 = new Pet();
			mascota1.setName("Max");
			Pet mascota2 = new Pet();
			mascota2.setName("Bella");
			owner.addPet(mascota1);
			owner.addPet(mascota2);
			// Act & Assert
			assertThat(owner.getPets()).hasSize(2);
			assertThat(owner.getPets()).containsExactly(mascota1, mascota2);
		}
	}

}
