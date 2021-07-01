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
public class DatoBiometrico {
    private int identificadorDato;
    private String imagenBiometrico;
}
