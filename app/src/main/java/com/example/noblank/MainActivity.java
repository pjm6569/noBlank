package com.example.noblank;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    private static final int REQ_CODE = 123;
    Button btn;
    TextView tv1;
    int mode;
    Button tv2;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.convert);
        tv1 = findViewById(R.id.loadedfile);
        tv2 = findViewById(R.id.loadfile);


        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("text/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "파일을 고르시오"), REQ_CODE);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] array = {"원본 파일 유지", "원본 파일 삭제"};
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("변환 모드 설정"); //제목
                dlg.setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mode = which;
                    }
                });
//                버튼 클릭시 동작
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File input = new File(Environment.getExternalStorageDirectory(), path);
                            File output = new File(Environment.getExternalStorageDirectory(), convertFilePath(path));

                            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));

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
                        }
                    }
                });

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
