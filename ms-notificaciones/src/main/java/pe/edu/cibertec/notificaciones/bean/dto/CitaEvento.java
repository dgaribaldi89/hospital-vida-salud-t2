package pe.edu.cibertec.notificaciones.bean.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaEvento {

    private Long citaId;

    private String codigoCita;

    private Long pacienteId;

    private String especialidad;

    private LocalDateTime fechaHora;

    private String profesionalCodigo;

    private String estado;

    private LocalDateTime fechaEvento;
}