package br.com.aera.backend.alunos.dto;

import br.com.aera.backend.alunos.domain.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

/**
 * Atualizacao parcial. Campos nulos representam ausencia de alteracao.
 *
 * <p>Documentos e dados do responsavel sao opcionais. Os limites de tamanho
 * acompanham as colunas da entidade; os campos do Forms sao provisorios.
 * Este contrato nao distingue campo omitido de null explicito.
 * Texto opcional vazio limpa o valor. Para limpar explicitamente um campo,
 * inclusive startDate, envie seu nome em clearFields (ex.: ["phone", "startDate"]).
 * Campos obrigatorios/tecnicos e nomes desconhecidos sao rejeitados pelo service.
 * Nao informe um valor e clearFields para o mesmo campo.</p>
 */
public record StudentUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "O nome completo nao pode estar em branco.")
        @Size(max = 255, message = "O campo fullName deve ter no maximo 255 caracteres.")
        String fullName,

        @PastOrPresent(message = "A data de nascimento nao pode estar no futuro.")
        LocalDate birthDate,

        @Size(max = 50, message = "O campo sex deve ter no maximo 50 caracteres.")
        String sex,

        @Size(max = 40, message = "O campo cpf deve ter no maximo 40 caracteres.")
        String cpf,

        @Size(max = 40, message = "O campo rg deve ter no maximo 40 caracteres.")
        String rg,

        @Size(max = 255, message = "O campo address deve ter no maximo 255 caracteres.")
        String address,

        @Size(max = 255, message = "O campo neighborhood deve ter no maximo 255 caracteres.")
        String neighborhood,

        @Size(max = 255, message = "O campo city deve ter no maximo 255 caracteres.")
        String city,

        @Size(max = 20, message = "O campo zipCode deve ter no maximo 20 caracteres.")
        String zipCode,

        @Size(max = 30, message = "O campo phone deve ter no maximo 30 caracteres.")
        String phone,

        @Size(max = 30, message = "O campo whatsapp deve ter no maximo 30 caracteres.")
        String whatsapp,

        @Email(message = "O e-mail deve ter um formato valido.")
        @Size(max = 255, message = "O campo email deve ter no maximo 255 caracteres.")
        String email,

        @Size(max = 255, message = "O campo guardianName deve ter no maximo 255 caracteres.")
        String guardianName,

        @Size(max = 40, message = "O campo guardianCpf deve ter no maximo 40 caracteres.")
        String guardianCpf,

        @Size(max = 30, message = "O campo guardianPhone deve ter no maximo 30 caracteres.")
        String guardianPhone,

        @Size(max = 30, message = "O campo guardianWhatsapp deve ter no maximo 30 caracteres.")
        String guardianWhatsapp,

        @Size(max = 100, message = "O campo relationship deve ter no maximo 100 caracteres.")
        String relationship,

        @Size(max = 100, message = "O campo modality deve ter no maximo 100 caracteres.")
        String modality,

        @Size(max = 50, message = "O campo level deve ter no maximo 50 caracteres.")
        String level,

        @Size(max = 100, message = "O campo classGroup deve ter no maximo 100 caracteres.")
        String classGroup,

        LocalDate startDate,

        StudentStatus status,

        @Size(max = 2000, message = "O campo notes deve ter no maximo 2000 caracteres.")
        String notes,

        @Size(max = 2000, message = "O campo importantInformation deve ter no maximo 2000 caracteres.")
        String importantInformation,

        @Size(max = 21, message = "No maximo 21 campos opcionais podem ser limpos.")
        Set<@NotBlank(message = "O nome do campo a limpar nao pode estar em branco.") String> clearFields
) {
}
