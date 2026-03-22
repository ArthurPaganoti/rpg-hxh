package com.rpg.rpghxh.register.mapper;

import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.register.dto.RegisterDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterMapperTest {

    private final RegisterMapper mapper = new RegisterMapper();

    @Test
    void shouldMapDtoToEntityWithEncodedPassword() {
        RegisterDTO dto = RegisterDTO.builder()
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("Senha@123")
                .confirmacaoSenha("Senha@123")
                .build();

        User user = mapper.toEntity(dto, "encodedPassword");

        assertEquals("Gon Freecss", user.getName());
        assertEquals("gon@hunter.com", user.getEmail());
        assertEquals("encodedPassword", user.getSenha());
        assertNull(user.getId());
    }
}
