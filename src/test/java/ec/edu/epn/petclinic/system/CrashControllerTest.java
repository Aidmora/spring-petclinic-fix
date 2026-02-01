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

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /oups - Debería lanzar RuntimeException")
    void triggerException_ShouldThrowRuntimeException() {
        // ARRANGE & ACT
        // El controlador lanza RuntimeException, que se envuelve en ServletException
        ServletException exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/oups")));

        // ASSERT
        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCause().getMessage())
                .contains("Expected: controller used to showcase what happens when an exception is thrown");
    }
}
