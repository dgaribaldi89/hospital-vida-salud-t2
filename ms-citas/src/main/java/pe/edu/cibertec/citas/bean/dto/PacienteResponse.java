package pe.edu.cibertec.citas.bean.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    private Long id;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private LocalDate fechaNacimiento;

    private String telefono;

    private String correo;

    private String direccion;

    private LocalDate fechaRegistro;

    private String estado;
}