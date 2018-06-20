package com.app.tanapoom.distancemeteroreo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class CameraActivityHeight extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private SensorManager mSensorManager;
    private MySensorEventListener mSensorEventListener;

    private String cameraID;
    private Size imageDimension;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession myCameraCaptureSession;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private CameraCharacteristics myCameraCharacteristics;

    TextView textView2;
    TextureView cameraOutputView;
    EditText heightBox;

    float angle;
    float angle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_height);

        ImageView crosshair = findViewById(R.id.crosshair);
        crosshair.setImageDrawable(getDrawable(R.drawable.crosshair));

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEventListener = new MySensorEventListener(mSensorManager);

        cameraOutputView = findViewById(R.id.texture);
        cameraOutputView.setSurfaceTextureListener(textureListener);

        heightBox = findViewById(R.id.height);
        textView2 = findViewById(R.id.text2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.

        if (cameraOutputView.getSurfaceTexture() != null) {
            openCamera();
        }

        startCameraHandlerThread();

        mSensorManager.registerListener(mSensorEventListener
                , mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        mSensorManager.registerListener(mSensorEventListener
                , mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();

        myCameraCaptureSession.close();
        closeCamera();
        stopCameraHandlerThread();
        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public void onButtonClick(View view) {
        mSensorEventListener.updateOrientationAngles();
        if (!heightBox.getText().toString().isEmpty()) {
            if (view.getId() == R.id.button1) {
                angle = Math.abs(mSensorEventListener.getPitch());
            }else if (view.getId() == R.id.button2) {
                angle2 = Math.abs(mSensorEventListener.getPitch());
                float quadrant = mSensorEventListener.getPitchQuadrantUpDown();
                angle2 = angle2 * (Math.signum(quadrant));
            }else if (view.getId() == R.id.button3) {
                float userHeight = Float.valueOf(heightBox.getText().toString()) / 100f;
                double length = userHeight * Math.tan(angle);
                double angleCalc = Math.PI/2.0 - Math.abs(angle2);
                double dist = length * Math.tan(angleCalc);

                double finalDisp = dist * (-1)/Math.signum(angle2);

                textView2.setText(" " + String.valueOf(userHeight + finalDisp) + " meter");
            }
        } else {
            Toast.makeText(this, "Please specify the height at which you are holding your device!", Toast.LENGTH_LONG).show();
        }


    }

    private void startCameraHandlerThread() {
        cameraThread = new HandlerThread("Camera Background");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopCameraHandlerThread() {
        cameraThread.quitSafely();
        cameraThread = null;
        cameraHandler = null;
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void openCamera() {
        if (cameraDevice == null) {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraID = manager.getCameraIdList()[0];
                myCameraCharacteristics = manager.getCameraCharacteristics(cameraID);
                StreamConfigurationMap map = myCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }

                manager.openCamera(cameraID, cameraStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected void createCameraPreview() {
        try {

            SurfaceTexture texture = cameraOutputView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getHeight(), imageDimension.getWidth());
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            myCameraCaptureSession = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getApplicationContext(), "on configure failed", Toast.LENGTH_SHORT).show();

                        }
                    }
                    , null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            myCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
}
