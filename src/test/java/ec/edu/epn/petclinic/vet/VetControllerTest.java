package ec.edu.epn.petclinic.vet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración para VetController usando MockMvc.
 */
@WebMvcTest(VetController.class)
@ActiveProfiles("test")
class VetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VetRepository vetRepository;

    private Vet vet1;
    private Vet vet2;
    private Vet vet3;
    private Specialty radiology;
    private Specialty surgery;

    @BeforeEach
    void setUp() {
        // Configurar especialidades
        radiology = new Specialty();
        radiology.setId(1);
        radiology.setName("radiology");

        surgery = new Specialty();
        surgery.setId(2);
        surgery.setName("surgery");

        // Configurar veterinario 1 - sin especialidades
        vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");

        // Configurar veterinario 2 - con una especialidad
        vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        vet2.addSpecialty(radiology);

        // Configurar veterinario 3 - con múltiples especialidades
        vet3 = new Vet();
        vet3.setId(3);
        vet3.setFirstName("Linda");
        vet3.setLastName("Douglas");
        vet3.addSpecialty(surgery);
        vet3.addSpecialty(radiology);
    }

    // Tests para GET /vets.html (Vista HTML)

    @Nested
    @DisplayName("GET /vets.html - Vista HTML paginada")
    class ShowVetListHtmlTests {

        @Test
        @DisplayName("Debería mostrar lista de veterinarios con paginación")
        void showVetList_ShouldShowVetListWithPagination() throws Exception {
            // ARRANGE
            Page<Vet> vetPage = new PageImpl<>(
                    List.of(vet1, vet2, vet3),
                    PageRequest.of(0, 5),
                    3);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(vetPage);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("vets/vetList"))
                    .andExpect(model().attributeExists("listVets"))
                    .andExpect(model().attributeExists("currentPage"))
                    .andExpect(model().attributeExists("totalPages"))
                    .andExpect(model().attributeExists("totalItems"));
        }

        @Test
        @DisplayName("Debería mostrar página 1 por defecto")
        void showVetList_ShouldShowFirstPageByDefault() throws Exception {
            // ARRANGE
            Page<Vet> vetPage = new PageImpl<>(
                    List.of(vet1, vet2, vet3),
                    PageRequest.of(0, 5),
                    3);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(vetPage);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));
        }

        @Test
        @DisplayName("Debería manejar parámetro de página correctamente")
        void showVetList_ShouldHandlePageParameter() throws Exception {
            // ARRANGE
            Page<Vet> vetPage = new PageImpl<>(
                    List.of(vet1),
                    PageRequest.of(1, 5),
                    6);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(vetPage);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html")
                    .param("page", "2"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 2));
        }

        @Test
        @DisplayName("Debería calcular total de páginas correctamente")
        void showVetList_ShouldCalculateTotalPagesCorrectly() throws Exception {
            // ARRANGE
            Page<Vet> vetPage = new PageImpl<>(
                    List.of(vet1, vet2, vet3),
                    PageRequest.of(0, 5),
                    12);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(vetPage);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("totalPages", 3))
                    .andExpect(model().attribute("totalItems", 12L));
        }

        @Test
        @DisplayName("Debería mostrar lista vacía cuando no hay veterinarios")
        void showVetList_ShouldShowEmptyList_WhenNoVets() throws Exception {
            // ARRANGE
            Page<Vet> emptyPage = new PageImpl<>(List.of());
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("vets/vetList"))
                    .andExpect(model().attribute("totalItems", 0L));
        }
    }

    // Tests para GET /vets (API REST JSON)
    @Nested
    @DisplayName("GET /vets - API REST JSON")
    class ShowResourcesVetListTests {

        @Test
        @DisplayName("Debería retornar lista de veterinarios en formato JSON")
        void showResourcesVetList_ShouldReturnVetsAsJson() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet1, vet2, vet3));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Debería incluir vetList en la respuesta JSON")
        void showResourcesVetList_ShouldIncludeVetListInResponse() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet1, vet2));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList").isArray())
                    .andExpect(jsonPath("$.vetList.length()").value(2));
        }

        @Test
        @DisplayName("Debería retornar datos correctos del veterinario")
        void showResourcesVetList_ShouldReturnCorrectVetData() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet1));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList[0].id").value(1))
                    .andExpect(jsonPath("$.vetList[0].firstName").value("James"))
                    .andExpect(jsonPath("$.vetList[0].lastName").value("Carter"));
        }

        @Test
        @DisplayName("Debería incluir especialidades en la respuesta JSON")
        void showResourcesVetList_ShouldIncludeSpecialtiesInResponse() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet2));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList[0].specialties").isArray())
                    .andExpect(jsonPath("$.vetList[0].specialties[0].name").value("radiology"));
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay veterinarios")
        void showResourcesVetList_ShouldReturnEmptyList_WhenNoVets() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of());

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList").isArray())
                    .andExpect(jsonPath("$.vetList.length()").value(0));
        }

        @Test
        @DisplayName("Debería retornar veterinario con múltiples especialidades")
        void showResourcesVetList_ShouldReturnVetWithMultipleSpecialties() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet3));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList[0].specialties.length()").value(2));
        }

        @Test
        @DisplayName("Debería retornar veterinario sin especialidades con array vacío")
        void showResourcesVetList_ShouldReturnVetWithEmptySpecialties() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet1));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList[0].specialties").isArray())
                    .andExpect(jsonPath("$.vetList[0].specialties.length()").value(0));
        }

        @Test
        @DisplayName("Debería retornar todos los veterinarios sin paginación")
        void showResourcesVetList_ShouldReturnAllVetsWithoutPagination() throws Exception {
            // ARRANGE
            when(vetRepository.findAll()).thenReturn(List.of(vet1, vet2, vet3));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList.length()").value(3));
        }
    }
}
