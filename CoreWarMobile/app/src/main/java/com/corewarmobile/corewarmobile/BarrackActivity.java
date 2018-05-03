package com.corewarmobile.corewarmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BarrackActivity extends AppCompatActivity {

    public String item;
    private static Context context;
    private List<String> fileList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarrackActivity.context = getApplicationContext();
        setContentView(R.layout.activity_barrack);
        File root = getFilesDir();
        ListDir(root);
        //String[] fileList(context.getFilesDir());
        //String Root = context.getFilesDir();

    }


    void ListDir(File f) {
        ListView lv;
        File[] files = f.listFiles();
        fileList.clear();
        for (File file : files) {
            fileList.add(file.getName());
        }
        //ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        //setListAdapter(directoryList);

        lv = (ListView)findViewById(R.id.ListView);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList));

        final Intent PlayS = new Intent(this, GameActivity.class);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Clicking on items
                item = ((TextView)v).getText().toString();
                File newW = new File(item);
                GameActivity.WarriorName = item;
                Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
                startActivity(PlayS);
                //GA.TextChanger(item);

            }
        });

    }

}
