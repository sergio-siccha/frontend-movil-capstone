package com.acj.mobile.android.verifyfacial.functions;

import android.util.Log;

import com.acj.mobile.android.verifyfacial.model.ResponseObjectAuth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GlobalConfig {
    private static GlobalConfig GLOBAL = null;
    private static final String TAG = "-----GLOBAL_CONFIG";
    private boolean cerroDialogEnroll = false;
    private ResponseObjectAuth responseAuth;
    private boolean bienvenidaShowed = false;
    private int intentosXManos = 1;
    private String tokenAuth = "";

    public static synchronized GlobalConfig getInstance() {
        if (null == GLOBAL) {
            Log.i(TAG, "----------Se inicializo el GlobalConfig.---------");
            GLOBAL = new GlobalConfig();
        }
        return GlobalConfig.GLOBAL;
    }
}
