package com.android.filemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class pmTextEdit extends AppCompatActivity {

    public boolean newFile = false;
    public String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm_text_edit);

        EditText textInput = (EditText) findViewById(R.id.editText);
        Button back = (Button) findViewById(R.id.backButton);
        Button save = (Button) findViewById(R.id.saveButton);
        TextView fileTitle = (TextView) findViewById(R.id.fileTitle);


        String filePath = getIntent().getStringExtra("FILE_ABSOLUTE_PATH");
        File file = new File( filePath);

        if( file.isFile()){
            newFile = false;
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (Exception e) {
//                Toast.makeText( pmTextEdit.this, "Unable to open file.", Toast.LENGTH_SHORT).show();
                Toast.makeText( pmTextEdit.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //Setting the  input text to file contents
            textInput.setText(text.toString());
            fileName = file.getName();
            fileTitle.setText( fileName);
        } else {
            newFile = true;
            textInput.setText( "");

            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            fileName = s.format(new Date()) + ".txt";
            fileTitle.setText( fileName);
        }




        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Save to file
                try{
                    if(  newFile) {
                        File file = new File(filePath, fileName);
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file,false);
                        writer.write(textInput.getText().toString());
                        writer.flush();
                        writer.close();
                        finish();
                    } else {
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file,false);
                        writer.write(textInput.getText().toString());
                        writer.flush();
                        writer.close();
                        finish();
                    }
                }  catch ( Exception e){
                    Toast.makeText( pmTextEdit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}