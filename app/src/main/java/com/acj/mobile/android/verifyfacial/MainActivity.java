package com.acj.mobile.android.verifyfacial;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acj.mobile.android.verifyfacial.functions.EikonGlobal;
import com.acj.mobile.android.verifyfacial.functions.Globals;
import com.acj.mobile.android.verifyfacial.model.GenericalResponse;
import com.acj.mobile.android.verifyfacial.service.ApiUtils;
import com.acj.mobile.android.verifyfacial.service.DatoBiometricoController;
import com.acj.mobile.android.verifyfacial.views.VerificacionDactilar.VerificarIdentidadActivityDP;
import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;
import com.acj.mobile.android.verifyfacial.views.VerificacionFacial.FaceRecognitionActivity_Gestos;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;

import java.util.HashMap;
import java.util.Iterator;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    private LinearLayout btnEntrada;
    private LinearLayout btnSalida;
    private Button salir;
    private TextView txtNombre;

    private Context mContext;

    private EikonGlobal eikonGlobal;
    private ReaderCollection readers;
    boolean permisos = false;
    boolean huellero = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btnEntrada = (LinearLayout) findViewById(R.id.btn_Enrollment);
        btnSalida = (LinearLayout) findViewById(R.id.btn_verification);
        txtNombre = (TextView) findViewById(R.id.textUsuarioNombre);

        mContext = this;

        if (!GlobalConfig.getInstance().isBienvenidaShowed()) {
            if (GlobalConfig.getInstance().getResponseAuth() != null) {
                bienvenida();
            } else {
                finishAffinity();
                startActivity(new Intent(mContext, InicioSplashActivity.class));
            }
        }
    }

    public void bienvenida() {
        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE);
        a.setCancelable(false);
        a.setCanceledOnTouchOutside(false);
        a.setConfirmButtonTextColor(Color.GREEN);
        a.setConfirmButtonTextColor(Color.WHITE);
        a.setContentText("Bienvenido: " + GlobalConfig.getInstance().getResponseAuth().getNombre() + " " +
                GlobalConfig.getInstance().getResponseAuth().getApellidoPaterno() + " " +
                GlobalConfig.getInstance().getResponseAuth().getApellidoMaterno());
        a.show();

        txtNombre.setText(GlobalConfig.getInstance().getResponseAuth().getNombre() + " " +
                GlobalConfig.getInstance().getResponseAuth().getApellidoPaterno().substring(0, 1) + ".");

        GlobalConfig.getInstance().setBienvenidaShowed(true);
    }


    @Override
    protected void onStart() {
        super.onStart();

        btnEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatoBiometricoController datoBiometricoController = ApiUtils.getApi().create(DatoBiometricoController.class);
                Call<GenericalResponse> status = datoBiometricoController.getStatus(GlobalConfig.getInstance().getResponseAuth().getUsername());
                status.enqueue(new Callback<GenericalResponse>() {
                    @Override
                    public void onResponse(Call<GenericalResponse> call, retrofit2.Response<GenericalResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getCodigoRespuesta().equals("215")) {
                                SweetAlertDialog dialogOperacion = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                                dialogOperacion.setTitleText("Tipo de Verificación");
                                dialogOperacion.setContentText("Seleccione el tipo de verificación que desea realizar");
                                dialogOperacion.setConfirmText("FACIAL");
                                dialogOperacion.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Intent facial = new Intent(MainActivity.this, FaceRecognitionActivity_Gestos.class);
                                        facial.putExtra("tipoOperacion", 1);
                                        startActivity(facial);
                                        sweetAlertDialog.dismiss();
                                    }
                                });

                                dialogOperacion.setCancelButton("DACTILAR", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        if (permisos) {
                                            Intent dactilar = new Intent(MainActivity.this, VerificarIdentidadActivityDP.class);
                                            dactilar.putExtra("tipoOperacion", 1);
                                            startActivity(dactilar);
                                            sweetAlertDialog.dismiss();
                                        } else {
                                            solicitarPermisos();
                                            sweetAlertDialog.dismiss();
                                        }
                                    }
                                });
                                dialogOperacion.show();
                            } else if (response.body().getCodigoRespuesta().equals("216")) {
                                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                                a.setCancelable(false);
                                a.setCanceledOnTouchOutside(false);
                                a.setConfirmText("OK");
                                a.setContentText("Usted ya marcó su ingreso el día de hoy.");
                                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismiss();
                                    }
                                });
                                a.show();
                            } else if (response.body().getCodigoRespuesta().equals("217")) {
                                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                                a.setCancelable(false);
                                a.setCanceledOnTouchOutside(false);
                                a.setConfirmText("OK");
                                a.setContentText("Usted ya marcó su ingreso y salida el día de hoy.");
                                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismiss();
                                    }
                                });
                                a.show();
                            }
                        } else {
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setConfirmText("OK");
                            a.setContentText("Ocurrió un error al verificar el estado del usuario");
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
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
                        a.setContentText("Ocurrió un error al verificar el estado del usuario");
                        a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                            }
                        });
                        a.show();
                    }
                });
            }

        });


        btnSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatoBiometricoController datoBiometricoController = ApiUtils.getApi().create(DatoBiometricoController.class);
                Call<GenericalResponse> status = datoBiometricoController.getStatus(GlobalConfig.getInstance().getResponseAuth().getUsername());
                status.enqueue(new Callback<GenericalResponse>() {
                    @Override
                    public void onResponse(Call<GenericalResponse> call, retrofit2.Response<GenericalResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getCodigoRespuesta().equals("216")) {
                                SweetAlertDialog dialogOperacion = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                                dialogOperacion.setTitleText("Tipo de Verificación");
                                dialogOperacion.setContentText("Seleccione el tipo de verificación que desea realizar");
                                dialogOperacion.setConfirmText("FACIAL");
                                dialogOperacion.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Intent facial = new Intent(MainActivity.this, FaceRecognitionActivity_Gestos.class);
                                        facial.putExtra("tipoOperacion", 2);
                                        startActivity(facial);
                                        sweetAlertDialog.dismiss();
                                    }
                                });

                                dialogOperacion.setCancelButton("DACTILAR", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        if (permisos) {
                                            Intent dactilar = new Intent(MainActivity.this, VerificarIdentidadActivityDP.class);
                                            dactilar.putExtra("tipoOperacion", 2);
                                            startActivity(dactilar);
                                            sweetAlertDialog.dismiss();
                                        } else {
                                            solicitarPermisos();
                                            sweetAlertDialog.dismiss();
                                        }
                                    }
                                });
                                dialogOperacion.show();
                            } else if (response.body().getCodigoRespuesta().equals("215")) {
                                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                                a.setCancelable(false);
                                a.setCanceledOnTouchOutside(false);
                                a.setConfirmText("OK");
                                a.setContentText("Primero debe marcar su ingreso antes de marcar su salida.");
                                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismiss();
                                    }
                                });
                                a.show();
                            } else if (response.body().getCodigoRespuesta().equals("217")) {
                                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                                a.setCancelable(false);
                                a.setCanceledOnTouchOutside(false);
                                a.setConfirmText("OK");
                                a.setContentText("Usted ya marcó su ingreso y salida el día de hoy.");
                                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismiss();
                                    }
                                });
                                a.show();
                            }
                        } else {
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setConfirmText("OK");
                            a.setContentText("Ocurrió un error al verificar el estado del usuario");
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
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
                        a.setContentText("Ocurrió un error al verificar el estado del usuario");
                        a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                            }
                        });
                        a.show();
                    }
                });
            }
        });
    }

    private void solicitarPermisos() {
        Context applContextFragment = this;

        PendingIntent mPermissionIntentFragment;
        mPermissionIntentFragment = PendingIntent.getBroadcast(applContextFragment, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filterFragment = new IntentFilter(ACTION_USB_PERMISSION);
        applContextFragment.registerReceiver(mUsbReceiver, filterFragment);

        @SuppressLint("WrongConstant")
        UsbManager managerFragment = (UsbManager) ((Context) applContextFragment).getSystemService("usb");
        HashMap<String, UsbDevice> deviceListFragment = managerFragment.getDeviceList();
        Iterator deviceIteratorFragment = deviceListFragment.values().iterator();

        UsbDevice deviceFragment = null;

        while (deviceIteratorFragment.hasNext()) {
            deviceFragment = (UsbDevice) deviceIteratorFragment.next();
        }

        if (deviceFragment != null) {
            Log.i("Sergio/PermisosEIKON", "TODO COMPLETO: " + deviceFragment.toString());
            Log.i("Sergio/PermisosEIKON", "PRODUCT NAME DEL DISPOSITIVO: " + deviceFragment.getProductName());
            managerFragment.requestPermission(deviceFragment, (PendingIntent) mPermissionIntentFragment);
        } else {
            Log.i("VerificarFDP Huellero", "No Hay Huellero");
            SweetAlertDialog a = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
            a.setCancelable(false);
            a.setCanceledOnTouchOutside(false);
            a.setTitleText("EIKON Fingerprint SDK");
            a.setConfirmText("OK");
            a.setContentText("¡La inicialización del scanner" +
                    " de huellas digitales ha fallado!" + "\n" +
                    "Conecte el scanner a su dispositivo");
            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismiss();
                }
            });
            a.show();
            permisos = false;
            applContextFragment.unregisterReceiver(mUsbReceiver);
        }
    }

    public boolean deteccionHuellero() {
        eikonGlobal = null;

        //Inicializar Huellero
        try {
            readers = Globals.getInstance().getReaders(getApplicationContext());
            int c = readers.size();

            eikonGlobal = new EikonGlobal(getApplicationContext());

            if (c >= 1) {
                eikonGlobal.getActivarHuelleroHilo();
                return true;
            } else {
                Log.i("VerificarFDP Huellero", "No Hay Huellero");
                SweetAlertDialog a = new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.ERROR_TYPE);
                a.setCancelable(false);
                a.setCanceledOnTouchOutside(false);
                a.setTitleText("EIKON Fingerprint SDK");
                a.setConfirmText("OK");
                a.setContentText("¡La inicialización del scanner" +
                        " de huellas digitales ha fallado!" + "\n" +
                        "Conecte el scanner a su dispositivo");
                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismiss();
                    }
                });
                a.show();
                return false;
            }

        } catch (UareUException e) {
            e.printStackTrace();
        }
        return false;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            Log.i("MAVERICK  eikon ", "-------broadcaster --------");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            permisos = true;
                            huellero = deteccionHuellero();

                            if (permisos && huellero) {
                                Intent dactilar = new Intent(MainActivity.this, VerificarIdentidadActivityDP.class);
                                dactilar.putExtra("tipVerificacion", 1);
                                startActivity(dactilar);

                                permisos = false;
                                huellero = false;
                                MainActivity.this.unregisterReceiver(mUsbReceiver);
                            }
                        }
                    }
                }
            }
        }
    };
}
