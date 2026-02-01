package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas de integración para PetTypeRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class PetTypeRepositoryTest {

    @Autowired
    private PetTypeRepository petTypeRepository;

    // Tests para findPetTypes()
    @Test
    @DisplayName("findPetTypes - Debería retornar todos los tipos de mascotas ordenados por nombre")
    void findPetTypes_ShouldReturnAllPetTypesSortedByName() {
        // ACT
        List<PetType> result = petTypeRepository.findPetTypes();

        // ASSERT
        assertThat(result).hasSize(6);
        assertThat(result)
                .extracting(PetType::getName)
                .containsExactly("bird", "cat", "dog", "hamster", "lizard", "snake");
    }

    @Test
    @DisplayName("findPetTypes - Debería retornar tipos con ID y nombre válidos")
    void findPetTypes_ShouldReturnTypesWithValidIdAndName() {
        // ACT
        List<PetType> result = petTypeRepository.findPetTypes();

        // ASSERT
        assertThat(result).allSatisfy(petType -> {
            assertThat(petType.getId()).isNotNull();
            assertThat(petType.getId()).isPositive();
            assertThat(petType.getName()).isNotBlank();
        });
    }
    // Tests para findById()

    @Test
    @DisplayName("findById - Debería retornar el tipo 'cat' con ID 1")
    void findById_ShouldReturnCatType() {
        // ARRANGE
        Integer catTypeId = 1;

        // ACT
        var result = petTypeRepository.findById(catTypeId);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("cat");
    }

    @Test
    @DisplayName("findById - Debería retornar el tipo 'dog' con ID 2")
    void findById_ShouldReturnDogType() {
        // ARRANGE
        Integer dogTypeId = 2;

        // ACT
        var result = petTypeRepository.findById(dogTypeId);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("dog");
    }

    @Test
    @DisplayName("findById - Debería retornar vacío cuando no existe el ID")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // ARRANGE
        Integer nonExistentId = 999;

        // ACT
        var result = petTypeRepository.findById(nonExistentId);

        // ASSERT
        assertThat(result).isEmpty();
    }

    // Tests para Crear nuevo PetType

    @Test
    @DisplayName("save - Debería guardar un nuevo tipo de mascota")
    void save_ShouldPersistNewPetType() {
        // ARRANGE
        PetType newType = new PetType();
        newType.setName("rabbit");

        // ACT
        PetType savedType = petTypeRepository.save(newType);

        // ASSERT
        assertThat(savedType.getId()).isNotNull();
        assertThat(savedType.getName()).isEqualTo("rabbit");

        // Verificar que aparece en la lista
        List<PetType> allTypes = petTypeRepository.findPetTypes();
        assertThat(allTypes).hasSize(7);
        assertThat(allTypes)
                .extracting(PetType::getName)
                .contains("rabbit");
    }
    // Tests para Actualizar PetType existente

    @Test
    @DisplayName("save - Debería actualizar el nombre de un tipo existente")
    void save_ShouldUpdateExistingPetType() {
        // ARRANGE
        PetType existingType = petTypeRepository.findById(1).orElseThrow();
        existingType.setName("feline");

        // ACT
        PetType updatedType = petTypeRepository.save(existingType);

        // ASSERT
        assertThat(updatedType.getId()).isEqualTo(1);
        assertThat(updatedType.getName()).isEqualTo("feline");

        // Verificar que el cambio persiste
        var reloaded = petTypeRepository.findById(1);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getName()).isEqualTo("feline");
    }

    // Tests para delete

    @Test
    @DisplayName("delete - Debería eliminar un tipo de mascota")
    void delete_ShouldRemovePetType() {
        // ARRANGE
        PetType newType = new PetType();
        newType.setName("turtle");
        PetType savedType = petTypeRepository.save(newType);
        Integer savedId = savedType.getId();

        // ACT
        petTypeRepository.delete(savedType);

        // ASSERT
        var result = petTypeRepository.findById(savedId);
        assertThat(result).isEmpty();
    }
    // Tests para count()

    @Test
    @DisplayName("count - Debería retornar el número correcto de tipos")
    void count_ShouldReturnCorrectNumberOfTypes() {
        // ACT
        long count = petTypeRepository.count();

        // ASSERT
        assertThat(count).isEqualTo(6);
    }
    // Tests para existsById()

    @Test
    @DisplayName("existsById - Debería retornar true cuando existe el tipo")
    void existsById_ShouldReturnTrue_WhenTypeExists() {
        // ARRANGE
        Integer existingId = 1;

        // ACT
        boolean exists = petTypeRepository.existsById(existingId);

        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - Debería retornar false cuando no existe el tipo")
    void existsById_ShouldReturnFalse_WhenTypeDoesNotExist() {
        // ARRANGE
        Integer nonExistentId = 999;

        // ACT
        boolean exists = petTypeRepository.existsById(nonExistentId);

        // ASSERT
        assertThat(exists).isFalse();
    }
}
