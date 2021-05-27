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
public class RequestVerify {

  /*  correo	string
    Correo a verificar que existe
    foto	string($byte)
    Foto a validar con la base de datos*/

private String correo;
private String foto;
}
