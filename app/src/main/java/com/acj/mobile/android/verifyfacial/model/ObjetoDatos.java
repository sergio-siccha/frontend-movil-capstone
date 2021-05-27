package com.acj.mobile.android.verifyfacial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ObjetoDatos {
    private String numero;
    private String nombres;
    private String apellido_paterno;
    private String apellido_materno;
    private String sexo;
    private String fecha_nacimiento;
    private String codigo_verificacion;
    private String nombre_completo;
}
