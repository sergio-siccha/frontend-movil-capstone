package com.acj.mobile.android.verifyfacial.views.VerificacionDactilar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.acj.mobile.android.verifyfacial.R;
import com.acj.mobile.android.verifyfacial.functions.EikonGlobal;
import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;
import com.acj.mobile.android.verifyfacial.functions.Globals;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.model.MejoresHuellasResponse;
import com.acj.mobile.android.verifyfacial.model.RequestAsistencia;
import com.acj.mobile.android.verifyfacial.service.ApiUtils;
import com.acj.mobile.android.verifyfacial.service.DatoBiometricoController;
import com.acj.mobile.android.verifyfacial.utils.Util;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VerificarIdentidadActivityDP extends AppCompatActivity {

    //    private TextView textDedoIzquierdo;
//    private TextView textDedoDerecho;
    private TextView textDNIVerificarIdentidad;
    private TextView textDedosHabilitados, textDedosHabilitados2, textverificarDactilar, textView14;
    private ImageView fingerprintImageView;
    private View circulo1, viewDedoDerecho;
    private View circulo2, intento, viewDedoIzquiedo;
    private Button salir;
    private Button capturar;
    private Context mContext;
    private CheckBox dedoInhabilitado;
    private int quality;
    private byte[] huellaRest;
    private static final String TAG = "ReniecEikonActivity";
    private static final Integer tipoDocuemento = 1;

/*    private String nombres;
    private String paterno;
    private String materno;*/

    private String nombreCompleto;
    private String numeroDocumento;
    private String tokenDatos;
    private String huella1;
    private String huella2;
    private AlertDialog dialogVerificandoHuellas;
    private AlertDialog dialogHuellasRegistradas;
    private AlertDialog dialogColocarDedo;
    private int numeroSerieDispositivo;
    private boolean m_reset = false;
    private Bitmap m_bitmap = null;
    private EikonGlobal eikonGlobal;
    private ReaderCollection readers;
    private byte[] currentFingerPrint;
    private Fmd m_fmd = null;
    private int intentos;
    private int checkboxMarcado;
    private String dedoDerecho;
    private String dedoIzquierdo;
    private String textdedoDerecho;
    private String textdedoIzquierdo;
    private int intentoCaptura;

    private RequestAsistencia requestAsistencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion_dactilar_dp);

        salir = (Button) findViewById(R.id.btn_salir_al_dni);
        capturar = (Button) findViewById(R.id.btn_Capturar);
        textDNIVerificarIdentidad = (TextView) findViewById(R.id.textDNIVerificarIdentidad);
        textDedosHabilitados = (TextView) findViewById(R.id.textDedosHabilitados);
        textDedosHabilitados2 = (TextView) findViewById(R.id.textDedosHabilitados2);
        textverificarDactilar = (TextView) findViewById(R.id.textverificarDactilar);
        fingerprintImageView = (ImageView) findViewById(R.id.fingerprintImageView);
        textView14 = (TextView) findViewById(R.id.textView14);
        circulo1 = (View) findViewById(R.id.intento1);
        circulo2 = (View) findViewById(R.id.intento2);
        intento = (View) findViewById(R.id.intento);
        viewDedoDerecho = (View) findViewById(R.id.dedoDerecho);
        viewDedoIzquiedo = (View) findViewById(R.id.dedoIzquierdo);
        dedoInhabilitado = (CheckBox) findViewById(R.id.dedoInhabilitado);


        mContext = this;
        numeroDocumento = GlobalConfig.getInstance().getResponseAuth().getUsername();

        /* REQUEST DE ASISTENCIA*/
        int tipoOperacion = getIntent().getExtras().getInt("tipoOperacion", 0);

        requestAsistencia = new RequestAsistencia();
        requestAsistencia.setTipoOperacion(tipoOperacion);
        requestAsistencia.setTipoVerificacion(1); // Verificacion tipo 2 es facial
        requestAsistencia.setTipoDocumento(1);
        requestAsistencia.setNumeroDocumento(GlobalConfig.getInstance().getResponseAuth().getUsername());
        requestAsistencia.setNombreCompletoUsuario(GlobalConfig.getInstance().getResponseAuth().getNombre() + " " +
									GlobalConfig.getInstance().getResponseAuth().getApellidoPaterno() + " " +
									GlobalConfig.getInstance().getResponseAuth().getApellidoMaterno());

        quality = 0;
        huella1 = "";
        huella2 = "";
        numeroSerieDispositivo = 0;
        intentos = 0;
        dedoInhabilitado.setChecked(false);

        textDNIVerificarIdentidad.setText("DNI : " + numeroDocumento);

        nombreCompleto = "null null null";

        intentoCaptura = 0;


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();

