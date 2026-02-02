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
 * Tests de integración del controlador de propietarios.
 */
@WebMvcTest(OwnerController.class)
@ActiveProfiles("test")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository ownerRepository;

    private Owner duenoPrimario;
    private Owner duenoSecundario;

    @BeforeEach
    void setUp() {
        duenoPrimario = new Owner();
        duenoPrimario.setId(1);
        duenoPrimario.setFirstName("George");
        duenoPrimario.setLastName("Franklin");
        duenoPrimario.setAddress("110 W. Liberty St.");
        duenoPrimario.setCity("Madison");
        duenoPrimario.setTelephone("6085551023");

        duenoSecundario = new Owner();
        duenoSecundario.setId(2);
        duenoSecundario.setFirstName("Betty");
        duenoSecundario.setLastName("Davis");
        duenoSecundario.setAddress("638 Cardinal Ave.");
        duenoSecundario.setCity("Sun Prairie");
        duenoSecundario.setTelephone("6085551749");
    }

    // formulario de alta
    @Nested
    @DisplayName("GET /owners/new - Formulario de creación")
    class InitCreationFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de creación con status 200")
        void initCreationForm_ShouldReturnCreationForm() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/owners/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeExists("owner"));
        }
    }

    // procesamiento de alta

    @Nested
    @DisplayName("POST /owners/new - Procesar creación")
    class ProcessCreationFormTests {
        @Test
        @DisplayName("Debería crear owner y redirigir cuando datos son válidos")
        void processCreationForm_ShouldCreateAndRedirect_WhenValidData() throws Exception {
            // Arrange
            when(ownerRepository.save(any(Owner.class))).thenAnswer(invocacion -> {
                Owner o = invocacion.getArgument(0);
                o.setId(1);
                return o;
            });
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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
            // Act & Assert
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

    // formulario de búsqueda
    @Nested
    @DisplayName("GET /owners/find - Formulario de búsqueda")
    class InitFindFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de búsqueda")
        void initFindForm_ShouldReturnFindForm() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/owners/find"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/findOwners"));
        }
    }

    // ejecución de búsqueda

    @Nested
    @DisplayName("GET /owners - Procesar búsqueda")
    class ProcessFindFormTests {

        @Test
        @DisplayName("Debería redirigir a detalles cuando encuentra exactamente un owner")
        void processFindForm_ShouldRedirectToDetails_WhenSingleOwnerFound() throws Exception {
            // Arrange
            Page<Owner> paginaUnica = new PageImpl<>(List.of(duenoPrimario));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(paginaUnica);
            // Act & Assert
            mockMvc.perform(get("/owners")
                    .param("lastName", "Franklin"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"));
        }

        @Test
        @DisplayName("Debería mostrar lista cuando encuentra múltiples owners")
        void processFindForm_ShouldShowList_WhenMultipleOwnersFound() throws Exception {
            // Arrange
            Page<Owner> paginaMultiple = new PageImpl<>(List.of(duenoPrimario, duenoSecundario));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(paginaMultiple);
            // Act & Assert
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
            // Arrange
            Page<Owner> sinResultados = new PageImpl<>(List.of());
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(sinResultados);
            // Act & Assert
            mockMvc.perform(get("/owners")
                    .param("lastName", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/findOwners"))
                    .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
        }

        @Test
        @DisplayName("Debería buscar todos los owners cuando lastName es vacío")
        void processFindForm_ShouldSearchAll_WhenLastNameEmpty() throws Exception {
            // Arrange
            Page<Owner> todosLosDuenos = new PageImpl<>(List.of(duenoPrimario));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(todosLosDuenos);
            // Act & Assert
            mockMvc.perform(get("/owners")
                    .param("lastName", ""))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("Debería manejar paginación correctamente")
        void processFindForm_ShouldHandlePagination() throws Exception {
            // Arrange
            Page<Owner> paginaConMetadata = new PageImpl<>(
                    List.of(duenoPrimario, duenoSecundario),
                    PageRequest.of(0, 5),
                    10);
            // Act & Assert
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(paginaConMetadata);

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
            // Arrange
            Page<Owner> paginaDefault = new PageImpl<>(List.of(duenoPrimario, duenoSecundario));
            when(ownerRepository.findByLastNameStartingWith(anyString(), any(Pageable.class)))
                    .thenReturn(paginaDefault);
            // Act & Assert
            mockMvc.perform(get("/owners")
                    .param("lastName", ""))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));
        }
    }

    // visualización de detalles

    @Nested
    @DisplayName("GET /owners/{ownerId} - Ver detalles")
    class ShowOwnerTests {
        @Test
        @DisplayName("Debería mostrar detalles del owner cuando existe")
        void showOwner_ShouldShowDetails_WhenOwnerExists() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(duenoPrimario));
            // Act & Assert
            mockMvc.perform(get("/owners/{ownerId}", 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/ownerDetails"))
                    .andExpect(model().attributeExists("owner"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void showOwner_ShouldThrowException_WhenOwnerNotFound()  {
            // Arrange
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());
            // Act & Assert
            ServletException errorCapturado = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/owners/{ownerId}", 999)));
            assertInstanceOf(IllegalArgumentException.class, errorCapturado.getCause());
        }
    }
    // formulario de edición

    @Nested
    @DisplayName("GET /owners/{ownerId}/edit - Formulario de edición")
    class InitUpdateFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de edición con datos del owner")
        void initUpdateForm_ShouldShowEditFormWithOwnerData() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(duenoPrimario));
            // Act & Assert
            mockMvc.perform(get("/owners/{ownerId}/edit", 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                    .andExpect(model().attributeExists("owner"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void initUpdateForm_ShouldThrowException_WhenOwnerNotFound()  {
            // Arrange
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());
            // Act & Assert
            ServletException excepcion = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/owners/{ownerId}/edit", 999)));
            assertInstanceOf(IllegalArgumentException.class, excepcion.getCause());
        }
    }

    // procesamiento de edición

    @Nested
    @DisplayName("POST /owners/{ownerId}/edit - Procesar edición")
    class ProcessUpdateFormTests {

        @Test
        @DisplayName("Debería actualizar owner y redirigir cuando datos son válidos")
        void processUpdateForm_ShouldUpdateAndRedirect_WhenValidData() throws Exception {
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(duenoPrimario));
            when(ownerRepository.save(any(Owner.class))).thenReturn(duenoPrimario);
            // Act & Assert
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
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(duenoPrimario));
            // Act & Assert
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
            // Arrange
            when(ownerRepository.findById(1)).thenReturn(Optional.of(duenoPrimario));
            // Act & Assert
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
