package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la entidad Visit.
 * Valida la lógica de negocio relacionada con la inicialización y gestión de visitas.
 */
class VisitTest {

	@Nested
	@DisplayName("Constructor - Inicialización de Visit")
	class ConstructorTests {

		@Test
		@DisplayName("Debería inicializar con fecha actual al crear nueva visita")
		void constructor_ShouldInitializeWithCurrentDate() {
			// Arrange
			LocalDate fechaEsperada = LocalDate.now();
			// Act
			Visit visita = new Visit();
			// Assert
			assertThat(visita.getDate()).isEqualTo(fechaEsperada);
		}

		@Test
		@DisplayName("Debería crear visita con ID null (isNew=true)")
		void constructor_ShouldCreateNewVisit() {
			// Act
			Visit visita = new Visit();
			// Assert
			assertThat(visita.isNew()).isTrue();
			assertThat(visita.getId()).isNull();
		}

		@Test
		@DisplayName("Debería crear visita con descripción null inicialmente")
		void constructor_ShouldCreateWithNullDescription() {
			// Act
			Visit visita = new Visit();
			// Assert
			assertThat(visita.getDescription()).isNull();
		}
	}

	@Nested
	@DisplayName("setDate y getDate - Gestión de fecha")
	class DateTests {

		@Test
		@DisplayName("setDate debería actualizar la fecha correctamente")
		void setDate_ShouldUpdateDate() {
			// Arrange
			Visit visita = new Visit();
			LocalDate nuevaFecha = LocalDate.of(2024, 6, 15);
			// Act
			visita.setDate(nuevaFecha);
			// Assert
			assertThat(visita.getDate()).isEqualTo(nuevaFecha);
		}

		@Test
		@DisplayName("setDate debería permitir establecer fecha pasada")
		void setDate_ShouldAllowPastDate() {
			// Arrange
			Visit visita = new Visit();
			LocalDate fechaPasada = LocalDate.of(2020, 1, 1);
			// Act
			visita.setDate(fechaPasada);
			// Assert
			assertThat(visita.getDate()).isEqualTo(fechaPasada);
		}

		@Test
		@DisplayName("setDate debería permitir establecer fecha futura")
		void setDate_ShouldAllowFutureDate() {
			// Arrange
			Visit visita = new Visit();
			LocalDate fechaFutura = LocalDate.of(2025, 12, 31);
			// Act
			visita.setDate(fechaFutura);
			// Assert
			assertThat(visita.getDate()).isEqualTo(fechaFutura);
		}

		@Test
		@DisplayName("setDate debería permitir null")
		void setDate_ShouldAllowNull() {
			// Arrange
			Visit visita = new Visit();
			// Act
			visita.setDate(null);
			// Assert
			assertThat(visita.getDate()).isNull();
		}

		@Test
		@DisplayName("getDate debería retornar la fecha establecida previamente")
		void getDate_ShouldReturnPreviouslySetDate() {
			// Arrange
			Visit visita = new Visit();
			LocalDate fecha1 = LocalDate.of(2024, 1, 15);
			LocalDate fecha2 = LocalDate.of(2024, 6, 20);
			// Act
			visita.setDate(fecha1);
			LocalDate primeraLectura = visita.getDate();
			visita.setDate(fecha2);
			LocalDate segundaLectura = visita.getDate();
			// Assert
			assertThat(primeraLectura).isEqualTo(fecha1);
			assertThat(segundaLectura).isEqualTo(fecha2);
		}
	}

	@Nested
	@DisplayName("setDescription y getDescription - Gestión de descripción")
	class DescriptionTests {

		@Test
		@DisplayName("setDescription debería establecer la descripción correctamente")
		void setDescription_ShouldSetDescription() {
			// Arrange
			Visit visita = new Visit();
			String descripcion = "Vacunación anual contra rabia";
			// Act
			visita.setDescription(descripcion);
			// Assert
			assertThat(visita.getDescription()).isEqualTo(descripcion);
		}

