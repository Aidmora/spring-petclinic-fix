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
 * Tests de integración - VetRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    @Test
    @DisplayName("findAll - Debería retornar todos los veterinarios")
    void findAll_ShouldReturnAllVets() {
        Collection<Vet> veterinarios = vetRepository.findAll();
        assertThat(veterinarios).hasSize(6);
    }

    @Test
    @DisplayName("findAll - Debería retornar veterinarios con datos válidos")
    void findAll_ShouldReturnVetsWithValidData() {
        // Arrange & Act
        Collection<Vet> todosLosVets = vetRepository.findAll();
        // Assert
        assertThat(todosLosVets).allSatisfy(v -> {
            assertThat(v.getId()).isNotNull();
            assertThat(v.getId()).isPositive();
            assertThat(v.getFirstName()).isNotBlank();
            assertThat(v.getLastName()).isNotBlank();
        });
    }

    @Test
    @DisplayName("findAll - Debería incluir a James Carter (primer veterinario)")
    void findAll_ShouldContainJamesCarter() {
        // Arrange & Act
        Collection<Vet> listaVets = vetRepository.findAll();
        // Assert
        assertThat(listaVets)
                .extracting(Vet::getFirstName)
                .contains("James");
        assertThat(listaVets)
                .extracting(Vet::getLastName)
                .contains("Carter");
    }
    // Pruebas de paginación

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar primera página correctamente")
    void findAllPageable_ShouldReturnFirstPageCorrectly() {
        // Arrange
        Pageable primeraPagina = PageRequest.of(0, 3);
        // Act
        Page<Vet> paginaVets = vetRepository.findAll(primeraPagina);
        // Assert
        assertThat(paginaVets.getContent()).hasSize(3);
        assertThat(paginaVets.getTotalElements()).isEqualTo(6);
        assertThat(paginaVets.getTotalPages()).isEqualTo(2);
        assertThat(paginaVets.isFirst()).isTrue();
        assertThat(paginaVets.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar segunda página correctamente")
    void findAllPageable_ShouldReturnSecondPageCorrectly() {
        // Arrange
        Pageable segundaPagina = PageRequest.of(1, 3);
        // Act
        Page<Vet> paginaDos = vetRepository.findAll(segundaPagina);
        // Assert
        assertThat(paginaDos.getContent()).hasSize(3);
        assertThat(paginaDos.isLast()).isTrue();
        assertThat(paginaDos.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("findAll(Pageable) - Debería retornar página vacía si el offset excede el total")
    void findAllPageable_ShouldReturnEmptyPage_WhenOffsetExceedsTotal() {
        // Arrange
        Pageable paginaFueraDeRango = PageRequest.of(10, 5);
        // Act
        Page<Vet> sinResultados = vetRepository.findAll(paginaFueraDeRango);
        // Assert
        assertThat(sinResultados.getContent()).isEmpty();
        assertThat(sinResultados.getTotalElements()).isEqualTo(6);
    }

    // Relación Vet-Specialty

    @Test
    @DisplayName("findAll - Debería cargar especialidades de Helen Leary (radiology)")
    void findAll_ShouldLoadSpecialtiesForHelenLeary() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act
        Vet helen = vets.stream()
                .filter(v -> v.getFirstName().equals("Helen") && v.getLastName().equals("Leary"))
                .findFirst()
                .orElseThrow();
        // Assert
        assertThat(helen.getNrOfSpecialties()).isEqualTo(1);
        assertThat(helen.getSpecialties())
                .extracting(Specialty::getName)
                .containsExactly("radiology");
    }

    @Test
    @DisplayName("findAll - Debería cargar múltiples especialidades de Linda Douglas")
    void findAll_ShouldLoadMultipleSpecialtiesForLindaDouglas() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act
        Vet linda = vets.stream()
                .filter(v -> v.getFirstName().equals("Linda") && v.getLastName().equals("Douglas"))
                .findFirst()
                .orElseThrow();
        // Assert
        assertThat(linda.getNrOfSpecialties()).isEqualTo(2);
        assertThat(linda.getSpecialties())
                .extracting(Specialty::getName)
                .containsExactlyInAnyOrder("surgery", "dentistry");
    }

    @Test
    @DisplayName("findAll - James Carter no tiene especialidades")
    void findAll_JamesCarterShouldHaveNoSpecialties() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act
        Vet james = vets.stream()
                .filter(v -> v.getFirstName().equals("James") && v.getLastName().equals("Carter"))
                .findFirst()
                .orElseThrow();
        // Assert
        assertThat(james.getNrOfSpecialties()).isZero();
        assertThat(james.getSpecialties()).isEmpty();
    }

    @Test
    @DisplayName("findAll - Sharon Jenkins no tiene especialidades")
    void findAll_SharonJenkinsShouldHaveNoSpecialties() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act
        Vet sharon = vets.stream()
                .filter(v -> v.getFirstName().equals("Sharon") && v.getLastName().equals("Jenkins"))
                .findFirst()
                .orElseThrow();
        // Assert
        assertThat(sharon.getNrOfSpecialties()).isZero();
    }

    @Test
    @DisplayName("getSpecialties - Debería retornar especialidades ordenadas alfabéticamente")
    void getSpecialties_ShouldReturnSpecialtiesSortedByName() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act
        Vet linda = vets.stream()
                .filter(v -> v.getFirstName().equals("Linda"))
                .findFirst()
                .orElseThrow();
        List<Specialty> especialidades = linda.getSpecialties();
        // Assert
        assertThat(especialidades)
                .extracting(Specialty::getName)
                .containsExactly("dentistry", "surgery");
    }

    @Test
    @DisplayName("findAll - Debería tener veterinarios con y sin especialidades")
    void findAll_ShouldHaveVetsWithAndWithoutSpecialties() {
        // Arrange
        Collection<Vet> todosVets = vetRepository.findAll();
        // Act & Assert
        long conEspecialidad = todosVets.stream()
                .filter(v -> v.getNrOfSpecialties() > 0)
                .count();
        long sinEspecialidad = todosVets.stream()
                .filter(v -> v.getNrOfSpecialties() == 0)
                .count();
        assertThat(conEspecialidad).isEqualTo(4);
        assertThat(sinEspecialidad).isEqualTo(2);
    }

    @Test
    @DisplayName("findAll - Todos los veterinarios deberían tener ID único")
    void findAll_AllVetsShouldHaveUniqueIds() {
        // Arrange
        Collection<Vet> vets = vetRepository.findAll();
        // Act & Assert
        List<Integer> idsVets = vets.stream()
                .map(Vet::getId)
                .toList();
        assertThat(idsVets).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("findAll(Pageable) - Paginación con tamaño 5 (como en el controlador)")
    void findAllPageable_ShouldWorkWithPageSizeFive() {
        // Arrange
        Pageable paginaCinco = PageRequest.of(0, 5);
        // Act
        Page<Vet> paginaResultado = vetRepository.findAll(paginaCinco);
        // Assert
        assertThat(paginaResultado.getContent()).hasSize(5);
        assertThat(paginaResultado.getTotalElements()).isEqualTo(6);
        assertThat(paginaResultado.getTotalPages()).isEqualTo(2);
    }
}
