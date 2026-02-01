package ec.edu.epn.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración para OwnerController usando MockMvc.
 * Valida el comportamiento de los endpoints REST relacionados con Owner.
 */
@WebMvcTest(OwnerController.class)
@ActiveProfiles("test")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository ownerRepository;

    private Owner testOwner;
    private Owner testOwner2;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1);
        testOwner.setFirstName("George");
        testOwner.setLastName("Franklin");
        testOwner.setAddress("110 W. Liberty St.");
        testOwner.setCity("Madison");
        testOwner.setTelephone("6085551023");

        testOwner2 = new Owner();
        testOwner2.setId(2);
        testOwner2.setFirstName("Betty");
        testOwner2.setLastName("Davis");
        testOwner2.setAddress("638 Cardinal Ave.");
        testOwner2.setCity("Sun Prairie");
        testOwner2.setTelephone("6085551749");
    }

    // Tests para GET /owners/new
    @Nested
    @DisplayName("GET /owners/new - Formulario de creación")
    class InitCreationFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de creación con status 200")
        void initCreationForm_ShouldReturnCreationForm() throws Exception {
            // ARRANGE - No requiere configuración

            // ACT & ASSERT
            mockMvc.perform(get("/owners/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeExists("owner"));
        }
    }

    // Tests para POST /owners/new

    @Nested
    @DisplayName("POST /owners/new - Procesar creación")
    class ProcessCreationFormTests {

        @Test
        @DisplayName("Debería crear owner y redirigir cuando datos son válidos")
        void processCreationForm_ShouldCreateAndRedirect_WhenValidData() throws Exception {
            // ARRANGE
            when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> {
                Owner owner = invocation.getArgument(0);
                owner.setId(1);
                return owner;
            });

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("address", "123 Main St")
                    .param("city", "Springfield")
                    .param("telephone", "1234567890"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"))
                    .andExpect(flash().attribute("message", "New Owner Created"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando firstName está vacío")
        void processCreationForm_ShouldShowErrors_WhenFirstNameEmpty() throws Exception {
            // ARRANGE - firstName vacío

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "")
                    .param("lastName", "Doe")
                    .param("address", "123 Main St")
                    .param("city", "Springfield")
                    .param("telephone", "1234567890"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "firstName"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando lastName está vacío")
        void processCreationForm_ShouldShowErrors_WhenLastNameEmpty() throws Exception {
            // ARRANGE - lastName vacío

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "")
                    .param("address", "123 Main St")
                    .param("city", "Springfield")
                    .param("telephone", "1234567890"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando address está vacío")
        void processCreationForm_ShouldShowErrors_WhenAddressEmpty() throws Exception {
            // ARRANGE - address vacío

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("address", "")
                    .param("city", "Springfield")
                    .param("telephone", "1234567890"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "address"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando city está vacío")
        void processCreationForm_ShouldShowErrors_WhenCityEmpty() throws Exception {
            // ARRANGE - city vacío

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("address", "123 Main St")
                    .param("city", "")
                    .param("telephone", "1234567890"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "city"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando telephone tiene formato inválido")
        void processCreationForm_ShouldShowErrors_WhenTelephoneInvalid() throws Exception {
            // ARRANGE - telephone no tiene 10 dígitos

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("address", "123 Main St")
                    .param("city", "Springfield")
                    .param("telephone", "12345"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "telephone"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando telephone contiene letras")
        void processCreationForm_ShouldShowErrors_WhenTelephoneContainsLetters() throws Exception {
            // ARRANGE - telephone con letras

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("address", "123 Main St")
                    .param("city", "Springfield")
                    .param("telephone", "123abc7890"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "telephone"));
        }

        @Test
        @DisplayName("Debería mostrar múltiples errores cuando varios campos son inválidos")
        void processCreationForm_ShouldShowMultipleErrors_WhenMultipleFieldsInvalid() throws Exception {
            // ARRANGE - múltiples campos vacíos

            // ACT & ASSERT
            mockMvc.perform(post("/owners/new")
                    .param("firstName", "")
                    .param("lastName", "")
                    .param("address", "")
                    .param("city", "")
                    .param("telephone", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasErrors("owner"));
        }
    }

    // Tests para GET /owners/find
    @Nested
    @DisplayName("GET /owners/find - Formulario de búsqueda")
    class InitFindFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de búsqueda")
        void initFindForm_ShouldReturnFindForm() throws Exception {
            // ARRANGE - No requiere configuración

            // ACT & ASSERT
            mockMvc.perform(get("/owners/find"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/findOwners"));
        }
    }

    // Tests para GET /owners (búsqueda)

    @Nested
    @DisplayName("GET /owners - Procesar búsqueda")
    class ProcessFindFormTests {

        @Test
        @DisplayName("Debería redirigir a detalles cuando encuentra exactamente un owner")
        void processFindForm_ShouldRedirectToDetails_WhenSingleOwnerFound() throws Exception {
            // ARRANGE
            Page<Owner> singleOwnerPage = new PageImpl<>(List.of(testOwner));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(singleOwnerPage);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("lastName", "Franklin"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"));
        }

        @Test
        @DisplayName("Debería mostrar lista cuando encuentra múltiples owners")
        void processFindForm_ShouldShowList_WhenMultipleOwnersFound() throws Exception {
            // ARRANGE
            Page<Owner> multipleOwnersPage = new PageImpl<>(List.of(testOwner, testOwner2));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(multipleOwnersPage);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("lastName", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/ownersList"))
                    .andExpect(model().attributeExists("listOwners"))
                    .andExpect(model().attributeExists("currentPage"))
                    .andExpect(model().attributeExists("totalPages"))
                    .andExpect(model().attributeExists("totalItems"));
        }

        @Test
        @DisplayName("Debería mostrar error cuando no encuentra owners")
        void processFindForm_ShouldShowError_WhenNoOwnersFound() throws Exception {
            // ARRANGE
            Page<Owner> emptyPage = new PageImpl<>(List.of());
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("lastName", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/findOwners"))
                    .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
        }

        @Test
        @DisplayName("Debería buscar todos los owners cuando lastName es vacío")
        void processFindForm_ShouldSearchAll_WhenLastNameEmpty() throws Exception {
            // ARRANGE
            Page<Owner> allOwnersPage = new PageImpl<>(List.of(testOwner));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(allOwnersPage);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("lastName", ""))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("Debería manejar paginación correctamente")
        void processFindForm_ShouldHandlePagination() throws Exception {
            // ARRANGE
            Page<Owner> paginatedPage = new PageImpl<>(
                    List.of(testOwner, testOwner2),
                    PageRequest.of(0, 5),
                    10 // Total de 10 elementos
            );
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(paginatedPage);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("page", "1")
                    .param("lastName", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/ownersList"))
                    .andExpect(model().attribute("currentPage", 1))
                    .andExpect(model().attribute("totalPages", 2))
                    .andExpect(model().attribute("totalItems", 10L));
        }

        @Test
        @DisplayName("Debería usar página 1 por defecto")
        void processFindForm_ShouldUseDefaultPage() throws Exception {
            // ARRANGE
            Page<Owner> page = new PageImpl<>(List.of(testOwner, testOwner2));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(page);

            // ACT & ASSERT
            mockMvc.perform(get("/owners")
                    .param("lastName", ""))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));
        }
    }

    // Tests para GET /owners/{ownerId}

    @Nested
    @DisplayName("GET /owners/{ownerId} - Ver detalles")
    class ShowOwnerTests {

        @Test
        @DisplayName("Debería mostrar detalles del owner cuando existe")
        void showOwner_ShouldShowDetails_WhenOwnerExists() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));

            // ACT & ASSERT
            mockMvc.perform(get("/owners/{ownerId}", 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/ownerDetails"))
                    .andExpect(model().attributeExists("owner"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void showOwner_ShouldThrowException_WhenOwnerNotFound() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}", 999))
            );
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        }
    }
    // Tests para GET /owners/{ownerId}/edit

    @Nested
    @DisplayName("GET /owners/{ownerId}/edit - Formulario de edición")
    class InitUpdateFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de edición con datos del owner")
        void initUpdateForm_ShouldShowEditFormWithOwnerData() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));

            // ACT & ASSERT
            mockMvc.perform(get("/owners/{ownerId}/edit", 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeExists("owner"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void initUpdateForm_ShouldThrowException_WhenOwnerNotFound() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}/edit", 999))
            );
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        }
    }

    // Tests para POST /owners/{ownerId}/edit

    @Nested
    @DisplayName("POST /owners/{ownerId}/edit - Procesar edición")
    class ProcessUpdateFormTests {

        @Test
        @DisplayName("Debería actualizar owner y redirigir cuando datos son válidos")
        void processUpdateForm_ShouldUpdateAndRedirect_WhenValidData() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));
            when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/edit", 1)
                    .param("firstName", "George")
                    .param("lastName", "Franklin")
                    .param("address", "Updated Address")
                    .param("city", "Madison")
                    .param("telephone", "6085551023"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"))
                    .andExpect(flash().attribute("message", "Owner Values Updated"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando datos son inválidos")
        void processUpdateForm_ShouldShowErrors_WhenInvalidData() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/edit", 1)
                    .param("firstName", "")
                    .param("lastName", "Franklin")
                    .param("address", "110 W. Liberty St.")
                    .param("city", "Madison")
                    .param("telephone", "6085551023"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "firstName"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando telephone es inválido en update")
        void processUpdateForm_ShouldShowErrors_WhenTelephoneInvalid() throws Exception {
            // ARRANGE
            when(ownerRepository.findById(1)).thenReturn(Optional.of(testOwner));

            // ACT & ASSERT
            mockMvc.perform(post("/owners/{ownerId}/edit", 1)
                    .param("firstName", "George")
                    .param("lastName", "Franklin")
                    .param("address", "110 W. Liberty St.")
                    .param("city", "Madison")
                    .param("telephone", "invalid"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeHasFieldErrors("owner", "telephone"));
        }
    }
}
