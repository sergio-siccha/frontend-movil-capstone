package com.acj.mobile.android.verifyfacial.views.VerificacionFacial;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.acj.mobile.android.verifyfacial.R;
import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.model.RequestAsistencia;
import com.acj.mobile.android.verifyfacial.service.ApiUtils;
import com.acj.mobile.android.verifyfacial.service.DatoBiometricoController;
import com.acj.mobile.android.verifyfacial.utils.BlinkTracker;
import com.acj.mobile.android.verifyfacial.utils.FacialAnalizador;
import com.acj.mobile.android.verifyfacial.utils.Util;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecognitionActivity_Gestos extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context mContext;
    private CameraView camara;
    private TextView mensaje;
    private TextView cuentaAtras;
    private TextView mensaje_ovalo;
    private ImageView img_face;
    private GifImageView gif_view;
    // private FloatingActionButton btn_change_camera;
    private TextView txtCuentaAtras;

    //Banderas de control
    private boolean haSonreido = false;
    private boolean dejoDeSonreir = false;
    private boolean pestanio = false;
    private boolean cumplio = false;
    private boolean mostrarMensaje = false;
    private boolean centro = false;
    private boolean activityRunning;
    private boolean gifShowed = false;
    private boolean ejecucionUnica = false;
    private boolean verificacionFinal = false;
    private boolean tiempoTerminado = false;

    //Banderas para controlar Flash y cámara posterior
    private boolean CAMARA_BACK = false;
    private boolean MODO_FLASH = false;

    // Veces que la persona entro al activity
    private int vecesInicioActivity;
    // Veces que la persona cambio de camara
    private int vecesCambioCamara;

    //Intentos para reconocimiento
    private int intentos = 0;

    //Inicio de contador antes de tomar foto
    private int cuentaAtrasTakePhoto = 4000;

    //Variable para almacenar globalmente el tipo de operacion
    private int tipoVerificacion = 0;

    //Variable contador
    private CountDownTimer timer;
    private CountDownTimer timerFinal;

    //Tiempo y tipo de verificacion 1 Sonrisa - 2 Pestañeo
    //ALEATORIOS
    private int primeraVerificacion = 0;
    private int segundaVerificacion = 0;
    private int tiempoEspera = 0;

    private static final String TAG = "----" + FaceRecognitionActivity_Gestos.class.getSimpleName();

    private FacialAnalizador analizador;

    private Vibrator vibe;
    private ToneGenerator toneGenerator;
    private boolean emitioSonido = false;

    private RequestAsistencia requestAsistencia;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition_gestos);

        analizador = new FacialAnalizador(this);

        camara = (CameraView) findViewById(R.id.camara);
        mensaje = (TextView) findViewById(R.id.mensajePantalla);
        cuentaAtras = (TextView) findViewById(R.id.cuenta);
        img_face = (ImageView) findViewById(R.id.img_face);
        mensaje_ovalo = (TextView) findViewById(R.id.mensaje_ovalo);
        gif_view = (GifImageView) findViewById(R.id.gif_view);
        // btn_change_camera = (FloatingActionButton) findViewById(R.id.btn_cambiar_camara);
        txtCuentaAtras = (TextView) findViewById(R.id.txtCuentaAtras);

        pestanio = false;
        vecesInicioActivity = 0;
        vecesCambioCamara = 0;

        /* REQUEST DE ASISTENCIA*/
        int tipoOperacion = getIntent().getExtras().getInt("tipoOperacion", 0);

        requestAsistencia = new RequestAsistencia();
        requestAsistencia.setTipoOperacion(tipoOperacion);
        requestAsistencia.setTipoVerificacion(2); // Verificacion tipo 2 es facial
        requestAsistencia.setTipoDocumento(1);
        requestAsistencia.setNumeroDocumento(GlobalConfig.getInstance().getResponseAuth().getUsername());
        requestAsistencia.setNombreCompletoUsuario(GlobalConfig.getInstance().getResponseAuth().getNombre());

        //Ocultando Vistas
        mensaje.setVisibility(View.INVISIBLE);
        mensaje_ovalo.setVisibility(View.INVISIBLE);

        // btn_change_camera.setEnabled(false);
        camara.setLifecycleOwner(this);
        camara.setFocusable(true);

        camara.setFacing(Facing.FRONT);
        mContext = this;
        camara.setMode(Mode.PICTURE);

        // primeraVerificacion = (Math.random() <= 0.5) ? 1 : 2;
        primeraVerificacion = 2;
        tiempoEspera = new Random().nextInt(2001) + 1000;

        vibe = (Vibrator) getSystemService("vibrator");

        // setVerificaciones();

        Log.i(TAG, "inicio lectura Firebase ML Kit");

        Log.i(TAG, "Primera verificación Aleatoria: " + primeraVerificacion);
        Log.i(TAG,"Tiempo Aleatorio: " + tiempoEspera);

        camara.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                camara.setFlash(Flash.OFF);
                Log.i(TAG, "onPictureTaken: tomo foto");
                result.toBitmap(1920, 1080, new BitmapCallback() {
                    @Override
                    public void onBitmapReady(@Nullable Bitmap bitmap) {
                        Bitmap bitmap_zoomed = getZoomedBitmap(1.4f, bitmap, 50f, 50f);
                        String fotoBase64 = Util.bitmapToBase64(bitmap_zoomed);

                        mensaje.setVisibility(View.GONE);
                        txtCuentaAtras.setVisibility(View.GONE);

                        requestAsistencia.getDatoBiometrico().setImagenBiometrico(fotoBase64);

                        SweetAlertDialog verify = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE);
                        verify.setCancelable(false);
                        verify.setCanceledOnTouchOutside(false);
                        verify.setTitleText("Atención");
                        verify.setContentText("Marcando asistencia mediante reconocimiento facial");
                        verify.show();

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

                                    verify.dismiss();

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
                    }
                });
            }

        });
    }

    private void setVerificaciones(){
        switch(primeraVerificacion){
            case 1:
                segundaVerificacion = 2;
                break;
            case 2:
                segundaVerificacion = 1;
                break;
        }
        Log.i(TAG,"Segunda verificación Aleatoria: " + segundaVerificacion);
    }

    private void mostrarDialogOPExitosa(){
        gif_view.setVisibility(View.VISIBLE);
        // btn_change_camera.setVisibility(View.GONE);
        if(CAMARA_BACK){ activarFlash(); }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gif_view.setVisibility(View.GONE);
                gifShowed = true;
                tomarFoto();
                // analisisFinal();
            }
        }, 2100);
    }

    public void checkBanderas(){
        switch(tipoVerificacion){
            case 1:
                if(haSonreido && dejoDeSonreir){
                    mostrarDialogOPExitosa();
                }
                break;
            case 2:
                if(pestanio){
                    // Emitir sonido y hacer que el telefono vibre
                    if(!emitioSonido) vibrate();
                    mostrarDialogOPExitosa();
                }
                break;
        }
    }

    private void comenzarAnalisis() {
        Log.i("Sergio","TIPO VERIFICACION: " + tipoVerificacion);

        switch (tipoVerificacion) {
            case 1:
                mensaje.setText("Por favor, sonría");
                break;
            case 2:
                mensaje.setText("Cierre los ojos durante 1 segundo, luego ábralos.");
                break;
        }

        // btn_change_camera.setEnabled(true);

        camara.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                Size size = frame.getSize();

                try {
                    if (frame.getDataClass() == byte[].class) {
                        byte[] bytes = frame.getData();
                        List<FirebaseVisionFace> results = Tasks.await(analizador.correrFacialAnalizador(size, bytes, frame.getRotationToUser()));
                        if (results.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mensaje.setVisibility(View.INVISIBLE);
                                    mensaje_ovalo.setVisibility(View.INVISIBLE);
                                    img_face.setBackgroundResource(R.drawable.overlay_face_ligthblue);
                                    mostrarMensaje = false;
                                }
                            });
                        } else {
                            if(results.size() == 1) {
                                FirebaseVisionFace face = results.get(0);
                                if (face.getBoundingBox().left > img_face.getLeft() && face.getBoundingBox().top > img_face.getTop() &&
                                        face.getBoundingBox().right < (img_face.getRight() + 125) && face.getBoundingBox().bottom < (img_face.getBottom() + 125)) {
                                    centro = true;
                                    if(isLookingStraight(face)) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mensaje_ovalo.setVisibility(View.INVISIBLE);
                                                img_face.setBackgroundResource(R.drawable.overlay_face_green);
                                                // img_face.setImageDrawable(getResources().getDrawable(R.drawable.cara_verde));

                                                if (!mostrarMensaje) {
                                                    mensaje.setVisibility(View.VISIBLE);
                                                    // drawBorderCircle(true);
                                                    mostrarMensaje = true;
                                                }else if(tiempoTerminado) {
                                                    mensaje.setVisibility(View.INVISIBLE);
                                                    mensaje_ovalo.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });

                                        switch (tipoVerificacion) {
                                            case 1:
                                                if (face.getSmilingProbability() != 0 && !haSonreido) {
                                                    if (face.getSmilingProbability() > 0.75f) {
                                                        Log.i("FB ML Kit", "HA SONREIDO | OBJETO: " + face.getSmilingProbability());
                                                        haSonreido = true;
                                                    }
                                                } else if (face.getSmilingProbability() != 0 && haSonreido) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mensaje.setText("Deje de sonreir");
                                                        }
                                                    });
                                                    if (face.getSmilingProbability() < 0.5f) {
                                                        Log.i("FB ML Kit", "DEJO DE SONREIR");
                                                        dejoDeSonreir = true;
                                                        cumplio = true;
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mensaje.setVisibility(View.INVISIBLE);
                                                                checkBanderas();
                                                                camara.clearFrameProcessors();
                                                            }
                                                        });
                                                    }
                                                }
                                                break;
                                            case 2:
                                                if (BlinkTracker.getInstance().onUpdate(face) && !pestanio) {
                                                /*if ((double)face.getLeftEyeOpenProbability() <= 0.8D || (double)face.getRightEyeOpenProbability() <= 0.8D
                                                                && !pestanio) {*/
                                                    Log.i("FB ML Kit", "HA PESTANIADO");
                                                    pestanio = true;
                                                    cumplio = true;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mensaje.setVisibility(View.INVISIBLE);
                                                            checkBanderas();
                                                            camara.clearFrameProcessors();
                                                        }
                                                    });
                                                }
                                                break;
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mensaje_ovalo.setText("Mire en dirección a la cámara");
                                                mensaje_ovalo.setVisibility(View.VISIBLE);
                                                mensaje.setVisibility(View.INVISIBLE);
                                                mostrarMensaje = false;
                                                img_face.setBackgroundResource(R.drawable.overlay_face_ligthblue);
                                                //img_face.setImageDrawable(getResources().getDrawable(R.drawable.cara_blanca));
                                                if(tiempoTerminado) {
                                                    mensaje.setVisibility(View.INVISIBLE);
                                                    mensaje_ovalo.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    centro = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mensaje_ovalo.setText("Centre su rostro en el circulo");
                                            mensaje_ovalo.setVisibility(View.VISIBLE);
                                            mensaje.setVisibility(View.INVISIBLE);
                                            mostrarMensaje = false;
                                            img_face.setBackgroundResource(R.drawable.overlay_face_ligthblue);
                                            //img_face.setImageDrawable(getResources().getDrawable(R.drawable.cara_blanca));
                                            if(tiempoTerminado) {
                                                mensaje.setVisibility(View.INVISIBLE);
                                                mensaje_ovalo.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* TAKEN FROM T5 */

    private boolean isLookingStraight(FirebaseVisionFace face) {
        return face.getHeadEulerAngleZ() > -12.0F && face.getHeadEulerAngleZ() < 12.0F && face.getHeadEulerAngleY() > -12.0F && face.getHeadEulerAngleY() < 12.0F;
    }

    private void vibrate() {
        emitioSonido = true;

        vibe.vibrate(100L);
        playBeep();
    }

    private void playBeep() {
        toneGenerator = new ToneGenerator(3, 100);
        toneGenerator.startTone(44, 200);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (toneGenerator != null) {
                    toneGenerator.release();
                    toneGenerator = null;
                }
            }
        }, 100L);
    }

    /*private void drawBorderCircle(boolean isCorrect) {
        try {
            Display display = this.getWindowManager().getDefaultDisplay();
            Bitmap bitmap = Bitmap.createBitmap(display.getWidth(), display.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(15.0F);
            paint.setColor(isCorrect ? -16711936 : -65536);
            canvas.drawCircle((float)(canvas.getWidth() / 2), (float)((double)canvas.getHeight() * 0.51D), (float)((double)canvas.getWidth() * 0.445D), paint);
            ((ImageView) findViewById(R.id.iv_border_view)).setImageBitmap(bitmap);
        } catch (Exception var6) {
            System.out.println("Error: " + var6.getMessage());
        }

    }*/

    /* TAKEN FROM T5 */

    private void analisisFinal(){
        camara.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                Size size = frame.getSize();

                try {
                    if (frame.getDataClass() == byte[].class) {
                        byte[] bytes = frame.getData();
                        List<FirebaseVisionFace> results = Tasks.await(analizador.correrFacialAnalizador(size, bytes, frame.getRotationToUser()));
                        if (results.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    verificacionFinal = false;
                                    ejecucionUnica = false;
                                    mensaje.setVisibility(View.INVISIBLE);
                                    mensaje_ovalo.setVisibility(View.INVISIBLE);
                                    txtCuentaAtras.setText("");
                                }
                            });
                        } else {
                            for (FirebaseVisionFace face : results) {
                                if (face.getBoundingBox().left > img_face.getLeft() && face.getBoundingBox().top > img_face.getTop() &&
                                        face.getBoundingBox().right < (img_face.getRight() + 125) && face.getBoundingBox().bottom < (img_face.getBottom() + 125)) {
                                    centro = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mensaje_ovalo.setVisibility(View.INVISIBLE);
                                            mensaje.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    if (gifShowed) {
                                        if (face.getSmilingProbability() < 0.5f && ojosAbiertos(face)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mensaje.setText("Manterer firme el equipo");
                                                    verificacionFinal = true;
                                                    if(!ejecucionUnica) {
                                                        ejecucionUnica = true;
                                                        iniciarCuentaFinal();
                                                    }
                                                }
                                            });
                                        } else {
                                            if (face.getSmilingProbability() > 0.75f) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        verificacionFinal = false;
                                                        ejecucionUnica = false;
                                                        mensaje.setText("Deje de sonreir");
                                                        txtCuentaAtras.setText("");
                                                    }
                                                });
                                            } else if (!ojosAbiertos(face)) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        verificacionFinal = false;
                                                        ejecucionUnica = false;
                                                        mensaje.setText("Abra más los ojos");
                                                        txtCuentaAtras.setText("");
                                                    }
                                                });
                                            }
                                        }
                                    }
                                } else {
                                    centro = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            verificacionFinal = false;
                                            ejecucionUnica = false;
                                            mensaje_ovalo.setVisibility(View.VISIBLE);
                                            mensaje.setVisibility(View.INVISIBLE);
                                            txtCuentaAtras.setText("");
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void activarFlash(){
        camara.setFlash(Flash.TORCH);
    }

    private void tomarFoto() {
        camara.takePicture();
        mensaje_ovalo.setVisibility(View.GONE);
    }

    private boolean ojosAbiertos(FirebaseVisionFace face){
        float left = face.getLeftEyeOpenProbability();
        float rigth = face.getRightEyeOpenProbability();

        return (left > 0.75 && rigth > 0.75) ? true : false;
    }

    private void iniciarCuentaFinal(){
        int segundosCuentaFinal = cuentaAtrasTakePhoto;

        timerFinal = new CountDownTimer(segundosCuentaFinal,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long time = millisUntilFinished / 1000;

                if (!activityRunning || !verificacionFinal) {
                    timerFinal.cancel();
                }

                if (time != 0 && verificacionFinal) {
                    txtCuentaAtras.setText(String.valueOf(time));
                }else if(time == 0 && verificacionFinal){
                    timerFinal.onFinish();
                }

                if(time == 1){
                    if(CAMARA_BACK){
                        activarFlash();
                    }
                }
            }

            @Override
            public void onFinish() {
                if(verificacionFinal) {
                    tomarFoto();
                    camara.clearFrameProcessors();
                }
            }
        }.start();
    }

    private void iniciarCuentaAtras() {
        int segundosCuentaAtras = 20000;
        if(vecesCambioCamara==1) vecesCambioCamara++;
        if(cuentaAtras.getVisibility() == View.INVISIBLE) cuentaAtras.setVisibility(View.VISIBLE);
        if(tiempoTerminado) tiempoTerminado = false;

        timer = new CountDownTimer(segundosCuentaAtras, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long time = millisUntilFinished / 1000;

                if (time != 0 ) {
                    cuentaAtras.setText("Tiempo: " + String.valueOf(time));
                    if(time == 1) {
                        tiempoTerminado = true;
                        timer.onFinish();
                        timer.cancel();
                    }
                }

                if (!activityRunning || vecesCambioCamara==1) { timer.cancel(); }

                if(cumplio){
                    timer.cancel();
                    cuentaAtras.setVisibility(View.GONE);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFinish() {
                Log.i("CountDown Timer","TIEMPO TERMINADO");
                if (!cumplio && intentos == 0) {
                    camara.clearFrameProcessors();
                    intentos++;

                    if(activityRunning) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionActivity_Gestos.this);
                        builder.setMessage(
                                Html.fromHtml("<b>" + "Parece que tardo demasiado <br>tiempo" + "</b>" +
                                        "<br> ¿Desea volver a intentarlo? <br>")
                        )
                                .setCancelable(false)
                                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // img_face.setImageDrawable(getResources().getDrawable(R.drawable.cara_blanca));
                                        mostrarMensaje = false;
                                        // tipoVerificacion = segundaVerificacion;
                                        iniciarCuentaAtras();
                                        comenzarAnalisis();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.azul_alert));

                        mensaje.setVisibility(View.INVISIBLE);
                        mensaje_ovalo.setVisibility(View.INVISIBLE);
                        cuentaAtras.setVisibility(View.INVISIBLE);
                    }
                } else if (!cumplio && intentos == 1) {
                    camara.clearFrameProcessors();

                    mensaje.setVisibility(View.INVISIBLE);
                    mensaje_ovalo.setVisibility(View.INVISIBLE);
                    cuentaAtras.setVisibility(View.INVISIBLE);

                    mostrarMensaje = true;
                    if(activityRunning) {
                        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                        a.setCancelable(false);
                        a.setCanceledOnTouchOutside(false);
                        a.setTitleText("Prueba de Vida");
                        a.setConfirmText("OK");
                        a.setConfirmButtonTextColor(Color.WHITE);
                        a.setConfirmButtonBackgroundColor(Color.RED);
                        a.setContentText("¡No se realizó el reconocimiento facial!");
                        a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                                finish();
                            }
                        });
                        a.show();

                        mensaje.setVisibility(View.INVISIBLE);
                        mensaje_ovalo.setVisibility(View.INVISIBLE);
                        cuentaAtras.setVisibility(View.INVISIBLE);

                    }
                }
            }
        }.start();
    }

    private Bitmap getZoomedBitmap(float zoomScale, Bitmap bmp, float xPercentage, float yPercentage){

        if (bmp != null) {
            bmp.setDensity(Bitmap.DENSITY_NONE);

            //Set the default values in case of bad input
            zoomScale = (zoomScale < 0.0f || zoomScale > 10.0f) ? 2.0f : zoomScale;
            xPercentage = (xPercentage < 0.0f || xPercentage > 100.0f) ? 50.0f : xPercentage;
            yPercentage = (yPercentage < 0.0f || yPercentage > 100.0f) ? 50.0f : yPercentage;

            float originalWidth = bmp.getWidth();
            float originalHeight = bmp.getHeight();

            //Get the new sizes based on zoomScale
            float newWidth = originalWidth / zoomScale;
            float newHeight = originalHeight / zoomScale;

            //get the new X/Y positions based on x/yPercentage
            float newX = (originalWidth * xPercentage / 100) - (newWidth / 2);
            float newY = (originalHeight * yPercentage / 100) - (newHeight / 2);

            //Make sure the x/y values are not lower than 0
            newX = (newX < 0) ? 0 : newX;
            newY = (newY < 0) ? 0 : newY;

            //make sure the image does not go over the right edge
            while ((newX + newWidth) > originalWidth) {
                newX -= 2;
            }

            //make sure the image does not go over the bottom edge
            while ((newY + newHeight) > originalHeight) {
                newY -= 2;
            }

            return Bitmap.createBitmap(bmp, Math.round(newX), Math.round(newY), Math.round(newWidth), Math.round(newHeight));
        }

        return null;
    }

    public void cambiarCamara(View view) {
        CAMARA_BACK = !CAMARA_BACK;
        Log.i(TAG, "cambiarCamara: " + CAMARA_BACK);
        if (CAMARA_BACK) {
            camara.setFacing(Facing.BACK);
            if(vecesCambioCamara == 0){
                vecesCambioCamara++;
                cuentaAtras.setVisibility(View.INVISIBLE);
                // primeraVerificacion = (Math.random() <= 0.5) ? 1 : 2;
                tiempoEspera = new Random().nextInt(2001) + 1000;
                // setVerificaciones();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // tipoVerificacion = primeraVerificacion;
                        iniciarCuentaAtras();
                        comenzarAnalisis();
                    }
                }, tiempoEspera);
            }
        } else {
            camara.setFacing(Facing.FRONT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityRunning = true;

        if(vecesInicioActivity == 0) {
            vecesInicioActivity++;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!CAMARA_BACK) {
                        tipoVerificacion = primeraVerificacion;
                        iniciarCuentaAtras();
                        comenzarAnalisis();
                    }
                }
            }, tiempoEspera);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityRunning = false;
        vecesInicioActivity = 0;
    }
}
