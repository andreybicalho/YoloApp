package com.example.yoloapp;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;

    private Mat currentFrame;

    private Yolo yolo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentFrame = inputFrame.rgba();

        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2RGB);
        yolo.detect(currentFrame);

        return currentFrame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        yolo = new Yolo(this,
                416, 416,
                "coco.names",
                "yolov3-tiny.cfg",
                "yolov3-tiny.weights",
                0.3f,
                0.2f);
    }

    @Override
    public void onCameraViewStopped() {
        currentFrame.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV debug couldn't load properly!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
            return;
        }

        baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }
}
