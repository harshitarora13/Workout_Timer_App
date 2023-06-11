package com.example.workouttimerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText edtTimeDuration, etRest,etSet;
    private MediaPlayer mediaPlayer;
    Button btnStart,btnContinue;
    long equalPartDuration,RestTimeLeftInMillis;
    long TimeLeftInMillis;
    int progress,TotalSets,ReamingSets;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtTimeDuration = findViewById(R.id.edtTimeDuration);
        etRest = findViewById(R.id.etRest);
        btnStart = findViewById(R.id.btn);
        etSet = findViewById(R.id.etSet);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTimeDuration.getText().toString().isEmpty()) {
                    edtTimeDuration.setError("please enter Duration");
                }
                if (etRest.getText().toString().isEmpty()) {
                    etRest.setError("please enter rest");
                }
                if (etSet.getText().toString().isEmpty()) {
                    etSet.setError("please enter sets");
                }else {

                    long TimeLeftInMillis = Long.parseLong(edtTimeDuration.getText().toString());
                    TimeLeftInMillis = TimeLeftInMillis * 60000;
                    RestTimeLeftInMillis = Long.parseLong(etRest.getText().toString()) * 1000;
                    equalPartDuration = TimeLeftInMillis / Long.parseLong(etSet.getText().toString());
                    SaveData();
                    Toast.makeText(MainActivity.this, ""+equalPartDuration, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),ActivityWorkOut.class);
                    startActivity(intent);

                }
            }
        });




    }

    private void SaveData() {
        SharedPreferences preferences = getSharedPreferences("shared1", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("TotalSets",Integer.parseInt(etSet.getText().toString()));
        editor.putInt("DefaultTotalSets",Integer.parseInt(etSet.getText().toString()));
        editor.putInt("ReamingSets",Integer.parseInt(etSet.getText().toString()));
        editor.putLong("RestTimeLeftInMillis",RestTimeLeftInMillis);
        editor.putLong("RestTimeLeftInMillisDefault",RestTimeLeftInMillis);
        editor.putLong("TimeLeftInMillis",equalPartDuration);
        editor.putLong("TimeLeftInMillisDefault",equalPartDuration);
        editor.apply();
    }


    @Override
    protected void onPause() {
        super.onPause();
        SaveData();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}