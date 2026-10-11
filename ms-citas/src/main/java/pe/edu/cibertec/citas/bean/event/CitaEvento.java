package pe.edu.cibertec.citas.bean.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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