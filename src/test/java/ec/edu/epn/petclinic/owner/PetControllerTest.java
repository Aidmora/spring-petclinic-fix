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
 * Pruebas de integración para PetController usando MockMvc.
 * Valida el comportamiento de los endpoints REST relacionados con Pet.
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

    private Owner testOwner;
    private Pet testPet;
    private PetType dogType;
    private PetType catType;

    @BeforeEach
    void setUp() {
        // Configurar tipos de mascota
        dogType = new PetType();
        dogType.setId(1);
        dogType.setName("dog");

        catType = new PetType();
        catType.setId(2);
        catType.setName("cat");

        // Configurar owner de prueba
        testOwner = new Owner();
        testOwner.setId(1);
        testOwner.setFirstName("George");
        testOwner.setLastName("Franklin");
        testOwner.setAddress("110 W. Liberty St.");
        testOwner.setCity("Madison");
        testOwner.setTelephone("6085551023");

        // Configurar pet de prueba
        testPet = new Pet();
        testPet.setId(1);
        testPet.setName("Leo");
        testPet.setBirthDate(LocalDate.of(2020, 5, 15));
        testPet.setType(catType);
    }

    // Tests para GET /owners/{ownerId}/pets/new

    @Nested
    @DisplayName("GET /owners/{ownerId}/pets/new - Formulario de creación de mascota")
    class InitCreationFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de creación de mascota")
        void initCreationForm_ShouldShowPetCreationForm() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
            // ARRANGE
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}/pets/new", 999))
            );
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        }
    }

    // Tests para POST /owners/{ownerId}/pets/new

    @Nested
    @DisplayName("POST /owners/{ownerId}/pets/new - Procesar creación de mascota")
    class ProcessCreationFormTests {

        @Test
        @DisplayName("Debería crear mascota y redirigir cuando datos son válidos")
        void processCreationForm_ShouldCreateAndRedirect_WhenValidData() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));
            when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

            // ACT & ASSERT
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
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            String futureDate = LocalDate.now().plusDays(30).toString();

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", futureDate)
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando type es null para pet nuevo")
        void processCreationForm_ShouldShowErrors_WhenTypeNull() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
            // ARRANGE
            Pet existingPet = new Pet();
            existingPet.setName("Buddy");
            testOwner.addPet(existingPet);
            existingPet.setId(10); // Set ID so pet is not considered "new" and duplicate check works

            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/pets/new", 1)
                    .param("name", "Buddy")
                    .param("birthDate", "2020-05-15")
                    .param("type", "dog"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "name"));
        }
    }

    // Tests para GET /owners/{ownerId}/pets/{petId}/edit

    @Nested
    @DisplayName("GET /owners/{ownerId}/pets/{petId}/edit - Formulario de edición de mascota")
    class InitUpdateFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de edición con datos de la mascota")
        void initUpdateForm_ShouldShowEditFormWithPetData() throws Exception {
            // ARRANGE
            testOwner.getPets().add(testPet);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
            mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", 1, 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeExists("pet"))
                    .andExpect(model().attributeExists("owner"))
                    .andExpect(model().attributeExists("types"));
        }
    }

    // Tests para POST /owners/{ownerId}/pets/{petId}/edit

    @Nested
    @DisplayName("POST /owners/{ownerId}/pets/{petId}/edit - Procesar edición de mascota")
    class ProcessUpdateFormTests {

        @Test
        @DisplayName("Debería actualizar mascota y redirigir cuando datos son válidos")
        void processUpdateForm_ShouldUpdateAndRedirect_WhenValidData() throws Exception {
            // ARRANGE
            testOwner.getPets().add(testPet);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));
            when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

            // ACT & ASSERT
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
            // ARRANGE
            testOwner.getPets().add(testPet);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
            // ARRANGE
            testOwner.getPets().add(testPet);
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            String futureDate = LocalDate.now().plusDays(30).toString();

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", 1, 1)
                    .param("name", "Leo")
                    .param("birthDate", futureDate)
                    .param("type", "cat"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdatePetForm"))
                    .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando nombre duplicado con otra mascota")
        void processUpdateForm_ShouldShowErrors_WhenDuplicateNameWithOtherPet() throws Exception {
            // ARRANGE
            Pet anotherPet = new Pet();
            anotherPet.setId(2);
            anotherPet.setName("Max");
            anotherPet.setType(dogType);
            anotherPet.setBirthDate(LocalDate.of(2019, 1, 1));

            // Add anotherPet BEFORE testPet so it's found first when checking for duplicates
            testOwner.getPets().add(anotherPet);
            testOwner.getPets().add(testPet);

            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(petTypeRepository.findPetTypes()).thenReturn(List.of(dogType, catType));

            // ACT & ASSERT
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
