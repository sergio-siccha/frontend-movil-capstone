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
public class RequestRegister {
/*    correo	string
    Correo electrónico de la verificación facial.

    datosBiometrico	string($byte)
    Datos biométrico de la verificación facial.

    fecha	string($date-time)
    Fecha de registro de la verificación facial.

            id	integer($int32)
    Identificador de la verificación facial.*/

private String id;
private String correo;
private String datosBiometrico;
private String fecha;


}
