package pe.edu.cibertec.citas.bean.entity;

import java.time.LocalDateTime;

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
@Table("cita")
public class Cita {

    @Id
    private Long id;

    @Column("codigo_cita")
    private String codigoCita;

    @Column("paciente_id")
    private Long pacienteId;

    @Column("especialidad")
    private String especialidad;

    @Column("fecha_hora")
    private LocalDateTime fechaHora;

    @Column("profesional_codigo")
    private String profesionalCodigo;

    @Column("motivo_administrativo")
    private String motivoAdministrativo;

    @Column("estado")
    private String estado;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column("ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
}