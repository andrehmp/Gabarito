package com.multiprova.andre.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.lang.Math.*;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint3;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

public class Main2Activity extends AppCompatActivity {
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ImageView  result = (ImageView) findViewById(R.id.camera_result);
        Bundle b = getIntent().getExtras();
        byte[] image = b.getByteArray("image");
        Bitmap btm = ImageOperation2(image);
        result.setImageBitmap(btm);
    }


    private Bitmap ImageOperation2(byte[] image) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        Mat source = new Mat();
        Utils.bitmapToMat(bmp, source);
        Imgproc.blur(source, source, new Size(3, 3));
        Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY, 0);


        source.convertTo(source, CvType.CV_8UC1, 255.0);
        Imgproc.adaptiveThreshold(source, source, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 301,100);
        Imgproc.Canny(source, source, 100, 100);

        Mat mLines = new Mat(source.rows(), source.cols(), CvType.CV_8UC1);
        Imgproc.HoughLinesP(source,mLines, 1, Math.PI/180, 100, 50, 40);

        LinkedList<LinkedList<Integer>> lines = new LinkedList<>();
        for (int i = 0; i < mLines.cols(); i++)
        {
            double vCircle[] = mLines.get(0,i);
            LinkedList<Integer> l= new LinkedList<>();
            for (int j = 0; j <4; j++) {
                int aux=new Integer((int)vCircle[j]);
                l.add(aux);
            }
            lines.add(l);
        }
        LinkedList<Point> corners = getCorners(lines);

        double x1 = 0;
        double y1 = source.height();
        double x2 = source.width();
        double y2 = source.height();
        double x3 = source.width();
        double y3 = 0;
        double x4 = 0;
        double y4 = 0;

        for (int i = 0; i <corners.size(); i++) {
            double x = corners.get(i).x;
            double y = corners.get(i).y;
            if(x>0&&y>0&&y<source.height()&&x<source.width()) {
                if (Math.sqrt((x - source.width())*(x - source.width()) + (y - 0)*(y - 0))<Math.sqrt((x1 - source.width())*(x1 - source.width()) + (y1 - 0)*(y1 - 0))) {
                    x1 = x;
                    y1 = y;
                }
                if (Math.sqrt((x - 0)*(x - 0) + (y - 0)*(y - 0))<Math.sqrt((x2 - 0)*(x2 - 0) + (y2 - 0)*(y2 - 0))) {
                    x2 = x;
                    y2 = y;
                }
                if (Math.sqrt((x - 0)*(x - 0) + (y - source.height())*(y - source.height()))<Math.sqrt((x3 - 0)*(x3 - 0) + (y3 - source.height())*(y3 - source.height()))) {
                    x3 = x;
                    y3 = y;
                }
                if (Math.sqrt((x - source.width())*(x - source.width()) + (y - source.height())*(y - source.height()))<Math.sqrt((x4 - source.width())*(x4 - source.width()) + (y4 - source.height())*(y4 - source.height()))){
                    x4 = x;
                    y4 = y;
                }
            }
        }




        Mat src = new Mat(4,1,CvType.CV_32FC2);
        src.put(0,0,x2, y2);
        src.put(1,0,x3, y3);
        src.put(2,0,x4, y4);
        src.put(3,0,x1, y1);
        Mat dst = new Mat(4,1,CvType.CV_32FC2);
        dst.put(0,0,0,0);
        dst.put(1,0,0,source.rows());
        dst.put(2, 0, source.cols(), source.rows());
        dst.put(3, 0, source.cols(), 0);
        Mat transmtx = Imgproc.getPerspectiveTransform(src, dst);

        Mat cropped_image = source.clone();
        Utils.bitmapToMat(bmp, cropped_image);
        Imgproc.warpPerspective(cropped_image, cropped_image, transmtx, new Size(source.width(), source.height()));
        boolean[][] ito=getSets(cropped_image);
        boolean[][] ito2=getSets2(cropped_image);
        boolean[][] ito3=getSets3(cropped_image);






        Utils.matToBitmap(cropped_image, bmp);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(Color.GREEN);
        p.setStrokeWidth(10);

        y1=(int)(1.5/13*cropped_image.width());
        y2=(int)(1.5/13*cropped_image.width());
        y3=(int)(5.4/13*cropped_image.width());
        y4=(int)(5.4/13*cropped_image.width());
        x1=(int)(3/8.7*cropped_image.height());
        x2=(int)(7.4/8.7*cropped_image.height());
        x3=(int)(3/8.7*cropped_image.height());
        x4=(int)(7.4/8.7*cropped_image.height());
        for (int i=0;i<10;i++){
            for (int j=0;j<10;j++){
                if (ito[i][j]==false)
                    canvas.drawRect((int)(y1+j*(y3-y1)/10),(int)(x1+i*(x2-x1)/10),(int)(y1+(j+1)*(y3-y1)/10),(int)(x1+(1+i)*(x2-x1)/10),p);
            }
        }

        y1=(int)(5.9/13*cropped_image.width());
        y2=(int)(5.9/13*cropped_image.width());
        y3=(int)(7.1/13*cropped_image.width());
        y4=(int)(7.1/13*cropped_image.width());
        x1=(int)(2.8/8.7*cropped_image.height());
        x2=(int)(6.7/8.7*cropped_image.height());
        x3=(int)(2.8/8.7*cropped_image.height());
        x4=(int)(6.7/8.7*cropped_image.height());
        p.setColor(Color.RED);
        for (int i=0;i<10;i++){
            for (int j=0;j<3;j++){
                if (ito2[i][j]==false)
                    canvas.drawRect((int)(y1+j*(y3-y1)/3),(int)(x1+i*(x2-x1)/10),(int)(y1+(j+1)*(y3-y1)/3),(int)(x1+(1+i)*(x2-x1)/10),p);
            }
        }

        y1=(int)(7.8/13*cropped_image.width());
        y2=(int)(7.8/13*cropped_image.width());
        y3=(int)(12.5/13*cropped_image.width());
        y4=(int)(12.5/13*cropped_image.width());
        x1=(int)(2.3/8.7*cropped_image.height());
        x2=(int)(6.15/8.7*cropped_image.height());
        x3=(int)(2.3/8.7*cropped_image.height());
        x4=(int)(6.15/8.7*cropped_image.height());
        p.setColor(Color.BLUE);
        for (int i=0;i<10;i++){
            for (int j=0;j<10;j++){
                if (ito3[i][j]==false)
                    canvas.drawRect((int)(y1+j*(y3-y1)/10),(int)(x1+i*(x2-x1)/10),(int)(y1+(j+1)*(y3-y1)/10),(int)(x1+(1+i)*(x2-x1)/10),p);
            }
        }
        return bmp;

    }


    private boolean[][] getSets(Mat m_){

        Mat m=new Mat();
        Size size = new Size();
        Imgproc.GaussianBlur(m_,m, size, 0.0001);
        int x1,x2,x3,x4,y1,y2,y3,y4;
        //p1 p2
        //p3 p4
        y1=(int)(1.5/13*m.width());
        y2=(int)(1.5/13*m.width());
        y3=(int)(5.4/13*m.width());
        y4=(int)(5.4/13*m.width());
        x1=(int)(3/8.7*m.height());
        x2=(int)(7.4/8.7*m.height());
        x3=(int)(3/8.7*m.height());
        x4=(int)(7.4/8.7*m.height());

        boolean[][] gabarito= new boolean[10][10];
        for (int i=0;i<10;i++){
            for (int j=0;j<10;j++){

                Rect rect = new Rect((int)(y1+j*(y3-y1)/10),(int)(x1+i*(x2-x1)/10),(y3-y1)/10,(x2-x1)/10);
                Mat roi = new Mat(m,rect);
                Mat mGray = new Mat(roi.rows(), roi.cols(), CvType.CV_8UC1);
                Imgproc.cvtColor(roi, mGray, Imgproc.COLOR_BGRA2GRAY);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                //Core.extractChannel(roi, aux,2);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);


                if (Core.countNonZero(mGray) > 400)
                    gabarito[i][j]=true;
                else
                    gabarito[i][j]=false;
            }
        }
        return gabarito;
    }
    private boolean[][] getSets2(Mat m_){
        Mat m=new Mat();
        Size size = new Size();
        Imgproc.GaussianBlur(m_,m, size, 0.0001);
        int x1,x2,x3,x4,y1,y2,y3,y4;
        //p1 p2
        //p3 p4
        y1=(int)(5.9/13*m.width());
        y2=(int)(5.9/13*m.width());
        y3=(int)(7.1/13*m.width());
        y4=(int)(7.1/13*m.width());
        x1=(int)(2.8/8.7*m.height());
        x2=(int)(6.7/8.7*m.height());
        x3=(int)(2.8/8.7*m.height());
        x4=(int)(6.7/8.7*m.height());

        boolean[][] gabarito= new boolean[10][10];
        for (int i=0;i<10;i++){
            for (int j=0;j<3;j++){
                Rect rect = new Rect((int)(y1+j*(y3-y1)/3),(int)(x1+i*(x2-x1)/10),(y3-y1)/3,(x2-x1)/10);
                Mat roi = new Mat(m,rect);
                Mat mGray = new Mat(roi.rows(), roi.cols(), CvType.CV_8UC1);
                Imgproc.cvtColor(roi, mGray, Imgproc.COLOR_BGRA2GRAY);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                //Core.extractChannel(roi, aux,2);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);


                if (Core.countNonZero(mGray) > 400)
                    gabarito[i][j]=true;
                else
                    gabarito[i][j]=false;
            }
        }
        return gabarito;
    }
    private boolean[][] getSets3(Mat m_){
        Mat m=new Mat();
        Size size = new Size();
        Imgproc.GaussianBlur(m_,m, size, 0.0001);
        int x1,x2,x3,x4,y1,y2,y3,y4;
        //p1 p2
        //p3 p4
        y1=(int)(7.8/13*m.width());
        y2=(int)(7.8/13*m.width());
        y3=(int)(12.5/13*m.width());
        y4=(int)(12.5/13*m.width());
        x1=(int)(2.3/8.7*m.height());
        x2=(int)(6.15/8.7*m.height());
        x3=(int)(2.3/8.7*m.height());
        x4=(int)(6.15/8.7*m.height());

        boolean[][] gabarito= new boolean[10][10];
        for (int i=0;i<10;i++){
            for (int j=0;j<10;j++){
                Rect rect = new Rect((int)(y1+j*(y3-y1)/10),(int)(x1+i*(x2-x1)/10),(y3-y1)/10,(x2-x1)/10);
                Mat roi = new Mat(m,rect);
                Mat mGray = new Mat(roi.rows(), roi.cols(), CvType.CV_8UC1);
                Imgproc.cvtColor(roi, mGray, Imgproc.COLOR_BGRA2GRAY);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                //Core.extractChannel(roi, aux,2);
                mGray.convertTo(mGray, CvType.CV_8UC1, 255.0);
                Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);


                if (Core.countNonZero(mGray) > 400)
                    gabarito[i][j]=true;
                else
                    gabarito[i][j]=false;
            }
        }
        return gabarito;
    }
    private Point computeIntersect(LinkedList<Integer> a, LinkedList<Integer>  b)
    {

        int x1 = a.get(0);
        int y1 = a.get(1);
        int x2 = a.get(2);
        int y2 = a.get(3);
        int x3 = b.get(0), y3 = b.get(1), x4 = b.get(2), y4 = b.get(3);
         	float d = ((float)(x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));


         	if (d!=0)
         	{
                Point pt = new Point();
            	pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
             	pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
             	return pt;
            } else
         		return new Point(-1, -1);
    }
    private LinkedList<Point> getCorners(  LinkedList<LinkedList<Integer>> lines)
    {
        LinkedList<Point> corners= new LinkedList<>();
         	for (int i = 0; i < lines.size(); i++)
         	{
         		for (int j = i+1; j < lines.size(); j++)
            	{
             			Point pt = computeIntersect(lines.get(i), lines.get(j));
             			if (pt.x >= 0 && pt.y >= 0)
                 				corners.add(pt);}
         	}
        return corners;
    }

    public void returnToMain(View v) {
        Intent i = new Intent(Main2Activity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Main2Activity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

}

