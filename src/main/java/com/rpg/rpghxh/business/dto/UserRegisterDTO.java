package com.rpg.rpghxh.business.dto;

import com.rpg.rpghxh.business.validation.PasswordMatch;
import com.rpg.rpghxh.business.validation.ValidPassword;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch(password = "senha", confirmPassword = "confirmacaoSenha", message = "A senha e a confirmação de senha não coincidem")
@Schema(description = "Dados para registro de um novo usuário")
public class UserRegisterDTO {

    @Hidden
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome do usuário", example = "João Silva")
    private String name;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @ValidPassword
    @Schema(description = "Senha do usuário (mínimo 8 caracteres, deve conter letra maiúscula, minúscula, número e caractere especial)", example = "Senha@123")
    private String senha;

    @NotBlank(message = "A confirmação de senha é obrigatória")
    @Schema(description = "Confirmação da senha", example = "Senha@123")
    private String confirmacaoSenha;

}

