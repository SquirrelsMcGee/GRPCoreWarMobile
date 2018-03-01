package com.corewarmobile.corewarmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
    }
    public void NewWarrior(View view){
        Intent NewW = new Intent(this, NewWarriorPage.class);
        startActivity(NewW);
    }
    public void Add(View view){
        Intent Barracks = new Intent(this, WarriorsBarracks.class);
        startActivity(Barracks);
    }
}

