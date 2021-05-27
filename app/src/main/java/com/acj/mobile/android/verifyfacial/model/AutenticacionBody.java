package com.acj.mobile.android.verifyfacial.model;

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
public class AutenticacionBody {
    private String numeroDocumento;
    private String clave;
}
