package pe.edu.cibertec.pacientes.bean.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("paciente")
public class Paciente {

    @Id
    private Long id;

    @Column("tipo_documento")
    private String tipoDocumento;

    @Column("numero_documento")
    private String numeroDocumento;

    @Column("nombres")
    private String nombres;

    @Column("apellidos")
    private String apellidos;

    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column("telefono")
    private String telefono;

    @Column("correo")
    private String correo;

    @Column("direccion")
    private String direccion;

    @Column("fecha_registro")
    private LocalDate fechaRegistro;

    @Column("estado")
    private String estado;
}