package ec.edu.epn.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Suite de pruebas para validar VisitController.
 * Cubre escenarios de creación de visitas mediante MockMvc.
 */
@WebMvcTest(VisitController.class)
@ActiveProfiles("test")
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository ownerRepository;

    private Owner propietario;
    private Pet mascotaPrueba;
    private PetType tipoGato;

    @BeforeEach
    void setUp() {
        // tipo de animal para las pruebas
        tipoGato = new PetType();
        tipoGato.setId(1);
        tipoGato.setName("cat");

        // mascota asociada al propietario
        mascotaPrueba = new Pet();
        mascotaPrueba.setId(1);
        mascotaPrueba.setName("Leo");
        mascotaPrueba.setBirthDate(LocalDate.of(2020, 5, 15));
        mascotaPrueba.setType(tipoGato);

        // propietario base con mascota vinculada
        propietario = new Owner();
        propietario.setId(1);
        propietario.setFirstName("George");
        propietario.setLastName("Franklin");
        propietario.setAddress("110 W. Liberty St.");
        propietario.setCity("Madison");
        propietario.setTelephone("6085551023");
        propietario.getPets().add(mascotaPrueba);
    }

    // Sección: formulario para registrar nueva visita

    @Nested
    @DisplayName("GET /owners/{ownerId}/pets/{petId}/visits/new - Formulario de nueva visita")
    class InitNewVisitFormTests {

        @Test
        @DisplayName("Debería mostrar formulario de nueva visita")
        void initNewVisitForm_ShouldShowVisitForm() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));

            mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdateVisitForm"))
                    .andExpect(model().attributeExists("visit"))
                    .andExpect(model().attributeExists("pet"))
                    .andExpect(model().attributeExists("owner"));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando owner no existe")
        void initNewVisitForm_ShouldThrowException_WhenOwnerNotFound() throws Exception {
            when(ownerRepository.findById(999)).thenReturn(Optional.empty());
            ServletException ex = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", 999, 1)));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando pet no existe para el owner")
        void initNewVisitForm_ShouldThrowException_WhenPetNotFound() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            ServletException thrown = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", 1, 999)));
            assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        }
    }

    // Sección: procesamiento del formulario de visita
    @Nested
    @DisplayName("POST /owners/{ownerId}/pets/{petId}/visits/new - Procesar nueva visita")
    class ProcessNewVisitFormTests {

        @Test
        @DisplayName("Debería crear visita y redirigir cuando datos son válidos")
        void processNewVisitForm_ShouldCreateAndRedirect_WhenValidData() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietario);

            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", LocalDate.now().toString())
                    .param("description", "Annual checkup"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"))
                    .andExpect(flash().attribute("message", "Your visit has been booked"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando description está vacía")
        void processNewVisitForm_ShouldShowErrors_WhenDescriptionEmpty() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));

            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", LocalDate.now().toString())
                    .param("description", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdateVisitForm"))
                    .andExpect(model().attributeHasFieldErrors("visit", "description"));
        }

        @Test
        @DisplayName("Debería mostrar errores cuando description es solo espacios")
        void processNewVisitForm_ShouldShowErrors_WhenDescriptionBlank() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", LocalDate.now().toString())
                    .param("description", "   "))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pets/createOrUpdateVisitForm"))
                    .andExpect(model().attributeHasFieldErrors("visit", "description"));
        }

        @Test
        @DisplayName("Debería aceptar visita con fecha pasada")
        void processNewVisitForm_ShouldAccept_WhenDateInPast() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietario);

            String fechaAnterior = LocalDate.now().minusDays(7).toString();

            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", fechaAnterior)
                    .param("description", "Follow-up visit"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"));
        }

        @Test
        @DisplayName("Debería aceptar visita con fecha futura")
        void processNewVisitForm_ShouldAccept_WhenDateInFuture() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietario);

            String fechaProgramada = LocalDate.now().plusDays(7).toString();

            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", fechaProgramada)
                    .param("description", "Scheduled vaccination"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"));
        }

        @Test
        @DisplayName("Debería crear visita con descripción larga")
        void processNewVisitForm_ShouldAccept_WhenDescriptionLong() throws Exception {
            when(ownerRepository.findById(1)).thenReturn(Optional.of(propietario));
            when(ownerRepository.save(any(Owner.class))).thenReturn(propietario);

            String textoExtendido = "This is a very detailed description of the visit " +
                    "including all symptoms observed, treatments administered, " +
                    "and follow-up instructions for the pet owner.";

            mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 1, 1)
                    .param("date", LocalDate.now().toString())
                    .param("description", textoExtendido))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/owners/1"));
        }
    }
}
