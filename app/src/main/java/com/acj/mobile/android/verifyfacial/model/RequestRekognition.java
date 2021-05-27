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
public class RequestRekognition {
    private String primera_imagen;
    private String segunda_imagen;
}
