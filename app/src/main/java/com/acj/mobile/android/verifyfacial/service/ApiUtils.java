package com.acj.mobile.android.verifyfacial.service;

import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {
    public static final String BASE_URL = "http://192.168.1.224:9895/";

    private static Retrofit retrofitLocal = null;

    private static OkHttpClient httpClientLocal = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Chain chain) throws IOException {
                    Request original = chain.request();

                    String token = GlobalConfig.getInstance().getTokenAuth();

                    Request request = original.newBuilder()
                            .header("Authorization", token)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }
            })
            .build();


    public static Retrofit getApi() {
        if (retrofitLocal == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
            retrofitLocal = builder
                    .client(httpClientLocal)
                    .build();
        }

        return retrofitLocal;
    }
}
