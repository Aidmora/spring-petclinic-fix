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
 * Tests de integración para OwnerRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class OwnerRepositoryTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    @DisplayName("findById - Debería retornar Owner cuando existe el ID")
    void findById_ShouldReturnOwner_WhenOwnerExists() {
        // Arrange
        Integer idBuscado = 1;
        // Act
        Optional<Owner> ownerEncontrado = ownerRepository.findById(idBuscado);
        // Assert
        assertThat(ownerEncontrado).isPresent();
        assertThat(ownerEncontrado.get().getFirstName()).isEqualTo("George");
        assertThat(ownerEncontrado.get().getLastName()).isEqualTo("Franklin");
        assertThat(ownerEncontrado.get().getAddress()).isEqualTo("110 W. Liberty St.");
        assertThat(ownerEncontrado.get().getCity()).isEqualTo("Madison");
        assertThat(ownerEncontrado.get().getTelephone()).isEqualTo("6085551023");
    }

    @Test
    @DisplayName("findById - Debería retornar vacío cuando no existe el ID")
    void findById_ShouldReturnEmpty_WhenOwnerDoesNotExist() {
        // Arrange
        Integer idInexistente = 999;
        // Act
        Optional<Owner> noExiste = ownerRepository.findById(idInexistente);
        // Assert
        assertThat(noExiste).isEmpty();
    }

    // -Búsqueda por apellido

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar owners que empiecen con 'Davis'")
    void findByLastNameStartingWith_ShouldReturnOwners_WhenLastNameMatches() {
        // Arrange
        String apellido = "Davis";
        Pageable paginacion = PageRequest.of(0, 10);
        // Act
        Page<Owner> ownersDavis = ownerRepository.findByLastNameStartingWith(apellido, paginacion);
        // Assert
        assertThat(ownersDavis.getContent()).hasSize(2);
        assertThat(ownersDavis.getContent())
                .extracting(Owner::getLastName)
                .containsOnly("Davis");
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar owners con apellido parcial 'Dav'")
    void findByLastNameStartingWith_ShouldReturnOwners_WhenPartialLastNameMatches() {
        // Arrange
        String apellidoParcial = "Dav";
        Pageable paginacion = PageRequest.of(0, 10);
        // Act
        Page<Owner> coincidencias = ownerRepository.findByLastNameStartingWith(apellidoParcial, paginacion);
        // Assert
        assertThat(coincidencias.getContent()).hasSize(2);
        assertThat(coincidencias.getContent())
                .extracting(Owner::getLastName)
                .allMatch(nombre -> nombre.startsWith("Dav"));
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar lista vacía cuando no hay coincidencias")
    void findByLastNameStartingWith_ShouldReturnEmpty_WhenNoMatch() {
        // Arrange
        String apellidoRaro = "Zzz";
        Pageable paginacion = PageRequest.of(0, 10);
        // Act
        Page<Owner> sinCoincidencias = ownerRepository.findByLastNameStartingWith(apellidoRaro, paginacion);
        // Assert
        assertThat(sinCoincidencias.getContent()).isEmpty();
        assertThat(sinCoincidencias.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByLastNameStartingWith - Debería retornar todos los owners con string vacío")
    void findByLastNameStartingWith_ShouldReturnAllOwners_WhenEmptyString() {
        // Arrange
        Pageable paginacion = PageRequest.of(0, 20);
        // Act
        Page<Owner> todos = ownerRepository.findByLastNameStartingWith("", paginacion);
        // Assert
        assertThat(todos.getContent()).hasSize(10);
    }

    // Paginación

    @Test
    @DisplayName("findByLastNameStartingWith - Debería paginar correctamente")
    void findByLastNameStartingWith_ShouldPaginateCorrectly() {
        // Arrange
        Pageable paginaUno = PageRequest.of(0, 5);
        Pageable paginaDos = PageRequest.of(1, 5);
        // Act
        Page<Owner> primeraPagina = ownerRepository.findByLastNameStartingWith("", paginaUno);
        Page<Owner> segundaPagina = ownerRepository.findByLastNameStartingWith("", paginaDos);
        // Assert
        assertThat(primeraPagina.getContent()).hasSize(5);
        assertThat(segundaPagina.getContent()).hasSize(5);
        assertThat(primeraPagina.getTotalElements()).isEqualTo(10);
        assertThat(primeraPagina.getTotalPages()).isEqualTo(2);
    }

    // Persistencia de nuevos owners

    @Test
    @DisplayName("save - Debería guardar un nuevo Owner correctamente")
    void save_ShouldPersistNewOwner() {
        // Arrange
        Owner nuevoOwner = new Owner();
        nuevoOwner.setFirstName("Carlos");
        nuevoOwner.setLastName("Mendez");
        nuevoOwner.setAddress("Av. Amazonas N34-12");
        nuevoOwner.setCity("Quito");
        nuevoOwner.setTelephone("0991234567");
        // Act
        Owner guardado = ownerRepository.save(nuevoOwner);
        // Assert
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFirstName()).isEqualTo("Carlos");
        assertThat(guardado.getLastName()).isEqualTo("Mendez");

        Optional<Owner> recuperado = ownerRepository.findById(guardado.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getFirstName()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("save - Debería actualizar un Owner existente")
    void save_ShouldUpdateExistingOwner() {
        // Arrange
        Owner ownerExistente = ownerRepository.findById(1).orElseThrow();
        String nombreOriginal = ownerExistente.getFirstName();
        ownerExistente.setFirstName("Roberto");
        // Act
        Owner actualizado = ownerRepository.save(ownerExistente);
        // Assert
        assertThat(actualizado.getId()).isEqualTo(1);
        assertThat(actualizado.getFirstName()).isEqualTo("Roberto");
        assertThat(actualizado.getFirstName()).isNotEqualTo(nombreOriginal);
    }

    // Relación Owner-Pet

    @Test
    @DisplayName("findById - Debería cargar las mascotas del Owner (EAGER loading)")
    void findById_ShouldLoadPetsWithOwner() {
        // Arrange
        Integer ownerConMascotas = 6;
        // Act
        Optional<Owner> ownerCargado = ownerRepository.findById(ownerConMascotas);
        // Assert
        assertThat(ownerCargado).isPresent();
        assertThat(ownerCargado.get().getPets()).hasSize(2);
        assertThat(ownerCargado.get().getPets())
                .extracting(Pet::getName)
                .containsExactlyInAnyOrder("Samantha", "Max");
    }

    @Test
    @DisplayName("findById - Debería retornar Owner sin mascotas si no tiene")
    void findById_ShouldReturnOwnerWithEmptyPets_WhenNoPets() {
        // Arrange
        Owner sinMascotas = new Owner();
        sinMascotas.setFirstName("Patricia");
        sinMascotas.setLastName("Vega");
        sinMascotas.setAddress("Calle Los Cedros 45");
        sinMascotas.setCity("Guayaquil");
        sinMascotas.setTelephone("0987654321");
        Owner persistido = ownerRepository.save(sinMascotas);
        // Act
        Optional<Owner> recuperado = ownerRepository.findById(persistido.getId());
        // Assert
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getPets()).isEmpty();
    }
}
