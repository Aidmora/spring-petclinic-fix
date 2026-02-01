package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas de integración para VetRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    // Tests para findAll()
    @Test
    @DisplayName("findAll - Debería retornar todos los veterinarios")
    void findAll_ShouldReturnAllVets() {
        // ARRANGE - Datos en (data.sql)
        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        assertThat(result).hasSize(6);
    }

    @Test
    @DisplayName("findAll - Debería retornar veterinarios con datos válidos")
    void findAll_ShouldReturnVetsWithValidData() {
        // ARRANGE - Datos en (data.sql)

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        assertThat(result).allSatisfy(vet -> {
            assertThat(vet.getId()).isNotNull();
            assertThat(vet.getId()).isPositive();
            assertThat(vet.getFirstName()).isNotBlank();
            assertThat(vet.getLastName()).isNotBlank();
        });
    }

    @Test
    @DisplayName("findAll - Debería incluir a James Carter (primer veterinario)")
    void findAll_ShouldContainJamesCarter() {
        // ARRANGE - Datos en (data.sql)

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        assertThat(result)
                .extracting(Vet::getFirstName)
                .contains("James");

        assertThat(result)
                .extracting(Vet::getLastName)
                .contains("Carter");
    }

    // Tests para findAll - Paginado

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar primera página correctamente")
    void findAllPageable_ShouldReturnFirstPageCorrectly() {
        // ARRANGE
        Pageable firstPage = PageRequest.of(0, 3);

        // ACT
        Page<Vet> result = vetRepository.findAll(firstPage);

        // ASSERT
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar segunda página correctamente")
    void findAllPageable_ShouldReturnSecondPageCorrectly() {
        // ARRANGE
        Pageable secondPage = PageRequest.of(1, 3);

        // ACT
        Page<Vet> result = vetRepository.findAll(secondPage);

        // ASSERT
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar página vacía si el offset excede el total")
    void findAllPageable_ShouldReturnEmptyPage_WhenOffsetExceedsTotal() {
        // ARRANGE
        Pageable outOfRangePage = PageRequest.of(10, 5);

        // ACT
        Page<Vet> result = vetRepository.findAll(outOfRangePage);

        // ASSERT
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(6); // Total sigue siendo 6
    }

    // Tests para relación Vet-Specialty
    @Test
    @DisplayName("findAll - Debería cargar especialidades de Helen Leary (radiology)")
    void findAll_ShouldLoadSpecialtiesForHelenLeary() {
        // ARRANGE
        // Helen Leary

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        Vet helenLeary = result.stream()
                .filter(v -> v.getFirstName().equals("Helen") && v.getLastName().equals("Leary"))
                .findFirst()
                .orElseThrow();

        assertThat(helenLeary.getNrOfSpecialties()).isEqualTo(1);
        assertThat(helenLeary.getSpecialties())
                .extracting(Specialty::getName)
                .containsExactly("radiology");
    }

    @Test
    @DisplayName("findAll - Debería cargar múltiples especialidades de Linda Douglas")
    void findAll_ShouldLoadMultipleSpecialtiesForLindaDouglas() {
        // ARRANGE
        // Linda Douglas

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        Vet lindaDouglas = result.stream()
                .filter(v -> v.getFirstName().equals("Linda") && v.getLastName().equals("Douglas"))
                .findFirst()
                .orElseThrow();

        assertThat(lindaDouglas.getNrOfSpecialties()).isEqualTo(2);
        assertThat(lindaDouglas.getSpecialties())
                .extracting(Specialty::getName)
                .containsExactlyInAnyOrder("surgery", "dentistry");
    }

    @Test
    @DisplayName("findAll - James Carter no tiene especialidades")
    void findAll_JamesCarterShouldHaveNoSpecialties() {
        // ARRANGE
        // James Carter

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        Vet jamesCarter = result.stream()
                .filter(v -> v.getFirstName().equals("James") && v.getLastName().equals("Carter"))
                .findFirst()
                .orElseThrow();

        assertThat(jamesCarter.getNrOfSpecialties()).isZero();
        assertThat(jamesCarter.getSpecialties()).isEmpty();
    }

    @Test
    @DisplayName("findAll - Sharon Jenkins no tiene especialidades")
    void findAll_SharonJenkinsShouldHaveNoSpecialties() {
        // ARRANGE
        // Sharon Jenkins

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        Vet sharonJenkins = result.stream()
                .filter(v -> v.getFirstName().equals("Sharon") && v.getLastName().equals("Jenkins"))
                .findFirst()
                .orElseThrow();

        assertThat(sharonJenkins.getNrOfSpecialties()).isZero();
    }

    // Tests para verificar especialidades ordenadas

    @Test
    @DisplayName("getSpecialties - Debería retornar especialidades ordenadas alfabéticamente")
    void getSpecialties_ShouldReturnSpecialtiesSortedByName() {
        // ARRANGE
        // Linda Douglas

        // ACT
        Collection<Vet> result = vetRepository.findAll();
        Vet lindaDouglas = result.stream()
                .filter(v -> v.getFirstName().equals("Linda"))
                .findFirst()
                .orElseThrow();

        List<Specialty> specialties = lindaDouglas.getSpecialties();

        // ASSERT
        assertThat(specialties)
                .extracting(Specialty::getName)
                .containsExactly("dentistry", "surgery");
    }

    // Tests para contar veterinarios con/sin especialidades
    @Test
    @DisplayName("findAll - Debería tener veterinarios con y sin especialidades")
    void findAll_ShouldHaveVetsWithAndWithoutSpecialties() {
        // ARRANGE - Datos en (data.sql)

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        long vetsWithSpecialties = result.stream()
                .filter(v -> v.getNrOfSpecialties() > 0)
                .count();

        long vetsWithoutSpecialties = result.stream()
                .filter(v -> v.getNrOfSpecialties() == 0)
                .count();
        assertThat(vetsWithSpecialties).isEqualTo(4);
        assertThat(vetsWithoutSpecialties).isEqualTo(2);
    }

    // Tests de integridad de datos
    @Test
    @DisplayName("findAll - Todos los veterinarios deberían tener ID único")
    void findAll_AllVetsShouldHaveUniqueIds() {
        // ARRANGE - Datos en (data.sql)

        // ACT
        Collection<Vet> result = vetRepository.findAll();

        // ASSERT
        List<Integer> ids = result.stream()
                .map(Vet::getId)
                .toList();

        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("findAll(Pageable) - Paginación con tamaño 5 (como en el controlador)")
    void findAllPageable_ShouldWorkWithPageSizeFive() {
        // ARRANGE
        int pageSize = 5;
        Pageable pageable = PageRequest.of(0, pageSize);

        // ACT
        Page<Vet> result = vetRepository.findAll(pageable);

        // ASSERT
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}
