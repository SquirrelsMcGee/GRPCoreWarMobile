package com.corewarmobile.corewarmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditActivity extends NewWarriorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
    }

    /*
    @Override
    public static void save() {
        TODO possibly direct save into file
    }
    */
}
