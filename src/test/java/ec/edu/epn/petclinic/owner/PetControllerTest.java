package ec.edu.epn.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Validación del controlador de mascotas con MockMvc.
 * Incluye escenarios de alta, edición y gestión de tipos.
 */
@WebMvcTest(PetController.class)
@Import(PetTypeFormatter.class)
@ActiveProfiles("test")
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository ownerRepository;

    @MockitoBean
    private PetTypeRepository petTypeRepository;

    private Owner propietarioBase;
    private Pet animalExistente;
    private PetType tipoPerro;
    private PetType tipoFelino;

    @BeforeEach
    void setUp() {
        // definición de tipos disponibles
        tipoPerro = new PetType();
        tipoPerro.setId(1);
        tipoPerro.setName("dog");

        tipoFelino = new PetType();
        tipoFelino.setId(2);
        tipoFelino.setName("cat");

        // propietario para las pruebas
        propietarioBase = new Owner();
        propietarioBase.setId(1);
        propietarioBase.setFirstName("George");
        propietarioBase.setLastName("Franklin");
        propietarioBase.setAddress("110 W. Liberty St.");
        propietarioBase.setCity("Madison");
        propietarioBase.setTelephone("6085551023");

        // mascota ya registrada
        animalExistente = new Pet();
        animalExistente.setId(1);
        animalExistente.setName("Leo");
        animalExistente.setBirthDate(LocalDate.of(2020, 5, 15));
        animalExistente.setType(tipoFelino);
    }

    // alta de nueva mascota

    @Nested
    @DisplayName("GET /owners/{ownerId}/pets/new - Formulario de creación de mascota")
    class InitCreationFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de creación de mascota")
        void initCreationForm_ShouldShowPetCreationForm() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act & Assert
            mockMvc.perform(get("/owners/{ownerId}/pets/new", 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeExists("pet"))
                    .andExpect(model().attributeExists("owner"))
                    .andExpect(model().attributeExists("types"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void initCreationForm_ShouldThrowException_WhenOwnerNotFound() throws Exception {
            // Arrange
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());

            // propietario inexistente provoca error
            // Act & Assert
            ServletException fallo = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/owners/{ownerId}/pets/new", 999)));
            assertInstanceOf(IllegalArgumentException.class, fallo.getCause());
        }
    }

    // procesamiento del formulario de alta

    @Nested
    @DisplayName("POST /owners/{ownerId}/pets/new - Procesar creación de mascota")
    class ProcessCreationFormTests {

        @Test
        @DisplayName("Debería crear mascota y redirigir cuando datos son válidos")
        void processCreationForm_ShouldCreateAndRedirect_WhenValidData() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietarioBase);
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", "2020-05-15")
                    .param("type", "dog"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"))
                    .andExpect(flash().attribute("message", "New Pet has been Added"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando name está vacío")
        void processCreationForm_ShouldShowErrors_WhenNameEmpty() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "")
                    .param("birthDate", "2020-05-15")
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "name"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando birthDate es null")
        void processCreationForm_ShouldShowErrors_WhenBirthDateNull() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));

            // sin fecha de nacimiento
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando birthDate está en el futuro")
        void processCreationForm_ShouldShowErrors_WhenBirthDateInFuture() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act
            String fechaFutura = LocalDate.now().plusDays(30).toString();
            // Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", fechaFutura)
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando type es null para pet nuevo")
        void processCreationForm_ShouldShowErrors_WhenTypeNull() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));

            // tipo de mascota no especificado
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", "2020-05-15"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "type"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando nombre de mascota ya existe para el owner")
        void processCreationForm_ShouldShowErrors_WhenDuplicatePetName() throws Exception {
            // Arrange
            Pet mascotaPrevia = new Pet();
            mascotaPrevia.setName("Buddy");
            propietarioBase.addPet(mascotaPrevia);
            mascotaPrevia.setId(10);
            // Act

            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));

            // nombre repetido
            // Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", "2020-05-15")
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "name"));
        }
    }

    // formulario de edición de mascota

    @Nested
    @DisplayName("GET /owners/{ownerId}/pets/{petId}/edit - Formulario de edición de mascota")
    class InitUpdateFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de edición con datos de la mascota")
        void initUpdateForm_ShouldShowEditFormWithPetData() throws Exception {
            // Arrange
            propietarioBase.getPets().add(animalExistente);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act & Assert
            mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", 1, 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeExists("pet"))
                    .andExpect(model().attributeExists("owner"))
                    .andExpect(model().attributeExists("types"));
        }
    }

    // procesamiento de edición

    @Nested
    @DisplayName("POST /owners/{ownerId}/pets/{petId}/edit - Procesar edición de mascota")
    class ProcessUpdateFormTests {

        @Test
        @DisplayName("Debería actualizar mascota y redirigir cuando datos son válidos")
        void processUpdateForm_ShouldUpdateAndRedirect_WhenValidData() throws Exception {
            // Arrange
            propietarioBase.getPets().add(animalExistente);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietarioBase);
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 1, 1)
                    .param("name", "Leo Updated")
                    .param("birthDate", "2020-05-15")
                    .param("type", "cat"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"))
                    .andExpect(flash().attribute("message", "Pet details has been edited"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando name está vacío en update")
        void processUpdateForm_ShouldShowErrors_WhenNameEmpty() throws Exception {
            // Arrange
            propietarioBase.getPets().add(animalExistente);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act & Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 1, 1)
                    .param("name", "")
                    .param("birthDate", "2020-05-15")
                    .param("type", "cat"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "name"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando birthDate está en el futuro en update")
        void processUpdateForm_ShouldShowErrors_WhenBirthDateInFuture() throws Exception {
            // Arrange
            propietarioBase.getPets().add(animalExistente);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Act
            String fechaPosterior = LocalDate.now().plusDays(30).toString();
            // Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 1, 1)
                    .param("name", "Leo")
                    .param("birthDate", fechaPosterior)
                    .param("type", "cat"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando nombre duplicado con otra mascota")
        void processUpdateForm_ShouldShowErrors_WhenDuplicateNameWithOtherPet() throws Exception {
            // Arrange
            Pet otraMascota = new Pet();
            otraMascota.setId(2);
            otraMascota.setName("Max");
            otraMascota.setType(tipoPerro);
            otraMascota.setBirthDate(LocalDate.of(2019, 1, 1));

            // agregar primero la otra mascota para que se detecte duplicado
            propietarioBase.getPets().add(otraMascota);
            propietarioBase.getPets().add(animalExistente);
            // Act
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietarioBase));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(tipoPerro, tipoFelino));
            // Assert
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 1, 1)
                    .param("name", "Max")
                    .param("birthDate", "2020-05-15")
                    .param("type", "cat"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "name"));
        }
    }
}