		@Test
		@DisplayName("setDescription debería permitir descripción vacía")
		void setDescription_ShouldAllowEmptyString() {
			// Arrange
			Visit visita = new Visit();
			// Act
			visita.setDescription("");
			// Assert
			assertThat(visita.getDescription()).isEmpty();
		}

		@Test
		@DisplayName("setDescription debería permitir descripción con espacios")
		void setDescription_ShouldAllowWhitespace() {
			// Arrange
			Visit visita = new Visit();
			// Act
			visita.setDescription("   ");
			// Assert
			assertThat(visita.getDescription()).isEqualTo("   ");
		}

		@Test
		@DisplayName("setDescription debería permitir descripción larga")
		void setDescription_ShouldAllowLongDescription() {
			// Arrange
			Visit visita = new Visit();
			String descripcionLarga = "Revisión general de salud incluyendo examen físico completo, "
					+ "análisis de sangre, vacunación contra rabia y parvovirus, "
					+ "desparasitación interna y externa, limpieza dental y recomendaciones nutricionales.";
			// Act
			visita.setDescription(descripcionLarga);
			// Assert
			assertThat(visita.getDescription()).isEqualTo(descripcionLarga);
			assertThat(visita.getDescription()).hasSizeGreaterThan(100);
		}

		@Test
		@DisplayName("setDescription debería permitir null")
		void setDescription_ShouldAllowNull() {
			// Arrange
			Visit visita = new Visit();
			visita.setDescription("Descripción inicial");
			// Act
			visita.setDescription(null);
			// Assert
			assertThat(visita.getDescription()).isNull();
		}

		@Test
		@DisplayName("setDescription debería permitir caracteres especiales")
		void setDescription_ShouldAllowSpecialCharacters() {
			// Arrange
			Visit visita = new Visit();
			String descripcionEspecial = "Revisión post-operatoria (cirugía). ¡Recuperación exitosa! @2024";
			// Act
			visita.setDescription(descripcionEspecial);
			// Assert
			assertThat(visita.getDescription()).isEqualTo(descripcionEspecial);
		}

		@Test
		@DisplayName("getDescription debería retornar la descripción establecida")
		void getDescription_ShouldReturnSetDescription() {
			// Arrange
			Visit visita = new Visit();
			String descripcion = "Control de peso";
			// Act
			visita.setDescription(descripcion);
			// Assert
			assertThat(visita.getDescription()).isEqualTo(descripcion);
		}
	}

	@Nested
	@DisplayName("Comportamiento completo de Visit")
	class CompleteVisitTests {

		@Test
		@DisplayName("Debería crear visita completa con todos los campos")
		void visit_ShouldCreateCompleteVisit() {
			// Arrange & Act
			Visit visita = new Visit();
			visita.setId(1);
			visita.setDate(LocalDate.of(2024, 5, 15));
			visita.setDescription("Vacunación anual");
			// Assert
			assertThat(visita.getId()).isEqualTo(1);
			assertThat(visita.getDate()).isEqualTo(LocalDate.of(2024, 5, 15));
			assertThat(visita.getDescription()).isEqualTo("Vacunación anual");
			assertThat(visita.isNew()).isFalse();
		}

		@Test
		@DisplayName("Debería permitir modificar todos los campos después de la creación")
		void visit_ShouldAllowFieldModification() {
			// Arrange
			Visit visita = new Visit();
			// Act
			visita.setId(5);
			visita.setDate(LocalDate.of(2024, 3, 10));
			visita.setDescription("Primera descripción");

			// Modificar campos
			visita.setDate(LocalDate.of(2024, 3, 15));
			visita.setDescription("Descripción actualizada");
			// Assert
			assertThat(visita.getDate()).isEqualTo(LocalDate.of(2024, 3, 15));
			assertThat(visita.getDescription()).isEqualTo("Descripción actualizada");
		}
	}

}
