package com.acj.mobile.android.verifyfacial.service;

import com.acj.mobile.android.verifyfacial.model.AutenticacionBody;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.model.RequestAsistencia;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DatoBiometricoController {


    String API_ROUTE = "v1/datoBiometrico";

    /*@POST(API_ROUTE + "/compareFaces")
    Call<ResponseRekognition> verificacionFacial(@Body RequestRekognition requestVerify, @Header("Authorization") String token);*/

    @GET(API_ROUTE + "/getMejoresHuellas/{tipoDocumento}/{numeroDocumento}")
    Call<GenericalResponse> getMejoresHuellas(@Path("tipoDocumento") int tipoDocumento, @Path("numeroDocumento") String numeroDocumento);

    @POST(API_ROUTE + "/asistencia/iniciar")
    Call<GenericalResponse> marcarAsistencia(@Body RequestAsistencia body);

    @GET(API_ROUTE + "/getStatus/{numeroDocumento}")
    Call<GenericalResponse> getStatus(@Path("numeroDocumento") String numeroDocumento);
}
