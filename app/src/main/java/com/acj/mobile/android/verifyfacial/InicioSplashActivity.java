package com.acj.mobile.android.verifyfacial;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.acj.mobile.android.verifyfacial.views.login.LoginActivity;

public class InicioSplashActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_inicio_splash);

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        final Matrix matrix = imageView.getImageMatrix();
        final float imageWidth = imageView.getDrawable().getIntrinsicWidth();
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final float scaleRatio = screenWidth / imageWidth;
        matrix.postScale(scaleRatio, scaleRatio);
        imageView.setImageMatrix(matrix);

        int DURACION_SPLASH = 3000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(InicioSplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURACION_SPLASH);
    }
}
