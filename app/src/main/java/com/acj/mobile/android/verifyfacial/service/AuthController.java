package com.acj.mobile.android.verifyfacial.service;

import com.acj.mobile.android.verifyfacial.model.AutenticacionBody;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.model.RequestRekognition;
import com.acj.mobile.android.verifyfacial.model.ResponseRekognition;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthController {


    String API_ROUTE = "v1/usuario";

    /*@POST(API_ROUTE + "/compareFaces")
    Call<ResponseRekognition> verificacionFacial(@Body RequestRekognition requestVerify, @Header("Authorization") String token);*/

    @POST(API_ROUTE + "/auth/autenticacion")
    Call<GenericalResponse> autenticacion(@Body AutenticacionBody autenticacionBody);
}
