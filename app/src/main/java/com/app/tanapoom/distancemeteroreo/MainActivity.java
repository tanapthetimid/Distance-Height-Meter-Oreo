package com.app.tanapoom.distancemeteroreo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void onLaunchClick(View view){
        if(view.getId() == R.id.open1) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }else if(view.getId() == R.id.open2) {
            Intent intent = new Intent(this, CameraActivityHeight.class);
            startActivity(intent);
        }
        else if(view.getId() == R.id.open3) {
            Intent intent = new Intent(this, AirPressureActivity.class);
            startActivity(intent);
        }
    }
}
