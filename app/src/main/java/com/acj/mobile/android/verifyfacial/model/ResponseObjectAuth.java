package com.acj.mobile.android.verifyfacial.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResponseObjectAuth {
    private String token;
    private int id;
    private String username;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private int genero;
}
