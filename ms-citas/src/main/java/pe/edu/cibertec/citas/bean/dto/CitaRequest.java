package pe.edu.cibertec.citas.bean.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequest {

    @NotNull(message = "El paciente es obligatorio")
    @Positive(message = "El identificador del paciente debe ser mayor que cero")
    private Long pacienteId;

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
    private String especialidad;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha de la cita debe ser futura")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El código del profesional es obligatorio")
    @Size(max = 50, message = "El código del profesional no puede superar los 50 caracteres")
    private String profesionalCodigo;

    @Size(max = 250, message = "El motivo administrativo no puede superar los 250 caracteres")
    private String motivoAdministrativo;
}