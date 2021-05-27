package com.acj.mobile.android.verifyfacial.views.VerificacionFacial;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acj.mobile.android.verifyfacial.R;
import com.acj.mobile.android.verifyfacial.enrollment.capture.FaceRegisterActivity;

public class DialogInstruccionEnrollement extends DialogFragment {
    private Button btnContinue;
    private CheckBox chckDontWindows;
    private String mensajePantalla;


    Bundle bundle = new Bundle();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_intruccion, container, false);


        btnContinue = (Button) view.findViewById(R.id.btn_continue_dialog_enroll);
        chckDontWindows = (CheckBox) view.findViewById(R.id.checkbox_enrollment_dialog);

        pref = getActivity().getSharedPreferences("PREFS", 0); // 0 - for private mode
        editor = pref.edit();

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnContinue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FaceRegisterActivity.class);
                intent.putExtra("mensajePantalla", mensajePantalla);
                dismiss();
        }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            bundle = getArguments().getBundle("bundle");
             mensajePantalla = getArguments().getString("mensajePantalla", "0");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        chckDontWindows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("neverShow", true); // Storing boolean - true/false
                editor.apply();

                getDialog().dismiss();
            }
        });


    }





}
