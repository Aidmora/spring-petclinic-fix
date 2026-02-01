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

    private Vet jamesCarter;
    private Vet helenLeary;
    private Vet lindaDouglas;
    private Specialty especialidadRadiologia;
    private Specialty especialidadCirugia;

    @BeforeEach
    void setUp() {
        especialidadRadiologia = new Specialty();
        especialidadRadiologia.setId(1);
        especialidadRadiologia.setName("radiology");

        especialidadCirugia = new Specialty();
        especialidadCirugia.setId(2);
        especialidadCirugia.setName("surgery");

        jamesCarter = new Vet();
        jamesCarter.setId(1);
        jamesCarter.setFirstName("James");
        jamesCarter.setLastName("Carter");

        helenLeary = new Vet();
        helenLeary.setId(2);
        helenLeary.setFirstName("Helen");
        helenLeary.setLastName("Leary");
        helenLeary.addSpecialty(especialidadRadiologia);

        lindaDouglas = new Vet();
        lindaDouglas.setId(3);
        lindaDouglas.setFirstName("Linda");
        lindaDouglas.setLastName("Douglas");
        lindaDouglas.addSpecialty(especialidadCirugia);
        lindaDouglas.addSpecialty(especialidadRadiologia);
    }

    // Tests para GET /vets.html (Vista HTML)

    @Nested
    @DisplayName("GET /vets.html - Vista HTML paginada")
    class ShowVetListHtmlTests {

        @Test
        @DisplayName("Debería mostrar lista de veterinarios con paginación")
        void showVetList_ShouldShowVetListWithPagination() throws Exception {
            // ARRANGE
            Page<Vet> paginaVeterinarios = new PageImpl<>(
                    List.of(jamesCarter, helenLeary, lindaDouglas),
                    PageRequest.of(0, 5),
                    3);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(paginaVeterinarios);

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
            Page<Vet> paginaInicial = new PageImpl<>(
                    List.of(jamesCarter, helenLeary, lindaDouglas),
                    PageRequest.of(0, 5),
                    3);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(paginaInicial);

            // ACT & ASSERT
            mockMvc.perform(get("/vets.html"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));
        }

        @Test
        @DisplayName("Debería manejar parámetro de página correctamente")
        void showVetList_ShouldHandlePageParameter() throws Exception {
            // ARRANGE
            Page<Vet> segundaPagina = new PageImpl<>(
                    List.of(jamesCarter),
                    PageRequest.of(1, 5),
                    6);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(segundaPagina);

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
            Page<Vet> paginaConDoceElementos = new PageImpl<>(
                    List.of(jamesCarter, helenLeary, lindaDouglas),
                    PageRequest.of(0, 5),
                    12);
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(paginaConDoceElementos);

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
            Page<Vet> sinVeterinarios = new PageImpl<>(List.of());
            when(vetRepository.findAll(any(Pageable.class))).thenReturn(sinVeterinarios);

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
            when(vetRepository.findAll()).thenReturn(List.of(jamesCarter, helenLeary, lindaDouglas));

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
            when(vetRepository.findAll()).thenReturn(List.of(jamesCarter, helenLeary));

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
            when(vetRepository.findAll()).thenReturn(List.of(jamesCarter));

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
            when(vetRepository.findAll()).thenReturn(List.of(helenLeary));

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
            when(vetRepository.findAll()).thenReturn(List.of(lindaDouglas));

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
            when(vetRepository.findAll()).thenReturn(List.of(jamesCarter));

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
            when(vetRepository.findAll()).thenReturn(List.of(jamesCarter, helenLeary, lindaDouglas));

            // ACT & ASSERT
            mockMvc.perform(get("/vets")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vetList.length()").value(3));
        }
    }
}
