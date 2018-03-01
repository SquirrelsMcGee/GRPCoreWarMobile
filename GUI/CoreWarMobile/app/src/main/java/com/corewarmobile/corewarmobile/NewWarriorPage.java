package com.corewarmobile.corewarmobile;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewWarriorPage extends AppCompatActivity {
    public EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_warrior_page);
        editText = findViewById(R.id.editText);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }
    public void clear(View v) {
        editText.setText("");
    }
    public void saveBtn(View v){
        //String filename = "myfile";
        //String fileContents = "Hello world!";
        //FileOutputStream outputStream;


        EditText warriorName = findViewById(R.id.WarriorName);
        String WarriorNameText = String.valueOf(warriorName.getText());
        File file = new File(v.getContext().getFilesDir(), WarriorNameText);
        //String[] Warrior = String.valueOf(editText.getText()).split(System.getProperty("line.separator"));
        String[] Warrior = String.valueOf(editText.getText()).split(System.getProperty("line.separator"));
        editText.setText("");
        warriorName.setText("");
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
        Save(file, Warrior);

        //try {
          //  outputStream = openFileOutput(WarriorNameText, Context.MODE_PRIVATE);
            //outputStream.write(Warrior.getBytes());
            //outputStream.close();
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

    }
    public static void Save(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }



}
