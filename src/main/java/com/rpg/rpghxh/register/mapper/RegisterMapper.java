package com.rpg.rpghxh.register.mapper;

import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.register.dto.RegisterDTO;
import org.springframework.stereotype.Component;

@Component
public class RegisterMapper {

    public User toEntity(RegisterDTO dto, String encodedPassword) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .senha(encodedPassword)
                .build();
    }
}
