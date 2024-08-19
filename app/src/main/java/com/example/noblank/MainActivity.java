package com.example.noblank;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import org.mozilla.universalchardet.UniversalDetector;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public void verifyStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // 권한 허용상태
            }else{
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",activity.getPackageName())));
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 1);
                }
            }
        }

    }


    Button btn;
    TextView tv1;
    int mode;
    Button tv2;
    String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.convert);
        tv1 = findViewById(R.id.loadedfile);
        tv2 = findViewById(R.id.loadfile);
        verifyStoragePermissions(this);



        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("text/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the spinner
                View spinnerLayout = getLayoutInflater().inflate(R.layout.encoding_spinner_layout, null);

                // Get the spinner view
                Spinner spinner = spinnerLayout.findViewById(R.id.encoding_spinner);

                // Define encoding options
                String[] encodingOptions = {"UTF-8", "EUC-KR", "UTF-16"};

                // Create an adapter for the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, encodingOptions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Set the adapter to the spinner
                spinner.setAdapter(adapter);
                final String[] array = {"원본 파일 유지", "원본 파일 삭제"};
                // Create the AlertDialog with the spinner
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("변환 모드 설정"); // Title
                dlg.setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mode = which;
                    }
                });

                // Set the spinner view to the dialog
                dlg.setView(spinnerLayout);

                // Set positive button with listener
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Retrieve selected encoding from the spinner
                        String selectedEncoding = (String) spinner.getSelectedItem();

                        // Use selected encoding in your conversion logic
                        try {
                            File input = new File(Environment.getExternalStorageDirectory(), path);
                            File output = new File(Environment.getExternalStorageDirectory(), convertFilePath(path));
                            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), selectedEncoding));
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), selectedEncoding));
                            String line;
                            boolean previousLineWasBlank = false;

                            while ((line = reader.readLine()) != null) {
                                if (line.trim().isEmpty()) {
                                    if (!previousLineWasBlank) {
                                        writer.write(line);
                                        writer.newLine();
                                        previousLineWasBlank = true;
                                    }
                                } else {
                                    writer.write(line);
                                    writer.newLine();
                                    previousLineWasBlank = false;
                                }
                            }
                            reader.close();
                            writer.close();
                            if (mode == 1) {
                                if (input.delete()) {
                                    // 새 파일 이름 변경
                                    output.renameTo(new File(path));
                                }
                            }
                            Toast.makeText(getApplicationContext(), path + " 변환 완료", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Show the AlertDialog
                dlg.show();
            }
        });
    }


    public static String convertFilePath(String filePath) {
        // 경로와 파일명을 분리
        File file = new File(filePath);
        String directory = file.getParent();
        String fileName = file.getName();

        // 파일 확장자 제거
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }

        // 새로운 파일명 생성
        String newFileName = fileName + "_공백제거";

        // 새로운 경로 반환
        return directory + File.separator + newFileName + ".txt";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = data.getData();
        path = uri.getPath();
        path = path.substring(path.indexOf(":") +1);
        if(path.contains("emulated")){
            path = path.substring(path.indexOf("0")+1);
        }
        tv1.setText(path);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
