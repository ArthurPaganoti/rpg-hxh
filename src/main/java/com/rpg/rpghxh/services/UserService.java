package com.rpg.rpghxh.services;

import com.rpg.rpghxh.business.dto.ResponseDTO;
import com.rpg.rpghxh.business.dto.UserRegisterDTO;
import com.rpg.rpghxh.entities.User;
import com.rpg.rpghxh.exceptions.EmailAlreadyExistsException;
import com.rpg.rpghxh.exceptions.NameAlreadyExistsException;
import com.rpg.rpghxh.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ResponseDTO<String> register(UserRegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("O email '" + dto.getEmail() + "' já está cadastrado");
        }

        if (userRepository.existsByName(dto.getName())) {
            throw new NameAlreadyExistsException("O nome '" + dto.getName() + "' já está cadastrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setSenha(passwordEncoder.encode(dto.getSenha()));

        userRepository.save(user);

        return ResponseDTO.success("Usuário registrado com sucesso");
    }
}