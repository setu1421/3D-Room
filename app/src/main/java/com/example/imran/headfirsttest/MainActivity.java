package com.example.imran.headfirsttest;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.View.OnTouchListener;
import android.view.MenuItem;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {
    /**
     * Hold a reference to our GLSurfaceView
     */

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    LessonFourRenderer lessonFourRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        gestureDetector= new GestureDetector(this,new OnSwipeGestureListener());

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));


         lessonFourRenderer = new LessonFourRenderer(this);


        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.
            glSurfaceView.setRenderer(lessonFourRenderer);
            rendererSet = true;
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since
             * we're not doing anything, the app will crash if the device
             * doesn't support OpenGL ES 2.0. If we publish on the market, we
             * should also add the following to AndroidManifest.xml:
             *
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             *
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(glSurfaceView);

        final float[] oldTouch = new float[1];
        final float[] finalTouch = new float[1];

        /*glSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    // Convert touch coordinates into normalized device
                    // coordinates, keeping in mind that Android's Y
                    // coordinates are inverted.

                    final float normalizedX =
                            (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY =
                            -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    oldTouch[0] =normalizedX;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                //lessonFourRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        finalTouch[0] = (event.getX() / (float) v.getWidth()) * 2 - 1;

                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {

                                if(finalTouch[0]>oldTouch[0]) {
                                    lessonFourRenderer.handleTouchMove(
                                            normalizedX, normalizedY,-1);
                                }
                                else{
                                    lessonFourRenderer.handleTouchMove(
                                            normalizedX, normalizedY,1);
                                }
                            }
                        });
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });*/


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class OnSwipeGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            float deltaX = e2.getX() - e1.getX();
            if ((Math.abs(deltaX) < SWIPE_MIN_DISTANCE)
                    || (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY)) {
                return false; // insignificant swipe
            } else {
                if (deltaX < 0) { // left to right
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            lessonFourRenderer.handleTouchMove(-1);
                        }
                    });

                } else { // right to left
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            lessonFourRenderer.handleTouchMove(1);
                        }
                    });

                }
            }
            return true;
        }
    }

    private void handleSwipeLeftToRight() {

    }

    private void handleSwipeRightToLeft() {

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}