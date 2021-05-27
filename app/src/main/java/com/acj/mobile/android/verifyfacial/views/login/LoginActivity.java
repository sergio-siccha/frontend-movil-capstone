package com.acj.mobile.android.verifyfacial.views.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


import com.acj.mobile.android.verifyfacial.MainActivity;
import com.acj.mobile.android.verifyfacial.R;
import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;
import com.acj.mobile.android.verifyfacial.model.AutenticacionBody;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.model.ResponseObjectAuth;
import com.acj.mobile.android.verifyfacial.service.AuthController;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.Objects;
import com.acj.mobile.android.verifyfacial.service.ApiUtils;
import com.google.gson.reflect.TypeToken;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usuarioBiometria;
    private TextInputEditText contraseniaBiometria;
    private TextInputLayout usuarioBiometriaLayout;
    private TextInputLayout contraseniaBiometriaLayout;
    private Button btnIngresarLogin;


    private String documento = "DNI";
    private boolean validUsuario;
    private boolean validPass;
    private int intentoHome = 0;
    private Context mContext;

    private final static String TAG = "------Login Validation";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuarioBiometria = (TextInputEditText) findViewById(R.id.usuarioBiometria);
        contraseniaBiometria = (TextInputEditText) findViewById(R.id.contraseniaBiometria);
        usuarioBiometriaLayout = (TextInputLayout) findViewById(R.id.usuarioBiometriaLayout);
        contraseniaBiometriaLayout = (TextInputLayout) findViewById(R.id.contraseniaBiometriaLayout);
        btnIngresarLogin = (Button) findViewById(R.id.btn_ingresar_login);

        mContext = this;

        btnIngresarLogin.setEnabled(false);

        btnIngresarLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingresar();
            }
        });

        contraseniaBiometria.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ingresar();
                    return true;
                }
                return false;
            }
        });

        usuarioBiometria.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 8) {
                    btnIngresarLogin.setEnabled(true);
                    btnIngresarLogin.setClickable(true);
                    btnIngresarLogin.setBackground(getResources().getDrawable(R.drawable.btn_login));
                }else{
                    btnIngresarLogin.setBackground(getResources().getDrawable(R.drawable.btn_login_disabled));
                    btnIngresarLogin.setEnabled(false);
                    btnIngresarLogin.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void ingresar() {
        validUsuario = false;
        validPass = false;

        String documento = String.valueOf(usuarioBiometria.getText());
        String password = String.valueOf(contraseniaBiometria.getText());

        validaciones();

        Log.i(TAG, "DATOS DE USUARIO: " + documento + " " + password);
        if (validUsuario && validPass && intentoHome == 0) {
            intentoHome++;

            AutenticacionBody autenticacionUsuario = new AutenticacionBody();
            autenticacionUsuario.setNumeroDocumento(documento);
            autenticacionUsuario.setClave(password);

            AuthController usuarioService = ApiUtils.getApi().create(AuthController.class);
            Call<GenericalResponse> auth = usuarioService.autenticacion(autenticacionUsuario);
            auth.enqueue(new Callback<GenericalResponse>() {
                @Override
                public void onResponse(Call<GenericalResponse> call, retrofit2.Response<GenericalResponse> response) {
                    if(response.isSuccessful()) {
                        Log.i(TAG, "----------------- SUCCESSFULL -----------------");
                        Log.i(TAG, "            Se obtuvo una respuesta ");
                        Log.i(TAG, response.body().toString());
                        Log.i(TAG, "----------------- SUCCESSFULL -----------------");
                        if(response.body() != null && response.body().getCodigoRespuesta().equals("150")) {
                            //Llena GlobalConfig
                            GsonBuilder gson = new GsonBuilder();
                            Type type = new TypeToken<ResponseObjectAuth>() {
                            }.getType();
                            String json = gson.create().toJson(response.body().getObjeto());
                            ResponseObjectAuth object = gson.create().fromJson(json, type);

                            GlobalConfig.getInstance().setResponseAuth(object);
                            GlobalConfig.getInstance().setTokenAuth("Bearer " + object.getToken());

                            Intent in = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(in);
                            finish();
                        } else {
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setConfirmText("OK");
                            a.setContentText(response.body().getDescRespuesta());
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    contraseniaBiometria.setText("");
                                }
                            });
                            a.show();
                            intentoHome = 0;
                        }
                    }
                }

                @Override
                public void onFailure(Call<GenericalResponse> call, Throwable t) {
                    SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                    a.setCancelable(false);
                    a.setCanceledOnTouchOutside(false);
                    a.setConfirmText("OK");
                    a.setContentText("Fallo la respuesta del servidor");
                    a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            contraseniaBiometria.setText("");
                        }
                    });
                    a.show();
                }
            });
        }
    }

    public void validaciones() {
        usuarioBiometriaLayout.setError(null);
        contraseniaBiometriaLayout.setError(null);
        if (Objects.requireNonNull(usuarioBiometria.getText()).toString().isEmpty()) {
            validUsuario = false;
            usuarioBiometriaLayout.setError("El campo es obligatorio");
        } else {
            if (documento.equals("DNI") && usuarioBiometria.getText().toString().length() != 8) {
                validUsuario = false;
                usuarioBiometriaLayout.setError("Longitud deben ser 8 números.");
            } else if (documento.equals("DNI") && usuarioBiometria.getText().toString().length() == 8) {
                validUsuario = true;
            }

            if (documento.equals("Carnet de Extranjeria") && usuarioBiometria.getText().toString().length() != 9) {
                validUsuario = false;
                usuarioBiometriaLayout.setError("Longitud deben ser 9 números.");
            } else if (documento.equals("Carnet de Extranjeria") && usuarioBiometria.getText().toString().length() == 9) {
                validUsuario = true;
            }
        }

        if (contraseniaBiometria.getText().toString().isEmpty()) {
            validPass = false;
            contraseniaBiometriaLayout.setError("El campo es obligatorio");
        } else {
            validPass = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentoHome = 0;
    }


}
