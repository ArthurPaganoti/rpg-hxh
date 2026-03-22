package com.rpg.rpghxh.register.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpg.rpghxh.config.SecurityConfig;
import com.rpg.rpghxh.login.filter.JwtAuthenticationFilter;
import com.rpg.rpghxh.login.filter.RateLimitFilter;
import com.rpg.rpghxh.register.dto.RegisterDTO;
import com.rpg.rpghxh.register.service.RegisterService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.EmailAlreadyExistsException;
import com.rpg.rpghxh.shared.exceptions.GlobalExceptionHandler;
import com.rpg.rpghxh.shared.exceptions.NameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = RegisterController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class}
    )
)
@Import({GlobalExceptionHandler.class, RegisterControllerTest.TestSecurityConfig.class})
class RegisterControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterService registerService;

    private RegisterDTO validDTO() {
        return RegisterDTO.builder()
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("Senha@123")
                .confirmacaoSenha("Senha@123")
                .build();
    }

    @Test
    void shouldReturnSuccessWhenDataIsValid() throws Exception {
        when(registerService.register(any())).thenReturn(ResponseDTO.success("Usuário registrado com sucesso"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso"));
    }

    @Test
    void shouldReturnValidationErrorWhenNameIsBlank() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setName("");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.name").exists());
    }

    @Test
    void shouldReturnValidationErrorWhenEmailIsInvalid() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setEmail("invalid-email");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.email").exists());
    }

    @Test
    void shouldReturnValidationErrorWhenPasswordIsWeak() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setSenha("123");
        dto.setConfirmacaoSenha("123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.senha").exists());
    }

    @Test
    void shouldReturnValidationErrorWhenPasswordsDoNotMatch() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setConfirmacaoSenha("Outra@456");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.registerDTO").value("A senha e a confirmação de senha não coincidem"));
    }

    @Test
    void shouldReturnValidationErrorWhenSenhaIsBlank() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setSenha("");
        dto.setConfirmacaoSenha("");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnValidationErrorWhenConfirmacaoSenhaIsBlank() throws Exception {
        RegisterDTO dto = validDTO();
        dto.setConfirmacaoSenha("");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.confirmacaoSenha").exists());
    }

    @Test
    void shouldReturnBusinessErrorWhenEmailAlreadyExists() throws Exception {
        when(registerService.register(any())).thenThrow(new EmailAlreadyExistsException("O email 'gon@hunter.com' já está cadastrado"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("O email 'gon@hunter.com' já está cadastrado"));
    }

    @Test
    void shouldReturnBusinessErrorWhenNameAlreadyExists() throws Exception {
        when(registerService.register(any())).thenThrow(new NameAlreadyExistsException("O nome 'Gon Freecss' já está cadastrado"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("O nome 'Gon Freecss' já está cadastrado"));
    }
}
