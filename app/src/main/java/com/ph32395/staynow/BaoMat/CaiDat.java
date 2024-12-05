package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.R;

public class CaiDat extends AppCompatActivity {

    private LinearLayout nextDoiMK;
    private LinearLayout updateNguoiDung;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        nextDoiMK = findViewById(R.id.nextDoiMK);
        updateNguoiDung = findViewById(R.id.updateNguoiDung);
        btnBack = findViewById(R.id.button_backSettings);

        nextDoiMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, DoiMK.class));
                finish();
            }
        });
        updateNguoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, CapNhatThongTin.class));
                finish();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, MainActivity.class));
                finish();
            }
        });
    }
}