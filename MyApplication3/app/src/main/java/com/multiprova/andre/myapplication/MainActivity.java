package com.multiprova.andre.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;


public class MainActivity extends Activity {
//    static {
//        if (!OpenCVLoader.initDebug()) {
//            // Handle initialization error
//        }
//    }
    private TextView textView;
    Camera camera;
    CameraPreview cameraPreview;
    final String TAG = "CameraApp";
    private Camera.PictureCallback mPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        textView = (TextView) findViewById(R.id.textView);
        if (checkCameraHardware(this)) {
            textView.setText("Existem " + Camera.getNumberOfCameras() + " Cameras");
            camera = getCameraInstance();
            cameraPreview = new CameraPreview(this,camera);
            preview.addView(cameraPreview);
        }

        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "Passou no onPictureTaken!");
                textView.setText("Tirou uma foto!");
                SalvaArquivo salvaArquivo = new SalvaArquivo(data);
                salvaArquivo.execute();
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                i.putExtra("image", data);
                startActivity(i);
                finish();
            }
        };

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        camera.takePicture(null, null, mPicture);
                        Log.d(TAG, "Voltou do Callback!");
                    }
                }
        );
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            Log.d("CameraDirect3", "Camera Falhou ao abrir...");
        }
        return c;
    }
}