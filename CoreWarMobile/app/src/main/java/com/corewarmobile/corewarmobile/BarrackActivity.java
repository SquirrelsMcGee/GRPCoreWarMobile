package com.corewarmobile.corewarmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Adapter;
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

    public BarrackActivity activty = this;

    ListView lv;
    File[] files;
    ArrayAdapter arrayAdapter;

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

        files = f.listFiles();
        fileList.clear();
        File file = null;
        for (int i = 0; i < files.length; i++) {
            file = files[i];
            fileList.add(file.getName());
        }
        //ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        //setListAdapter(directoryList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        lv = (ListView)findViewById(R.id.ListView);
        lv.setAdapter(arrayAdapter);

        final Intent PlayS = new Intent(this, GameActivity.class);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Clicking on items
                item = ((TextView)v).getText().toString();
                File newW = new File(item);
                if (GameActivity.WarriorName == "Warrior 1")
                    GameActivity.WarriorName = item;
                else if(GameActivity.WarriorName2 == "Warrior 2")
                    GameActivity.WarriorName2 = item;
                Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
                startActivity(PlayS);
                //GA.TextChanger(item);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
                List<String> temp = fileList;

                item = ((TextView)v).getText().toString();

                File dir = getFilesDir();
                File file = new File(dir, item);
                boolean deleted = file.delete();

                if (deleted) {
                    Toast.makeText(getBaseContext(), item + " deleted", Toast.LENGTH_SHORT).show();
                }

                fileList.remove(item);
                temp.remove(item);
                arrayAdapter.notifyDataSetChanged();

                return true;
            }
        });
    }

}
