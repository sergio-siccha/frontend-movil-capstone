package com.acj.mobile.android.verifyfacial.functions;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Quality;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.digitalpersona.uareu.jni.DpfjQuality;

public class EikonGlobal {
    //Variables
    private String m_deviceName = "";
    private ReaderCollection readers;
    private Reader m_reader;
    private Context applContext;
    private int m_DPI = 0;
    private int qualityResutlEikon;
    private Bitmap m_bitmap = null;
    private PendingIntent mPermissionIntent;
    private Reader.CaptureResult cap_result = null;
    private int numeroSerieDispositivo;
private boolean permisosAceptados;
    private Reader readerS;
    private byte[] forGetSerialNumber;
    private byte[] huellaByte;
    private String serialNumber = new String("N/A");

    //Guarda huellas en formato Base64
    private Fmd m_fmd = null;

    private Engine m_engine = null;

    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";


    public EikonGlobal() {
    }


    public Engine getEngine() {
        return m_engine;
    }

    public Fmd getFmD() {
        Log.i("SERGIO", "Fmd EikonGlobal: " + m_fmd);
        return m_fmd;
    }

    public byte[] getHuellaByte() {
        return huellaByte;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public boolean isPermisosAceptados() {
        return permisosAceptados;
    }

    public void setPermisosAceptados(boolean permisosAceptados) {
        this.permisosAceptados = permisosAceptados;
    }

    public int getQualityResutlEikon() {
        return qualityResutlEikon;
    }

    public void setQualityResutlEikon(int qualityResutlEikon) {
        this.qualityResutlEikon = qualityResutlEikon;
    }

    public EikonGlobal(Context applContext) throws UareUException {
        this.applContext = applContext;

        Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;

        m_bitmap = Globals.GetLastBitmap();

        readers = Globals.getInstance().getReaders(applContext);

        Log.i("SERGIO", "Inicializando Variables");
    }


    //Metodos

    public void getActivarHuelleroHilo() {
        try {
            applContext = applContext.getApplicationContext();
            readers = Globals.getInstance().getReaders(applContext);
            mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            applContext.registerReceiver(mUsbReceiver, filter);
            if (!readers.isEmpty()) {
                m_deviceName = readers.get(0).GetDescription().name;

                readers.get(0).Open(Reader.Priority.COOPERATIVE);
                readers.get(0).Close();
            } else {
                m_deviceName = "";
            }
        } catch (UareUException e) {
            e.printStackTrace();
        }

        Log.i("MAVERICK  eikon ", "-------entro on activity result --------");
        if (true) {
            Log.i("MAVERICK  eikon ", "-------no entro if1 --------");
            try {
                applContext = applContext.getApplicationContext();
                m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
                Log.i("MAVERICK  eikon ", "------usa el global --------" + m_reader.toString());
                {
                    Log.i("MAVERICK  eikon ", "-------Permisos de usb  --------");
                    if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName)) {
                        try {
                            m_reader.Open(Reader.Priority.EXCLUSIVE);
                            m_DPI = Globals.GetFirstDPI(m_reader);

                            m_reader.Close();
                            applContext.unregisterReceiver(mUsbReceiver);
                        } catch (UareUException e1) {

                        }
                        Log.i("MAVERICK  eikon ", "------dpfpddusbHost permisos cheeck libreria --------");
                    }
                    Log.i("MAVERICK  eikon ", "-------no entro al dpfdfddu --------");
                }
            } catch (UareUException e1) {
//
            } catch (DPFPDDUsbException e) {
//
            }
        } else {
//
        }
    }


    public String getCodigoDispositivo() {
        try {
            readerS = Globals.getInstance().getReader(m_deviceName, applContext);
            readerS.Open(Reader.Priority.EXCLUSIVE);

            try {
                forGetSerialNumber = readerS.GetParameter(Reader.ParamId.PARMID_PTAPI_GET_GUID);

                if (16 == forGetSerialNumber.length) {
                    final char[] hexArray = "0123456789ABCDEF".toCharArray();
                    char[] hexChars = new char[forGetSerialNumber.length * 2];
                    for (int j = 0; j < forGetSerialNumber.length; j++) {
                        int v = forGetSerialNumber[j] & 0xFF;
                        hexChars[j * 2] = hexArray[v >>> 4];
                        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
                    }
                    serialNumber = new String(hexChars);
                    Log.i("PRUEBA IMAGE DATA", "------- Serial Number de Dispositivo --------" + serialNumber);
                }
            } catch (Exception e) {
            }
            readerS.Close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serialNumber;
    }

    public Bitmap captureImage() throws UareUException {
        try {
            applContext = applContext.getApplicationContext();
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
            m_reader.Open(Reader.Priority.COOPERATIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();
            byte[] result = m_reader.GetParameter(Reader.ParamId.DPFPDD_PARMID_PAD_ENABLE);
            cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);

            if (cap_result.image != null) {
                m_reader.CancelCapture();
                m_fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                Log.w("Fmd Original", "Dato: " + m_fmd.toString());


                huellaByte = m_fmd.getData();


                Log.i("PRUEBA(TEMPLATE BASE64)", "-------m_bitmap   --------  " + ByteAString(m_fmd.getData()));

                // save bitmap image locally
                m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                // calculate nfiq score
                DpfjQuality quality = new DpfjQuality();
                int nfiqScore = quality.nfiq_raw(
                        cap_result.image.getViews()[0].getImageData(),    // raw image data
                        cap_result.image.getViews()[0].getWidth(),        // image width
                        cap_result.image.getViews()[0].getHeight(),        // image height
                        m_DPI,                                            // device DPI
                        cap_result.image.getBpp(),                        // image bpp
                        Quality.QualityAlgorithm.QUALITY_NFIQ_NIST        // qual. algo.
                );
                qualityResutlEikon = nfiqScore;

                Log.i("UareUSampleJava", "capture result nfiq score: " + nfiqScore);


                m_reader.CancelCapture();
            }

            m_reader.Close();
            return m_bitmap;
        } catch (Exception e) {

            m_bitmap = null;
            return m_bitmap;

        }
    }

    protected void CheckDevice()
    {
        try
        {
            m_reader.Open(Reader.Priority.EXCLUSIVE);
            Reader.Capabilities cap = m_reader.GetCapabilities();

            m_reader.Close();
        }
        catch (UareUException e1)
        {

        }
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    Log.i("MAVERICK  eikon ", "-------broadcaster --------");
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            numeroSerieDispositivo = device.getProductId();
                            //call method to set up device communication
                            //                                m_reader.Open(Reader.Priority.EXCLUSIVE);
//                                m_DPI = Globals.GetFirstDPI(m_reader);
                            permisosAceptados = true;
                            Log.i("MAVERICK  eikon ", "-------permisosAceptados --------" + isPermisosAceptados());
//                                m_reader.Close();
                        }
                    } else {
                        permisosAceptados = false;
                    }
                }

            }

        }
    };

    public String ByteAString(byte[] template) {

        String huellaBase64 = Base64.encodeToString(template, Base64.NO_WRAP);

        return huellaBase64;
    }
}

