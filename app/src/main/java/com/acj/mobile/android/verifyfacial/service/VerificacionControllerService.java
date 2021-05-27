package com.acj.mobile.android.verifyfacial.service;

import com.acj.mobile.android.verifyfacial.model.RequestRegister;
import com.acj.mobile.android.verifyfacial.model.RequestVerify;
import com.acj.mobile.android.verifyfacial.model.ResponseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VerificacionControllerService {


    String API_ROUTE = "api/verificacion-facial";

    @GET(API_ROUTE)
    Call<List<ResponseService>> getAll();

    @GET(API_ROUTE + "/{id}")
    Call<ResponseService> getOne(@Path("id") Integer id);

    @POST(API_ROUTE)
    Call<ResponseService> addOne(@Body RequestRegister requestRegister);

    @DELETE(API_ROUTE)
    Call<ResponseService> deleteAll();

    @POST(API_ROUTE + "/verificacionFacial")
    Call<ResponseService> verificacionFacial(@Body RequestVerify requestVerify);

}
