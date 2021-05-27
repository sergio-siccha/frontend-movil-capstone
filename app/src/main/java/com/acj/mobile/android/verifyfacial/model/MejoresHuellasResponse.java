package com.acj.mobile.android.verifyfacial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MejoresHuellasResponse {
    private String identificadorDato;
    private String descripcionDato;
    private boolean tieneTemplate;
}
