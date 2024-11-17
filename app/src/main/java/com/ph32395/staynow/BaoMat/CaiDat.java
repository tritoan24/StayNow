package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.ph32395.staynow.R;

public class CaiDat extends AppCompatActivity {

    private LinearLayout nextDoiMK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        nextDoiMK = findViewById(R.id.nextDoiMK);

        nextDoiMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CaiDat.this, DoiMK.class));
                finish();
            }
        });
    }
}