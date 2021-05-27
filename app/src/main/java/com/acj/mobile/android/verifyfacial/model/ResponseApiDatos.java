package com.acj.mobile.android.verifyfacial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseApiDatos {
    private boolean success;
    private int origen;
    private ObjetoDatos data;
}
