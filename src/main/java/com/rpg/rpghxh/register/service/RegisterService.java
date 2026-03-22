package com.rpg.rpghxh.register.service;

import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.EmailAlreadyExistsException;
import com.rpg.rpghxh.shared.exceptions.NameAlreadyExistsException;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.register.dto.RegisterDTO;
import com.rpg.rpghxh.register.mapper.RegisterMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterMapper registerMapper;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder, RegisterMapper registerMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.registerMapper = registerMapper;
    }

    @Transactional
    public ResponseDTO<String> register(RegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("O email '" + dto.getEmail() + "' já está cadastrado");
        }

        if (userRepository.existsByName(dto.getName())) {
            throw new NameAlreadyExistsException("O nome '" + dto.getName() + "' já está cadastrado");
        }

        userRepository.save(registerMapper.toEntity(dto, passwordEncoder.encode(dto.getSenha())));

        return ResponseDTO.success("Usuário registrado com sucesso");
    }
}
