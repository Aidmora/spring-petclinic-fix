package ec.edu.epn.petclinic.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Tests de integración para WelcomeController usando MockMvc.
 */
@WebMvcTest(WelcomeController.class)
@ActiveProfiles("test")
class WelcomeControllerTest {

    private static final String ROOT_PATH = "/";
    private static final String WELCOME_VIEW = "welcome";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / - Debería retornar vista 'welcome' con status 200")
    void welcome_ShouldReturnWelcomeView() throws Exception {
        // ARRANGE

        // ACT & ASSERT
        ResultActions resultado = mockMvc.perform(get(ROOT_PATH));

        resultado.andExpect(status().isOk())
                .andExpect(view().name(WELCOME_VIEW));
    }
}
