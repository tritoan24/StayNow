package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ph32395.staynow.R;
import com.ph32395.staynow.databinding.ActivityThayDoiNgonNguBinding;

public class CaiDat extends AppCompatActivity {

    private LinearLayout nextDoiMK;
    private LinearLayout updateNguoiDung;
    private LinearLayout language;
    private ImageButton btnBackSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        nextDoiMK = findViewById(R.id.nextDoiMK);
        updateNguoiDung = findViewById(R.id.updateNguoiDung);
        language = findViewById(R.id.btnLanguage);
        btnBackSetting = findViewById(R.id.button_backSettings);

        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, ThayDoiNgonNgu.class));
            }
        });

        btnBackSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        nextDoiMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, DoiMK.class));
            }
        });
        updateNguoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, CapNhatThongTin.class));

            }
        });
    }
}