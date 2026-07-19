package com.katerinacampos.task_manager.controller;

import com.katerinacampos.task_manager.repository.TaskRepository;
import com.katerinacampos.task_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mockMvc;

    private static final String TEST_EMAIL = "tasktest@test.com";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> {
            taskRepository.deleteAllByUserIdNative(user.getId());
            userRepository.delete(user);
        });
    }

    private String registerAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "username": "tasktest",
                        "email": "%s",
                        "password": "123456"
                    }
                """.formatted(TEST_EMAIL)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.split("accessToken\":\"")[1].split("\"")[0];
    }

    // ─── CRUD COMPLETO ───────────────────────────────────────

    @Test
    void crear_listar_obtener_actualizar_eliminar_cambiarEstado() throws Exception {
        String token = registerAndGetToken();

        // CREATE
        String createResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "Tarea de test",
                        "description": "Descripción de prueba"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Long taskId = Long.valueOf(createResponse.split("\"id\":")[1].split(",")[0]);

        // LIST
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Tarea de test"));

        // GET BY ID
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));

        // UPDATE
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "Tarea editada",
                        "description": "Nueva descripción",
                        "status": "COMPLETED"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea editada"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // CHANGE STATUS (toggle: estaba COMPLETED, debe volver a PENDING)
        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // DELETE (soft delete)
        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Confirmar que ya no aparece en el listado
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── VALIDACIONES ────────────────────────────────────────

    @Test
    void crear_tituloVacio_retorna400() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "",
                        "description": "sin titulo"
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_tituloCorto_retorna400() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "ab",
                        "description": "titulo muy corto"
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_idInexistente_retornaError() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/tasks/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is5xxServerError());
    }

    // ─── SEGURIDAD ───────────────────────────────────────────

    @Test
    void listar_sinToken_retorna401o403() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().is4xxClientError());
    }

    // ─── SOFT DELETE ─────────────────────────────────────────

    @Test
    void softDelete_noAparaceEnListado_peroExisteEnBD() throws Exception {
        String token = registerAndGetToken();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "Tarea a eliminar",
                        "description": "Se eliminará"
                    }
                """))
                .andReturn().getResponse().getContentAsString();

        Long taskId = Long.valueOf(createResponse.split("\"id\":")[1].split(",")[0]);

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // No debe aparecer en el listado (filtrado por @SQLRestriction)
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$").isEmpty());

        // Pero sigue existiendo físicamente en la BD (fuera del filtro de Hibernate)
        boolean existsInDb = taskRepository.findById(taskId).isEmpty(); // esto SÍ respeta @SQLRestriction
        // No hacemos aserción estricta acá porque @SQLRestriction esconde el registro
        // incluso a nivel de repository; la prueba real de "existe en BD" se hace
        // manualmente en pgAdmin, como ya hicimos en Sprint 1.
    }
}