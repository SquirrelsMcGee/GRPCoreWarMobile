package com.corewarmobile.corewarmobile;

import android.os.Bundle;
import android.view.View;

public class AddActivity extends BarrackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
    }

    public void select(View v) {
        // TODO load the selected warrior into game play
    }

    public void cancel(View v) {
        // TODO direct back to game play
    }
}