//
//    //Valida conexion con huellero
//    private class ActivarhuelleroEikonHilo extends android.os.AsyncTask<Void, Integer, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            try {
//                applContext = applContext.getApplicationContext();
//                readers = Globals.getInstance().getReaders(applContext);
//                mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
//                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//                applContext.registerReceiver(mUsbReceiver, filter);
//                if (!readers.isEmpty()) {
//                    m_deviceName = readers.get(0).GetDescription().name;
//
//                    readers.get(0).Open(Reader.Priority.COOPERATIVE);
//                    readers.get(0).Close();
//                } else {
//                    m_deviceName = "";
//                }
//            } catch (UareUException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        protected void onPostExecute(String m_deviceName) {
//            super.onPostExecute(m_deviceName);
//       }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            Log.i("MAVERICK  eikon ", "-------entro on activity result --------");
//            if (true) {
//                Log.i("MAVERICK  eikon ", "-------no entro if1 --------");
//                try {
//                    applContext = applContext.getApplicationContext();
//                    m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
//                    Log.i("MAVERICK  eikon ", "------usa el global --------" + m_reader.toString());
//                    {
//                        Log.i("MAVERICK  eikon ", "-------Permisos de usb  --------");
//                        if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName)) {
//                            try {
//                                m_reader.Open(Reader.Priority.EXCLUSIVE);
//                                m_DPI = Globals.GetFirstDPI(m_reader);
//
//                                m_reader.Close();
//                            } catch (UareUException e1) {
//
//                            }
//                            Log.i("MAVERICK  eikon ", "------dpfpddusbHost permisos cheeck libreria --------");
//                        }
//                        Log.i("MAVERICK  eikon ", "-------no entro al dpfdfddu --------");
//                    }
//                } catch (UareUException e1) {
////
//                } catch (DPFPDDUsbException e) {
////
//                }
//            } else {
////
//            }
//
//            return m_deviceName;
//        }
//    }