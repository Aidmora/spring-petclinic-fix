package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas de integración para OwnerRepository.
 * Usa @DataJpaTest que configura una base de datos H2 en memoria
 * y carga automáticamente el schema.sql y data.sql.
 */
@DataJpaTest
@ActiveProfiles("test")
class OwnerRepositoryTest {
    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    @DisplayName("findById - Debería retornar Owner cuando existe el ID")
    void findById_ShouldReturnOwner_WhenOwnerExists() {
        // ARRANGE
        Integer ownerId = 1;

        // ACT
        Optional<Owner> result = ownerRepository.findById(ownerId);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("George");
        assertThat(result.get().getLastName()).isEqualTo("Franklin");
        assertThat(result.get().getAddress()).isEqualTo("110 W. Liberty St.");
        assertThat(result.get().getCity()).isEqualTo("Madison");
        assertThat(result.get().getTelephone()).isEqualTo("6085551023");
    }

    @Test
    @DisplayName("findById - Debería retornar vacío cuando no existe el ID")
    void findById_ShouldReturnEmpty_WhenOwnerDoesNotExist() {
        // ARRANGE
        Integer nonExistentId = 999;

        // ACT
        Optional<Owner> result = ownerRepository.findById(nonExistentId);

        // ASSERT
        assertThat(result).isEmpty();
    }

    // Tests para findByLastNameStartingWith()
    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar owners que empiecen con 'Davis'")
    void findByLastNameStartingWith_ShouldReturnOwners_WhenLastNameMatches() {
        // ARRANGE
        String lastName = "Davis";
        Pageable pageable = PageRequest.of(0, 10);

        // ACT
        Page<Owner> result = ownerRepository.findByLastNameStartingWith(lastName, pageable);

        // ASSERT
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Owner::getLastName)
                .containsOnly("Davis");
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar owners con apellido parcial 'Dav'")
    void findByLastNameStartingWith_ShouldReturnOwners_WhenPartialLastNameMatches() {
        // ARRANGE
        String partialLastName = "Dav";
        Pageable pageable = PageRequest.of(0, 10);

        // ACT
        Page<Owner> result = ownerRepository.findByLastNameStartingWith(partialLastName, pageable);

        // ASSERT
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Owner::getLastName)
                .allMatch(name -> name.startsWith("Dav"));
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar lista vacía cuando no hay coincidencias")
    void findByLastNameStartingWith_ShouldReturnEmpty_WhenNoMatch() {
        // ARRANGE
        String nonExistentLastName = "XYZ";
        Pageable pageable = PageRequest.of(0, 10);

        // ACT
        Page<Owner> result = ownerRepository.findByLastNameStartingWith(nonExistentLastName, pageable);

        // ASSERT
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar todos los owners con string vacío")
    void findByLastNameStartingWith_ShouldReturnAllOwners_WhenEmptyString() {
        // ARRANGE
        String emptyLastName = "";
        Pageable pageable = PageRequest.of(0, 20);

        // ACT
        Page<Owner> result = ownerRepository.findByLastNameStartingWith(emptyLastName, pageable);

        // ASSERT
        assertThat(result.getContent()).hasSize(10); // Hay 10 owners en data.sql
    }

    // Tests para paginación
    @Test
    @DisplayName("findByLastNameStartingWith - Debería paginar correctamente")
    void findByLastNameStartingWith_ShouldPaginateCorrectly() {
        // ARRANGE
        String emptyLastName = "";
        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // ACT
        Page<Owner> firstPageResult = ownerRepository.findByLastNameStartingWith(emptyLastName, firstPage);
        Page<Owner> secondPageResult = ownerRepository.findByLastNameStartingWith(emptyLastName, secondPage);

        // ASSERT
        assertThat(firstPageResult.getContent()).hasSize(5);
        assertThat(secondPageResult.getContent()).hasSize(5);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(10);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(2);
    }

    // Tests para Crear nuevo Owner

    @Test
    @DisplayName("save - Debería guardar un nuevo Owner correctamente")
    void save_ShouldPersistNewOwner() {
        // ARRANGE
        Owner newOwner = new Owner();
        newOwner.setFirstName("John");
        newOwner.setLastName("Doe");
        newOwner.setAddress("123 Main St.");
        newOwner.setCity("Springfield");
        newOwner.setTelephone("1234567890");

        // ACT
        Owner savedOwner = ownerRepository.save(newOwner);

        // ASSERT
        assertThat(savedOwner.getId()).isNotNull();
        assertThat(savedOwner.getFirstName()).isEqualTo("John");
        assertThat(savedOwner.getLastName()).isEqualTo("Doe");
        Optional<Owner> foundOwner = ownerRepository.findById(savedOwner.getId());
        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get().getFirstName()).isEqualTo("John");
    }
    // Tests para Actualizar Owner existente

    @Test
    @DisplayName("save - Debería actualizar un Owner existente")
    void save_ShouldUpdateExistingOwner() {
        // ARRANGE
        Owner existingOwner = ownerRepository.findById(1).orElseThrow();
        String originalFirstName = existingOwner.getFirstName();
        existingOwner.setFirstName("UpdatedName");

        // ACT
        Owner updatedOwner = ownerRepository.save(existingOwner);

        // ASSERT
        assertThat(updatedOwner.getId()).isEqualTo(1);
        assertThat(updatedOwner.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updatedOwner.getFirstName()).isNotEqualTo(originalFirstName);
    }

    // Tests para relación Owner-Pet

    @Test
    @DisplayName("findById - Debería cargar las mascotas del Owner (EAGER loading)")
    void findById_ShouldLoadPetsWithOwner() {
        // ARRANGE
        Integer ownerIdWithPets = 6;

        // ACT
        Optional<Owner> result = ownerRepository.findById(ownerIdWithPets);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getPets()).hasSize(2);
        assertThat(result.get().getPets())
                .extracting(Pet::getName)
                .containsExactlyInAnyOrder("Samantha", "Max");
    }

    @Test
    @DisplayName("findById - Debería retornar Owner sin mascotas si no tiene")
    void findById_ShouldReturnOwnerWithEmptyPets_WhenNoPets() {
        // ARRANGE
        Owner ownerWithoutPets = new Owner();
        ownerWithoutPets.setFirstName("Ariel");
        ownerWithoutPets.setLastName("Mora");
        ownerWithoutPets.setAddress("Inti Oe2");
        ownerWithoutPets.setCity("Quito");
        ownerWithoutPets.setTelephone("0995468359");
        Owner saved = ownerRepository.save(ownerWithoutPets);

        // ACT
        Optional<Owner> result = ownerRepository.findById(saved.getId());

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getPets()).isEmpty();
    }
}
