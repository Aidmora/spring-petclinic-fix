package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests de integración para PetTypeRepository.
 * Valida las operaciones sobre tipos de mascota.
 */
@DataJpaTest
@ActiveProfiles("test")
class PetTypeRepositoryTest {

    @Autowired
    private PetTypeRepository petTypeRepository;

    @Test
    @DisplayName("findPetTypes - Debería retornar todos los tipos de mascotas ordenados por nombre")
    void findPetTypes_ShouldReturnAllPetTypesSortedByName() {
        // Arrange & Act
        List<PetType> tipos = petTypeRepository.findPetTypes();
        // Assert
        assertThat(tipos).hasSize(6);
        assertThat(tipos)
                .extracting(PetType::getName)
                .containsExactly("bird", "cat", "dog", "hamster", "lizard", "snake");
    }

    @Test
    @DisplayName("findPetTypes - Debería retornar tipos con ID y nombre válidos")
    void findPetTypes_ShouldReturnTypesWithValidIdAndName() {
        // Arrange & Act
        List<PetType> tiposMascota = petTypeRepository.findPetTypes();
        // Assert
        assertThat(tiposMascota).allSatisfy(tipo -> {
            assertThat(tipo.getId()).isNotNull();
            assertThat(tipo.getId()).isPositive();
            assertThat(tipo.getName()).isNotBlank();
        });
    }

    // -- Búsqueda por ID --

    @Test
    @DisplayName("findById - Debería retornar el tipo 'cat' con ID 1")
    void findById_ShouldReturnCatType() {
        // Arrange & Act
        Integer idGato = 1;
        var tipoGato = petTypeRepository.findById(idGato);
        // Assert
        assertThat(tipoGato).isPresent();
        assertThat(tipoGato.get().getName()).isEqualTo("cat");
    }

    @Test
    @DisplayName("findById - Debería retornar el tipo 'dog' con ID 2")
    void findById_ShouldReturnDogType() {
        // Arrange
        Integer idPerro = 2;
        // Act
        var tipoPerro = petTypeRepository.findById(idPerro);
        // Assert
        assertThat(tipoPerro).isPresent();
        assertThat(tipoPerro.get().getName()).isEqualTo("dog");
    }

    @Test
    @DisplayName("findById - Debería retornar vacío cuando no existe el ID")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        Integer idFantasma = 999;
        // Act
        var noEncontrado = petTypeRepository.findById(idFantasma);
        // Assert
        assertThat(noEncontrado).isEmpty();
    }

    // -- Crear nuevo tipo --

    @Test
    @DisplayName("save - Debería guardar un nuevo tipo de mascota")
    void save_ShouldPersistNewPetType() {
        // Arrange
        PetType tipoNuevo = new PetType();
        tipoNuevo.setName("tortoise");
        // Act
        PetType guardado = petTypeRepository.save(tipoNuevo);
        // Assert
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getName()).isEqualTo("tortoise");

        List<PetType> todosTipos = petTypeRepository.findPetTypes();
        assertThat(todosTipos).hasSize(7);
        assertThat(todosTipos)
                .extracting(PetType::getName)
                .contains("tortoise");
    }

    // -- Actualizar tipo existente --

    @Test
    @DisplayName("save - Debería actualizar el nombre de un tipo existente")
    void save_ShouldUpdateExistingPetType() {
        // Arrange
        PetType tipoExistente = petTypeRepository.findById(1).orElseThrow();
        tipoExistente.setName("felino");
        // Act
        PetType modificado = petTypeRepository.save(tipoExistente);
        // Assert
        assertThat(modificado.getId()).isEqualTo(1);
        assertThat(modificado.getName()).isEqualTo("felino");

        var recargado = petTypeRepository.findById(1);
        assertThat(recargado).isPresent();
        assertThat(recargado.get().getName()).isEqualTo("felino");
    }

    // -- Eliminar tipo --

    @Test
    @DisplayName("delete - Debería eliminar un tipo de mascota")
    void delete_ShouldRemovePetType() {
        // Arrange
        PetType temporal = new PetType();
        temporal.setName("ferret");
        PetType persistido = petTypeRepository.save(temporal);
        Integer idTemporal = persistido.getId();
        // Act
        petTypeRepository.delete(persistido);
        var eliminado = petTypeRepository.findById(idTemporal);
        // Assert
        assertThat(eliminado).isEmpty();
    }

    // -- Conteo --

    @Test
    @DisplayName("count - Debería retornar el número correcto de tipos")
    void count_ShouldReturnCorrectNumberOfTypes() {
        // Arrange & Act
        long cantidad = petTypeRepository.count();
        // Assert
        assertThat(cantidad).isEqualTo(6);
    }

    // -- Existencia --

    @Test
    @DisplayName("existsById - Debería retornar true cuando existe el tipo")
    void existsById_ShouldReturnTrue_WhenTypeExists() {
        // Arrange
        Integer idExistente = 1;
        // Act
        boolean existe = petTypeRepository.existsById(idExistente);
        // Assert
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsById - Debería retornar false cuando no existe el tipo")
    void existsById_ShouldReturnFalse_WhenTypeDoesNotExist() {
        // Arrange
        Integer idInexistente = 999;
        // Act
        boolean existe = petTypeRepository.existsById(idInexistente);
        // Assert
        assertThat(existe).isFalse();
    }
}
