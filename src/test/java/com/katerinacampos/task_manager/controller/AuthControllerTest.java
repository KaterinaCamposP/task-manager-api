package com.katerinacampos.task_manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@ActiveProfiles("test")
public class AuthControllerTest {

@Autowired
private WebApplicationContext context;

@Autowired
private com.katerinacampos.task_manager.repository.UserRepository userRepository;

@Autowired
private com.katerinacampos.task_manager.repository.TaskRepository taskRepository;

private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.findByEmail("katerina@test.com").ifPresent(user -> {
            taskRepository.deleteAllByUserIdNative(user.getId());
            userRepository.delete(user);
        });
        userRepository.findByEmail("duplicado@test.com").ifPresent(user -> {
            taskRepository.deleteAllByUserIdNative(user.getId());
            userRepository.delete(user);
        });
    }

    // ─── REGISTER ────────────────────────────────────────────

    @Test
    void register_exitoso() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "katerina",
                                "email": "katerina@test.com",
                                "password": "123456"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_emailDuplicado() throws Exception {
        String body = """
                    {
                        "username": "katerina",
                        "email": "duplicado@test.com",
                        "password": "123456"
                    }
                """;
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordCorto() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "katerina",
                                "email": "katerina@test.com",
                                "password": "123"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_emailInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "katerina",
                                "email": "no-es-un-email",
                                "password": "123456"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_usernameVacio() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "",
                                "email": "katerina@test.com",
                                "password": "123456"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ─── LOGIN ───────────────────────────────────────────────

    @Test
    void login_exitoso() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "katerina",
                                "email": "katerina@test.com",
                                "password": "123456"
                            }
                        """));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "email": "katerina@test.com",
                                "password": "123456"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_credencialesIncorrectas() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "email": "noexiste@test.com",
                                "password": "wrongpass"
                            }
                        """))
                .andExpect(status().is4xxClientError());
    }

    // ─── PROFILE ─────────────────────────────────────────────

    @Test
    void profile_sinToken_retorna401o403() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void profile_conTokenValido() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username": "katerina",
                                "email": "katerina@test.com",
                                "password": "123456"
                            }
                        """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = response.split("accessToken\":\"")[1].split("\"")[0];

        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("katerina@test.com"))
                .andExpect(jsonPath("$.username").value("katerina"));
    }
}