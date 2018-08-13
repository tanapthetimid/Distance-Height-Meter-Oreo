package com.app.tanapoom.distancemeteroreo;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AirPressureActivity extends AppCompatActivity implements SensorEventListener {
    int SAMPLE_SIZE = 15;

    boolean collectingData = false;

    private SensorManager mSensorManager;
    private Sensor mPressure;


    TextView heightView;
    Button markTop;
    Button markBottom;
    Button calibrate;
    Button calculate;
    EditText calibrateHeight;

    float millibarsOfPressure;

    float[] bottomPressure = new float[1];
    float[] topPressure = new float[1];

    float meterPerMbar;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_pressure);

        heightView = findViewById(R.id.height_text_apb);

        markBottom = findViewById(R.id.apb1);
        markTop = findViewById(R.id.apb2);
        calculate = findViewById(R.id.apb3);
        calibrate = findViewById(R.id.apb4);

        calibrateHeight = findViewById(R.id.calibration_height);

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onClick(View view){

        if(view.getId() == R.id.apb1){
            samplesLeft = SAMPLE_SIZE;
            data = bottomPressure;
            collectingData = true;

            progressDialog = ProgressDialog.show(AirPressureActivity.this, "",
                    "Loading. Please wait...", true);
        }

        else if(view.getId() == R.id.apb2){
            samplesLeft = SAMPLE_SIZE;
            data = topPressure;
            collectingData = true;

            progressDialog = ProgressDialog.show(AirPressureActivity.this, "",
                    "Loading. Please wait...", true);
        }

        else if(view.getId() == R.id.apb3){
            float diffPressure = bottomPressure[0] - topPressure[0];
            float distance = diffPressure * meterPerMbar; //10 meter per mbar
            heightView.setText(String.format("height: %.3f meter",distance));
        }

        else if(view.getId() == R.id.apb4){
            float diffPressure = bottomPressure[0] - topPressure[0];
            meterPerMbar = Float.valueOf(calibrateHeight.getText().toString()) / diffPressure;
            //heightView.setText(String.format("height: %.3f meter",meterPerMbar));
        }
    }

    private void disableButtons(){
        markBottom.setClickable(false);
        markTop.setClickable(false);
        calculate.setClickable(false);
        calibrate.setClickable(false);
    }

    private void enableButtons(){
        markBottom.setClickable(false);
        markTop.setClickable(false);
        calculate.setClickable(false);
        calibrate.setClickable(false);
    }

    int samplesLeft = 0;
    float[] data;

    int INIT_DELAY = 2;
    int delay = -1;

    float lulPressure;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        lulPressure = sensorEvent.values[0];
        if(collectingData){
            if(delay == -1){
                delay = INIT_DELAY;
            }
            if(delay == 0) {
                if (samplesLeft > 0) {
                    millibarsOfPressure += sensorEvent.values[0];
                    samplesLeft--;
                } else {
                    data[0] = millibarsOfPressure / (float) SAMPLE_SIZE;
                    collectingData = false;
                    delay = -1;
                    progressDialog.dismiss();
                    //heightView.setText(String.format("%.3f",data[0]));
                }
            }else{
                delay--;
            }
        }else{
            millibarsOfPressure = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