//        dialogVerificandoHuellas alert de verificando huellas
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);// if you want user to wait for some process to finish,
        builder.setView(R.layout.progress_dialog_alert);
        dialogVerificandoHuellas = builder.create();

        AlertDialog.Builder builderHr = new AlertDialog.Builder(this);
        builder.setCancelable(false);// if you want user to wait for some process to finish,
        builder.setView(R.layout.progress_da_huellas_registradas);
        dialogHuellasRegistradas = builder.create();

        pintarHuellas();

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setCancelable(false);// if you want user to wait for some process to finish,
        builder2.setView(R.layout.dialog_colocar_dedo_reniec);
        dialogColocarDedo = builder2.create();


        capturar.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View v) {
                readers = Globals.getInstance().getReaders(mContext);
                int c = readers.size();
                Log.i(TAG, "recapturarHuellaEikon:  " + c);
                if (c >= 1 && intentoCaptura == 0) {
                    intentoCaptura++;
                    dialogColocarDedo.show();
                    new HiloCaptura().start();
                    dedoInhabilitado.setVisibility(View.INVISIBLE);
                    textDedosHabilitados.setVisibility(View.GONE);
                    if (GlobalConfig.getInstance().getIntentosXManos() == 1) {
                        textverificarDactilar.setTextColor(getResources().getColor(R.color.Black));
                        textverificarDactilar.setText(Html.fromHtml("Coloque el " + "<b>" + textdedoDerecho + "</b>" + " sobre el lector de huella"));
                    } else if (GlobalConfig.getInstance().getIntentosXManos() == 2) {
                        textverificarDactilar.setTextColor(getResources().getColor(R.color.Black));
                        textverificarDactilar.setText(Html.fromHtml("Coloque el " + "<b>" + textdedoIzquierdo + "</b>" + " sobre el lector de huella"));

                    }
                } else if(c < 1) {
                    SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                    a.setCancelable(false);
                    a.setCanceledOnTouchOutside(false);
                    a.setTitleText("EIKON Fingerprint SDK");
                    a.setConfirmText("OK");
                    a.setConfirmButtonTextColor(Color.WHITE);
                    a.setConfirmButtonBackgroundColor(Color.RED);
                    a.setContentText("¡La inicialización del scanner" +
                            " de huellas digitales ha fallado!" + "\n" +
                            "Conecte el scanner a su dispositivo");
                    a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            m_reset = true;
                            finish();
                        }
                    });
                    a.show();
                }
            }
        });
    }

    void pintarHuellas() {
        if (GlobalConfig.getInstance().getIntentosXManos() == 1) {
            dialogHuellasRegistradas.setTitle("Obteniendo Huella Disponible");
             dialogHuellasRegistradas.show();
        }else if(GlobalConfig.getInstance().getIntentosXManos() == 2){
            dialogHuellasRegistradas.setTitle("Obteniendo Huella Alterna");
            dialogHuellasRegistradas.show();
        }
        DatoBiometricoController mejoresHuellasService = ApiUtils.getApi().create(DatoBiometricoController.class);
        Call<GenericalResponse> call = mejoresHuellasService.getMejoresHuellas(1, numeroDocumento);
        call.enqueue(new Callback<GenericalResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<GenericalResponse> call, Response<GenericalResponse> response) {
                if (response.isSuccessful()) {
                    GenericalResponse responseHuellas = response.body();

                    GsonBuilder gson = new GsonBuilder();
                    Type type = new TypeToken<List<MejoresHuellasResponse>>() {
                    }.getType();
                    String json = gson.create().toJson(responseHuellas.getObjeto());
                    List<MejoresHuellasResponse> object = gson.create().fromJson(json, type);
                    Log.i("SERGIO =====  ", "---------ESTOY EN ON RESPONSE------------");
                    if (responseHuellas.getCodigoRespuesta().equals("00")) {
                        Log.i("SERGIO =====  ", "---------ESTOY EN ON RESPONSE------------ " + responseHuellas.getObjeto());
                        dedoDerecho = object.get(0).getIdentificadorDato();
                        dedoIzquierdo = object.get(1).getDescripcionDato();
                        textdedoDerecho = object.get(0).getDescripcionDato();
                        textdedoIzquierdo = object.get(1).getDescripcionDato();

                        if (GlobalConfig.getInstance().getIntentosXManos() == 1) {

                            circulo1.setBackground(getDrawable(R.drawable.bola_gris));
                            circulo2.setBackground(getDrawable(R.drawable.bola_gris));

                            switch (dedoDerecho) {
                                case "01":
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_1));
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_manoizquierda));
                                    break;
                                case "02":
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_2));
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_manoizquierda));
                                    break;
                                case "03":
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_3));
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_manoizquierda));
                                    break;
                                case "04":
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_4));
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_manoizquierda));
                                    break;
                                case "05":
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_5));
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_manoizquierda));
                                    break;
                            }
                        } else if (GlobalConfig.getInstance().getIntentosXManos() == 2) {
//                            textverificarDactilar.setText("Coloque el " + textdedoIzquierdo + " sobre el lector de huella");
                            circulo1.setBackground(getDrawable(R.drawable.bola_gris));
                            circulo2.setBackground(getDrawable(R.drawable.bola_gris));
                            switch (dedoIzquierdo) {
                                case "06":
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_6));
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_manoderecha));
                                    break;
                                case "07":
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_7));
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_manoderecha));
                                    break;
                                case "08":
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_8));
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_manoderecha));
                                    break;
                                case "09":
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_9));
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_manoderecha));
                                    break;
                                case "10":
                                    viewDedoIzquiedo.setBackground(getDrawable(R.drawable.ic_10));
                                    viewDedoDerecho.setBackground(getDrawable(R.drawable.ic_manoderecha));
                                    break;
                            }
                        }
                        textDNIVerificarIdentidad.setTextColor(getResources().getColor(R.color.Black));

                       try {
                           activarHuellero();
                       }catch (Exception e){
                           finish();
                       }

                        Log.i("SERGIO ", "CAPTURANDO HUELLAS" +
                                "Coloque cualquiera de los dedos habilitados sobre el lector de huellas.");

                    } else {

                        dialogHuellasRegistradas.dismiss();
                        salir.setVisibility(View.VISIBLE);
                        circulo1.setVisibility(View.GONE);
                        circulo2.setVisibility(View.GONE);
                        textView14.setVisibility(View.INVISIBLE);
                        intento.setVisibility(View.INVISIBLE);
                        textDedosHabilitados2.setVisibility(View.INVISIBLE);
                        viewDedoDerecho.setVisibility(View.INVISIBLE);
                        viewDedoDerecho.setVisibility(View.INVISIBLE);
                        textDedosHabilitados.setVisibility(View.INVISIBLE);
                        salir.setVisibility(View.INVISIBLE);
                        textverificarDactilar.setVisibility(View.GONE);
                        capturar.setVisibility(View.INVISIBLE);
                        dedoInhabilitado.setVisibility(View.GONE);
                        textDedosHabilitados.setTextColor(getResources().getColor(R.color.bg_danger));
                        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                        a.setCancelable(false);
                        a.setCanceledOnTouchOutside(false);
                        a.setTitleText("Fallo en la Conexión");
                        a.setConfirmText("Ok");
                        a.setContentText("Revise su conexión con internet o revise la conexión con el servidor");
                        a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                                finish();
                            }
                        });
                        a.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GenericalResponse> call, Throwable t) {
            }
        });

    }

    public void activarHuellero() {
        try {
            eikonGlobal = new EikonGlobal(mContext);

            Log.i("SERGIO ", "ACTIVANDO HUELLERO");

            eikonGlobal.getActivarHuelleroHilo();

            try {
                Thread.sleep(1 * 1000);

                dialogHuellasRegistradas.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dedoInhabilitado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dedoInhabilitado.isChecked()) {
                        Log.e(TAG, "------------------------clic en check box -----------" + dedoInhabilitado.isChecked());

                        if (GlobalConfig.getInstance().getIntentosXManos() == 1) {
                            Log.e(TAG, "----------------Entro al primer checkbox --------" + dedoInhabilitado.isChecked());
                            GlobalConfig.getInstance().setIntentosXManos(2);
                            checkboxMarcado = 1;
                            pintarHuellas();

                            dedoInhabilitado.setChecked(false);

                            Log.e(TAG, "------------------------seteo de check box-----------" + dedoInhabilitado.isChecked());

                        } else if (GlobalConfig.getInstance().getIntentosXManos() == 2) {

                            Log.e(TAG, "----------------Entro al segundo checkbox --------" + dedoInhabilitado.isChecked());

                            dialogHuellasRegistradas.dismiss();
                            checkboxMarcado = 2;
                            salir.setVisibility(View.VISIBLE);
                            circulo1.setVisibility(View.GONE);
                            circulo2.setVisibility(View.GONE);
                            textView14.setVisibility(View.GONE);
                            intento.setVisibility(View.INVISIBLE);
                            textDedosHabilitados.setVisibility(View.GONE);
                            textDedosHabilitados2.setVisibility(View.INVISIBLE);
                            viewDedoDerecho.setVisibility(View.INVISIBLE);
                            viewDedoIzquiedo.setVisibility(View.INVISIBLE);
                            capturar.setVisibility(View.INVISIBLE);
//                            capturaHuellaHilo.cancel(true);
//                            capturaHuellaHilo.isCancelled();
                            dedoInhabilitado.setVisibility(View.INVISIBLE);
                            salir.setVisibility(View.INVISIBLE);
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setTitleText("¡Oh, no!");
                            a.setConfirmText("Ok");
                            a.setContentText("No se procedió con la captura de los dedos habilitados");
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    finish();
                                }
                            });
                            a.show();
                        }
                    } else {
                        Log.e(TAG, "----------------Entro else del checkbox --------" + dedoInhabilitado.isChecked());
                        Log.e(TAG, "-------------else del ischecked -----------");
//
                    }
                }
            });

            Log.e(TAG, "-------------check box  ´para iniciar captura-----------" + checkboxMarcado);
