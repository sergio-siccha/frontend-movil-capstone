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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.acj.mobile.android.verifyfacial.functions.EikonGlobal;
import com.acj.mobile.android.verifyfacial.functions.Globals;
import com.acj.mobile.android.verifyfacial.views.VerificacionDactilar.VerificarIdentidadActivityDP;
import com.acj.mobile.android.verifyfacial.functions.GlobalConfig;
import com.acj.mobile.android.verifyfacial.views.VerificacionFacial.FaceRecognitionActivity_Gestos;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;

import java.util.HashMap;
import java.util.Iterator;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    private Button btnEntrada;
    private Button btnSalida;
    private Button salir;
    private ImageView btnInfo;

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

        btnEntrada = (Button) findViewById(R.id.btn_Enrollment);
        btnSalida = (Button) findViewById(R.id.btn_verification);
        btnInfo = (ImageView) findViewById(R.id.btn_info);

        mContext = this;

        if (!GlobalConfig.getInstance().isBienvenidaShowed()) {
            if(GlobalConfig.getInstance().getResponseAuth() != null) {
                bienvenida();
            } else {
                finishAffinity();
                startActivity(new Intent(mContext,InicioSplashActivity.class));
            }
        }
    }

    public void bienvenida() {
        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE);
        a.setCancelable(false);
        a.setCanceledOnTouchOutside(false);
        a.setConfirmButtonTextColor(Color.GREEN);
        a.setConfirmButtonTextColor(Color.WHITE);
        a.setContentText("Bienvenido: " + GlobalConfig.getInstance().getResponseAuth().getNombre());
        a.show();

        GlobalConfig.getInstance().setBienvenidaShowed(true);
    }


    @Override
    protected void onStart() {
        super.onStart();

        btnEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if(permisos) {
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
            }

        });


        btnSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if(permisos) {
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
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InformationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void solicitarPermisos(){
        Context applContextFragment = this;

        PendingIntent mPermissionIntentFragment;
        mPermissionIntentFragment = PendingIntent.getBroadcast(applContextFragment, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filterFragment = new IntentFilter(ACTION_USB_PERMISSION);
        applContextFragment.registerReceiver(mUsbReceiver, filterFragment);

        @SuppressLint("WrongConstant")
        UsbManager managerFragment = (UsbManager)((Context)applContextFragment).getSystemService("usb");
        HashMap<String, UsbDevice> deviceListFragment = managerFragment.getDeviceList();
        Iterator deviceIteratorFragment = deviceListFragment.values().iterator();

        UsbDevice deviceFragment = null;

        while(deviceIteratorFragment.hasNext()){
            deviceFragment = (UsbDevice) deviceIteratorFragment.next();
        }

        if(deviceFragment != null) {
            Log.i("Sergio/PermisosEIKON", "TODO COMPLETO: " + deviceFragment.toString());
            Log.i("Sergio/PermisosEIKON", "PRODUCT NAME DEL DISPOSITIVO: " + deviceFragment.getProductName());
            managerFragment.requestPermission(deviceFragment, (PendingIntent) mPermissionIntentFragment);
        }else{
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

                            if(permisos && huellero) {
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