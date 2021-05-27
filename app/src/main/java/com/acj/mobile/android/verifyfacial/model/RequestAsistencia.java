package com.acj.mobile.android.verifyfacial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequestAsistencia {
    private int tipoOperacion;
    private int tipoVerificacion;
    private String ipCliente;
    private String macCliente;
    private String nombreCompletoUsuario;
    private int tipoDocumento;
    private String numeroDocumento;
    private String numeroSerieDispositivo;
    private int identificadorDato;
    private DatoBiometrico datoBiometrico = new DatoBiometrico();
}