//            if (checkboxMarcado == 0) {
//                Log.e(TAG, "-------------mano 1 o mano 2  sin marcara check box -----------");
//
//
//
//
        } catch (UareUException e) {
            e.printStackTrace();
        }

    }


   class HiloCaptura extends Thread {
       @SneakyThrows
       @Override
       public void run() {
           try {
               m_reset = false;
               Log.i(TAG, "run: para entrar a la funcion " + !m_reset + " la otra opcion " + m_bitmap.toString());
               if (!m_reset) {
                   Log.i(TAG, "run: entro a la captura");
                   m_bitmap = eikonGlobal.captureImage();
                   quality = eikonGlobal.getQualityResutlEikon();
                   currentFingerPrint = eikonGlobal.getHuellaByte();
                   //*    if (m_bitmap == null) continue;*//*
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           UpdateGUI();
                       }
                   });
               }
           } catch (Exception e) {
               if (!m_reset) {
                   Log.i(TAG, "try: entro a la captura");
                   m_bitmap = eikonGlobal.captureImage();
                   quality = eikonGlobal.getQualityResutlEikon();
                   currentFingerPrint = eikonGlobal.getHuellaByte();
                   //*    if (m_bitmap == null) continue;*//*
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           UpdateGUI();
                       }
                   });

               }else {
                   Log.i(TAG, "try: else ");
                   finish();
               }
           }
       }
   }


    public void UpdateGUI() {
        if (quality != 0 ) {
            if (quality < 3) {
//                    probar null los valores antes ingrsados

                if(!this.isFinishing() && dialogColocarDedo != null &&
                        dialogColocarDedo.isShowing()) { dialogColocarDedo.dismiss(); }

                fingerprintImageView.setImageBitmap(m_bitmap);
                fingerprintImageView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.borde_negro));

                intentos++;

                dialogVerificandoHuellas.setTitle("Verificando huella");
                dialogVerificandoHuellas.show();

                sendPost();
            } else if (quality > 2) {
             new HiloCaptura().start();
            }
        } else if (quality == 0) {
            fingerprintImageView.setImageDrawable(null);
        }


    }


    public void sendPost() {

        requestAsistencia.getDatoBiometrico().setImagenBiometrico(Util.bytesToString(currentFingerPrint));

        if (GlobalConfig.getInstance().getIntentosXManos() == 1) {
            requestAsistencia.setIdentificadorDato(Integer.parseInt(dedoDerecho));
        }else if(GlobalConfig.getInstance().getIntentosXManos() == 2){
            requestAsistencia.setIdentificadorDato(Integer.parseInt(dedoDerecho));
        }

        if(requestAsistencia.getDatoBiometrico().getImagenBiometrico() != null) {
            DatoBiometricoController datoBiometricoController = ApiUtils.getApi().create(DatoBiometricoController.class);
            Call<GenericalResponse> asistencia = datoBiometricoController.marcarAsistencia(requestAsistencia);
            asistencia.enqueue(new Callback<GenericalResponse>() {
                @Override
                public void onResponse(Call<GenericalResponse> call, retrofit2.Response<GenericalResponse> response) {
                    if(response.isSuccessful()) {
                        Log.i(TAG, "----------------- SUCCESSFULL -----------------");
                        Log.i(TAG, "            Se obtuvo una respuesta ");
                        Log.i(TAG, response.body().toString());
                        Log.i(TAG, "----------------- SUCCESSFULL -----------------");

                        dialogVerificandoHuellas.dismiss();

                        if(response.body() != null && response.body().getCodigoRespuesta().equals("200")){
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setConfirmText("OK");
                            a.setContentText(response.body().getDescRespuesta());
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    finish();
                                }
                            });
                            a.show();
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
                                    finish();
                                }
                            });
                            a.show();
                        }
                    } else {
                        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                        a.setCancelable(false);
                        a.setCanceledOnTouchOutside(false);
                        a.setConfirmText("OK");
                        a.setContentText("Ocurrió un error al realizar la verificación");
                        a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                                finish();
                            }
                        });
                        a.show();
                    }
                }

                @Override
                public void onFailure(Call<GenericalResponse> call, Throwable t) {
                    SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                    a.setCancelable(false);
                    a.setCanceledOnTouchOutside(false);
                    a.setConfirmText("OK");
                    a.setContentText("Ocurrió un error al realizar la verificación");
                    a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            finish();
                        }
                    });
                    a.show();
                }
            });
        }else{
            dialogVerificandoHuellas.dismiss();
            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
            a.setCancelable(false);
            a.setCanceledOnTouchOutside(false);
            a.setTitleText("¡Oh, no!");
            a.setConfirmText("Ok");
            a.setConfirmButtonTextColor(Color.WHITE);
            a.setConfirmButtonBackgroundColor(Color.RED);
            a.setContentText("Los Datos para la verificacion de la persona estan incompletos , vuelva iniciar el proceso");
            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismiss();
                    finish();
                }
            });
            a.show();

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void colorXIntentos(int contador) {

        if (contador == 1) {

            circulo1.setBackground(getDrawable(R.drawable.bola_verde));
            circulo2.setBackground(getDrawable(R.drawable.bola_gris));
        } else if (contador == 2) {
            circulo1.setBackground(getDrawable(R.drawable.bola_roja));
            circulo2.setBackground(getDrawable(R.drawable.bola_gris));

        } else if (contador == 3) {
            circulo1.setBackground(getDrawable(R.drawable.bola_roja));
            circulo2.setBackground(getDrawable(R.drawable.bola_roja));
        } else if (contador == 4) {
            circulo1.setBackground(getDrawable(R.drawable.bola_roja));
            circulo2.setBackground(getDrawable(R.drawable.bola_verde));
            intentos = 0;
        }
    }


    public void retardo() {
        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        numeroDocumento = GlobalConfig.getInstance().getResponseAuth().getUsername();
        intentoCaptura = 0;
    }

    @Override
    public void onBackPressed() {
     goNext();

    }

    public void goNext() {
        dialogColocarDedo.dismiss();
        m_reset = true;

        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        numeroDocumento = GlobalConfig.getInstance().getResponseAuth().getUsername();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        m_reset = true;
        getIntent().getExtras().clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_reset = true;
    }
}
