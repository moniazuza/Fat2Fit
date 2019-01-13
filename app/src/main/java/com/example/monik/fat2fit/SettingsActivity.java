package com.example.monik.fat2fit;

import android.os.Bundle;
import android.renderscript.Element;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.fitness.data.DataType;

import javax.sql.DataSource;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
         final Button saveSettingsButton = (Button) findViewById(R.id.saveSettingsButton);
         saveSettingsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 saveSettings();
             }
         });
    }

    private void saveSettings() {
        
    }

}
