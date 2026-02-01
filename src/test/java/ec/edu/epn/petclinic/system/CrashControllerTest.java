package ec.edu.epn.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletException;

/**
 * Tests de integración para CrashController usando MockMvc.
 */
@WebMvcTest(CrashController.class)
@ActiveProfiles("test")
class CrashControllerTest {

    private static final String OUPS_ENDPOINT = "/oups";
    private static final String EXPECTED_ERROR_MSG = "Expected: controller used to showcase what happens when an exception is thrown";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /oups - Debería lanzar RuntimeException")
    void triggerException_ShouldThrowRuntimeException() {
        // ARRANGE & ACT
        ServletException excepcionCapturada = assertThrows(ServletException.class,
                () -> mockMvc.perform(get(OUPS_ENDPOINT)));

        // ASSERT
        Throwable causa = excepcionCapturada.getCause();
        assertThat(causa).isInstanceOf(RuntimeException.class);
        assertThat(causa.getMessage()).contains(EXPECTED_ERROR_MSG);
    }
}
