package com.corewarmobile.corewarmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }
    public void PlayScreen(View view){
        Intent PlayS = new Intent(this, GameActivity.class);
        startActivity(PlayS);
    }
    public void WarriorB(View view){
        Intent Barracks = new Intent(this, BarrackActivity.class);
        startActivity(Barracks);
    }
}
