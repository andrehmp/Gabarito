package com.multiprova.andre.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    final String TAG = "CameraApp";
    private int count = 0, tam = 0;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            setWillNotDraw(false);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera arg1) {
                    count++;
                    tam = data.length;
                    Log.d(TAG, "onPreviewFrame()" + tam);
                    invalidate();
                }
            });
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        count++;

        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setStrokeWidth(15);
        float height=canvas.getHeight();
        float width=canvas.getWidth();
        canvas.drawPoint( width *(float) 0.15, height *(float) 0.1, p);
        canvas.drawPoint( width *(float) 0.85,height *(float) 0.1,p);
        canvas.drawPoint( width *(float) 0.15,height *(float) 0.9,p);
        canvas.drawPoint(width *(float) 0.85,height*(float) 0.9,p);
 //       canvas.drawRect(10, 10, 400, 400, p);
//
//        Paint myPaint = new Paint();
//        myPaint.setColor(Color.GREEN);
//        myPaint.setStrokeWidth(10);
//        canvas.drawRect(100, 500, 200, 700, myPaint);

        Log.w(this.getClass().getName(), "On Draw Called - Tamanho da Imagem: " + tam);
    }

//    private void desenhar(Canvas canvas) {
//        Paint p = new Paint(Color.RED);
//        canvas.drawRect(0, 0, 400, 400, p);
//    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
/*        boolean retry = true;
        DotsThread.setRunning(false);
        while (retry) {
            try {
                DotsThread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }*/
        Log.d(TAG, "Valor do contador: " + count);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "Entrou no Surface Changed...");
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        try {
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
            {
                parameters.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);

                mCamera.setPreviewDisplay(holder);

                mCamera.startPreview();
            }
            else
            {
                // This is an undocumented although widely known feature
                parameters.set("orientation", "landscape");
                // For Android 2.2 and above
                mCamera.setDisplayOrientation(0);
                // Uncomment for Android 2.0 and above
                parameters.setRotation(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

        //Novidades de desenho colocadas aqui
/*        if (DotsThread==null){
            DotsThread = new DotsThread(mHolder);
            DotsThread.setRunning(true);
            DotsThread.setSurfaceSize(w, h);
            DotsThread.start();
        }*/
    }

    public void continuaPreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        Log.d(TAG, "Passou no continuaPreview...");
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}

