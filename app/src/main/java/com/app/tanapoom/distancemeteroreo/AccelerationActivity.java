package com.app.tanapoom.distancemeteroreo;

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
import android.widget.TextView;

import java.util.ArrayList;

public class AccelerationActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;

    TextView textBox1;
    TextView textBox2;
    TextView textBox3;
    TextView textBox4;
    TextView textBox5;
    TextView textBox6;
    TextView textBox7;

    Button start;
    Button end;

    boolean collecting = false;

    ArrayList<Float> accelerationValues;

    long time = -1;
    long avgDeltaTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        textBox1 = (TextView) findViewById(R.id.acc_text_1);
        textBox2 = (TextView) findViewById(R.id.acc_text_2);
        textBox3 = (TextView) findViewById(R.id.acc_text_3);
        textBox4 = (TextView) findViewById(R.id.acc_text_4);
        textBox5 = (TextView) findViewById(R.id.acc_text_5);
        textBox6 = (TextView) findViewById(R.id.acc_text_6);
        textBox7 = (TextView) findViewById(R.id.acc_text_7);

        start = (Button) findViewById(R.id.acc_start);
        end = (Button) findViewById(R.id.acc_end);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long newTime = System.nanoTime();

        if(time > 0){
            if(avgDeltaTime < 0){
                avgDeltaTime = newTime - time;
            }else{
                avgDeltaTime = (long)( 0.5 * ((newTime - time) + avgDeltaTime));
            }
        }

        time = newTime;

        float acceleration = sensorEvent.values[2];

        if(Math.abs(acceleration) < 0.1)acceleration = 0;

        //textBox1.setText(String.valueOf(sensorEvent.values[0]));
        //textBox2.setText(String.valueOf(sensorEvent.values[1]));
        textBox3.setText(String.valueOf(acceleration));

        Log.e("hi",String.valueOf(sensorEvent.values[2]));

        if(sensorEvent.values[0] > 1){
            textBox4.setText("HIIIIII");
        }else{
            textBox4.setText("-");
        }
        if(sensorEvent.values[1] > 1){
            textBox5.setText("HIIIIII");
        }else{
            textBox5.setText("-");
        }
        if(sensorEvent.values[2] > 1){
            textBox6.setText("HIIIIII");
        }else{
            textBox6.setText("-");
        }

        if(collecting){
            accelerationValues.add(acceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onAccClick(View view){
        if(view.getId() == R.id.acc_start){
            collecting = true;
            accelerationValues = new ArrayList<>();
        } else if(view.getId() == R.id.acc_end){
            collecting = false;
            calculateValues();
        }
    }

    private void calculateValues(){
        ArrayList<Float> velocities = new ArrayList<>();

        for(int x = 1; x < accelerationValues.size(); x++){
            velocities.add(trapezoidalIntegration(accelerationValues,0,x));
        }

        double displacement = trapezoidalIntegration(velocities, 0, velocities.size()-1);

        textBox7.setText(String.valueOf(displacement));
    }

    private float trapezoidalIntegration(ArrayList<Float> values, int start, int end){
        float sum = values.get(start) + values.get(end);
        for(int x = start+1; x < end; x++){
            sum += values.get(x);
        }

        float integral = avgDeltaTime * 0.5f * sum;
        Log.e("HI", String.valueOf(avgDeltaTime/1000000000.0));
        return integral / 1000000000.0f;
    }
}
