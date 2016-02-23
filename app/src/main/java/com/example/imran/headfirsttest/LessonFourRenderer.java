/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.example.imran.headfirsttest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;


import com.example.imran.headfirsttest.RawResourceReader;
import com.example.imran.headfirsttest.ShaderHelper;
import com.example.imran.headfirsttest.TextureHelper;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class LessonFourRenderer implements GLSurfaceView.Renderer
{
    private static final int COLOR_WALL=1;
    private static final int COLOR_BED=2;
    private static final int COLOR_DOOR=3;
    private static final int COLOR_MATTRESS =4;

    /** Used for debug logs. */
    private static final String TAG = "LessonFourRenderer";

    private int direction = 1;
    float upY;
    private final Context mActivityContext;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];

    /** Store our model data in a float buffer. */
    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColorswall;
    private final FloatBuffer mCubeColorsBed;
    private final FloatBuffer mCubeColorDoor;
    private final FloatBuffer mCubeColorMatress;
    private final FloatBuffer mCubeNormals;
    private final FloatBuffer mCubeTextureCoordinates;

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in the modelview matrix. */
    private int mMVMatrixHandle;

    /** This will be used to pass in the light position. */
    private int mLightPosHandle;

    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;
    private int mTextureUniformHandle1;
    private int mTextureUniformHandle2;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    /** This will be used to pass in model normal information. */
    private int mNormalHandle;

    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    /** Size of the normal data in elements. */
    private final int mNormalDataSize = 3;

    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    /** This is a handle to our cube shading program. */
    private int mProgramHandle;

    /** This is a handle to our light point program. */
    private int mPointProgramHandle;

    /** This is a handle to our texture data. */
    private int mTextureDataHandle;
    private int mTextureDataHandle1;
    private int mTextureDataHandle2;
    private int mTextureDataHandle3;
    private int mTextureDataHandlepaint1;
    private int mTextureDataHandlepaint2;
    private int mTextureDataHandlematress;
    private int mTextureDataHandlebed;
    private int mTextureDataHandlepillow;
    private int mTextureDataHandleclock;
    private int mTextureDataHandlefootcleaner;
    private int mTextureDataHandlesmallcupboard;
    private int mTextureDataHandlebigcupboard;
    private int mTextureDataHandletable;


    private int mCubecolorProgamHandle;

    /**
     * Initialize the model data.
     */
    public LessonFourRenderer(final Context activityContext)
    {
        mActivityContext = activityContext;

        final float[] cubeColorDatawall =
                {
                        // Front face (red)
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,

                        // Left face (green)
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,

                        // Back face (blue)
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,
                        0.87f, 0.84f, 0.77f, 1.0f,

                        // Right face (yellow)
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,
                        0.77f, 0.84f, 0.77f, 1.0f,

                        // Bottom face
                        0.78f, 0.76f, 0.73f, 1.0f,
                        0.78f, 0.76f, 0.73f, 1.0f,
                        0.78f, 0.76f, 0.73f, 1.0f,
                        0.78f, 0.76f, 0.73f, 1.0f,
                        0.78f, 0.76f, 0.73f, 1.0f,
                        0.78f, 0.76f, 0.73f, 1.0f,

                        // Top face (magenta)
                        0.86f, 0.83f, 0.73f, 1.0f,
                        0.86f, 0.83f, 0.73f, 1.0f,
                        0.86f, 0.83f, 0.73f, 1.0f,
                        0.86f, 0.83f, 0.73f, 1.0f,
                        0.86f, 0.83f, 0.73f, 1.0f,
                        0.86f, 0.83f, 0.73f, 1.0f,
                };

        final float[] cubeColorDataMatress =
                {
                        // Front face (grey)
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,

                        // Right face (light slate gray) //119,136,153
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,


                        // Back face //pale golden rod (238,232,170)
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,

                        // Left face //burly wood (222,184,135)
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,

                        // Top face //yellow green (154,205,50)
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,

                        // Bottom face (silver)
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                        1f, 1f, 1f, 1.0f,
                };

        // R, G, B, A
        final float[] cubeColorDataBed =
                {
                        // Front face (grey)
                        1f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,

                        // Right face (light slate gray) //119,136,153
                        0f, 1f, 1f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 0f, 1f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        1f, 0f, 0f, 1.0f,


                        // Back face //pale golden rod (238,232,170)
                        1f, 0f, 1f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        1f, 0f, 1f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,

                        // Left face //burly wood (222,184,135)
                        0f, 0f, 1f, 1.0f,
                        1f, 0f, 0f, 1.0f,
                        0f, 1f, 1f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,

                        // Top face //yellow green (154,205,50)
                        0f, 0f, 1f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 1f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 0f, 1f, 1.0f,
                        1f, 1f, 0f, 1.0f,


                        // Bottom face (silver)
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                        0f, 0f, 0f, 1.0f,
                };

        final float[] cubeColorDataDoor =
                {
                        // Front face (grey)
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,

                        // Right face (light slate gray) //119,136,153
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,


                        // Back face //pale golden rod (238,232,170)
                        0.93f, 0.90f, 0.67f, 1.0f,
                        0.93f, 0.90f, 0.67f, 1.0f,
                        0.93f, 0.90f, 0.67f, 1.0f,
                        0.93f, 0.90f, 0.67f, 1.0f,
                        0.93f, 0.90f, 0.67f, 1.0f,
                        0.93f, 0.90f, 0.67f, 1.0f,

                        // Left face //burly wood (222,184,135)
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,
                        0.78f, 0.7f, 0.5f, 1.0f,

                        // Top face //yellow green (154,205,50)
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,
                        0f, 1f, 0f, 1.0f,


                        // Bottom face (silver)
                        0.75f, 0f, 0.75f, 1.0f,
                        0.75f, 0f, 0.75f, 1.0f,
                        0.75f, 0f, 0.75f, 1.0f,
                        0.75f, 0.75f, 0.75f, 1.0f,
                        0.75f, 0.75f, 0.75f, 1.0f,
                        0.75f, 0.75f, 0.75f, 1.0f
                };

        // Define points for a cube.

        // X, Y, Z
        final float[] cubePositionData =
                {
                        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                        // usually represent the backside of an object and aren't visible anyways.

                        // Front face
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,

                        // Right face
                        1.0f, 1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Back face
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,

                        // Left face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, 1.0f,

                        // Top face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Bottom face
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                };




        final float[] cubeNormalData =
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,

                        // Bottom face
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f
                };


        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Right face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };

        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColorswall = ByteBuffer.allocateDirect(cubeColorDatawall.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColorswall.put(cubeColorDatawall).position(0);

        //Extra
        mCubeColorsBed=ByteBuffer.allocateDirect(cubeColorDataBed.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColorsBed.put(cubeColorDataBed).position(0);

        mCubeColorDoor=ByteBuffer.allocateDirect(cubeColorDataDoor.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColorDoor.put(cubeColorDataDoor).position(0);

        mCubeColorMatress=ByteBuffer.allocateDirect(cubeColorDataMatress.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColorMatress.put(cubeColorDataMatress).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
    }


    public void handleTouchPress(float normalizedX, float normalizedY) {


        direction *=-1;

    }

    public void handleTouchMove(int dir){
        Matrix.rotateM(mViewMatrix, 0, dir*1.0f, 0.0f, 1.0f, 0.0f);
    }

    protected String getVertexShader()
    {
        return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader_tex_and_light);
    }

    protected String getFragmentShader()
    {
        return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader_tex_and_light);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        final float eyeX = 0.1f;
        final float eyeY = 0.0f;
        final float eyeZ = -3.15f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -10.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
         upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[] {"a_Position",  "a_Color", "a_Normal", "a_TexCoordinate"});

        // Define a simple shader program for our point.
        final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);
        final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);

        final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[] {"a_Position"});


        //load of normal color cube programm
        final String vertexShaderSource2 = TextResourceReader
                .readTextFileFromResource(mActivityContext, R.raw.per_pixel_vertex_shader_no_tex);
        final String fragmentShaderSource2 = TextResourceReader
                .readTextFileFromResource(mActivityContext, R.raw.per_pixel_fragment_shader_no_tex);

        int vertexShader2 = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource2);
        int fragmentShader2 = ShaderHelper
                .compileShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderSource2);

        mCubecolorProgamHandle = ShaderHelper.createAndLinkProgram(vertexShader2, fragmentShader2, new String[] {"a_Position",  "a_Color", "a_Normal"});

        /*if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(textureprogram);
        }*/


        // Load the texture
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.door1);
        mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.brick_texture);
        mTextureDataHandle2= TextureHelper.loadTexture(mActivityContext,R.drawable.stone36);
        mTextureDataHandle3= TextureHelper.loadTexture(mActivityContext,R.drawable.upperwall);
        mTextureDataHandlepaint1=TextureHelper.loadTexture(mActivityContext,R.drawable.paint1);
        mTextureDataHandlepaint2=TextureHelper.loadTexture(mActivityContext,R.drawable.paint2);
        mTextureDataHandlematress=TextureHelper.loadTexture(mActivityContext,R.drawable.bed_matress);
        mTextureDataHandlebed=TextureHelper.loadTexture(mActivityContext,R.drawable.bed_texture2);
        mTextureDataHandlepillow=TextureHelper.loadTexture(mActivityContext,R.drawable.pillow);
        mTextureDataHandleclock=TextureHelper.loadTexture(mActivityContext,R.drawable.clock1);
        mTextureDataHandlefootcleaner= TextureHelper.loadTexture(mActivityContext,R.drawable.pillow);
        mTextureDataHandlesmallcupboard=TextureHelper.loadTexture(mActivityContext,R.drawable.small_cupboard);
        mTextureDataHandlebigcupboard=TextureHelper.loadTexture(mActivityContext,R.drawable.big_cupboard);
        mTextureDataHandletable=TextureHelper.loadTexture(mActivityContext,R.drawable.glass2);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width/ height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mLightModelMatrix, 0, direction*angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        //Door
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.25f, -0.4f, -4.4f);
        Matrix.rotateM(mModelMatrix, 0, 0, 0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, 0.045f, 0.8f, 0.1f);
        drawCube();

        //Floor

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -2.2f, -9.0f);
        Matrix.scaleM(mModelMatrix, 0, 4.0f, 0.1f, 3.8f);
       // mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.brick_texture);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        drawCube();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle2);
        GLES20.glUniform1i(mTextureUniformHandle2, 0);
        //Left wall
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 2.0f, 2.0f);
        drawCube();

        //Right Wall
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 2.0f, 2.0f);
        drawCube();


        //Back Wall
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -9.0f);
        Matrix.scaleM(mModelMatrix, 0, 4.0f, 2.0f, 0.5f);
        drawCube();


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle3);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //Roof
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 2.2f, -9.0f);
        Matrix.scaleM(mModelMatrix, 0, 4.0f, 0.1f, 3.8f);
        drawCubeWithColor(COLOR_WALL);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlepaint1);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //painting(left wall)
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -3.0f, 0.0f, -5.5f);
        Matrix.scaleM(mModelMatrix, 0, 0.01f, .4f, .4f);
        drawCubeWithColor(COLOR_DOOR);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlepaint2);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //painting(right wall)
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 3.0f, 0.0f, -5.5f);
        Matrix.scaleM(mModelMatrix, 0, 0.01f, .4f, .4f);
        drawCubeWithColor(COLOR_WALL);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlematress);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //Bed Matress
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -1.7f, -7.1f);
        //Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, .9f, .005f, 1.2f);
        drawCubeWithColor(COLOR_MATTRESS);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlebed);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);

        //Bed
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -1.8f, -9.0f);
        // Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, 1f, .06f, 3.0f);
        drawCubeWithColor(COLOR_BED);
        //Bed-Side
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -1.6f, -8.5f);
        //Matrix.rotateM(modelMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, 1f, .2f, 0.05f);
        drawCubeWithColor(COLOR_BED);



        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlepillow);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //pillow1
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -0.5f, -1.5f, -7.5f);
        Matrix.scaleM(mModelMatrix, 0, .2f, .01f, 0.2f);
        drawCubeWithColor(COLOR_DOOR);

        //pillow2
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.5f, -1.5f, -7.5f);
        Matrix.scaleM(mModelMatrix, 0, .2f, .01f, 0.2f);
        drawCubeWithColor(COLOR_DOOR);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandleclock);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //AC
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -3.0f, 1.5f, -8.5f);
        Matrix.scaleM(mModelMatrix, 0, .3f, .3f, 0.001f);
        drawCubeWithColor(COLOR_WALL);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlebigcupboard);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //wardrobe
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -2.6f, -1.385f, -8.2f);
        //Matrix.rotateM(mModelMatrix, 0,angleInDegrees, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, .55f, 1.2f, 0.02f);
        drawCubeWithColor(COLOR_DOOR);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlefootcleaner);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //foot cleaner
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, -4.4f);
        Matrix.scaleM(mModelMatrix, 0, .3f, 0.001f, .1f);
        drawCubeWithColor(COLOR_DOOR);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandlesmallcupboard);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //small box
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -1.5f, -1.80f, -8.2f);
        //Matrix.rotateM(mModelMatrix, 0,90, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, .3f, 0.25f, 0.01f);
        drawCubeWithColor(COLOR_DOOR);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandletable);
        GLES20.glUniform1i(mTextureUniformHandle1, 0);
        //Table
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.6f, -1.5f, -9.0f);
        //Matrix.rotateM(mModelMatrix, 0,180, 1.0f, 0.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, .6f, 0.05f, 1f);
        drawCubeWithColor(COLOR_DOOR);



        GLES20.glUseProgram(mCubecolorProgamHandle);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mCubecolorProgamHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mCubecolorProgamHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mCubecolorProgamHandle, "u_LightPos");

        mPositionHandle = GLES20.glGetAttribLocation(mCubecolorProgamHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mCubecolorProgamHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mCubecolorProgamHandle, "a_Normal");






        //table-right leg
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.7f, -1.60f, -7.6f);
        Matrix.rotateM(mModelMatrix, 0,90, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, 0.05f, 0.16f, 0.03f);
        drawCubeWithColor(COLOR_MATTRESS);
        //table-left leg
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.0f, -1.60f, -7.6f);
        Matrix.rotateM(mModelMatrix, 0,112.5f, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, 0.05f, 0.16f, 0.03f);
        drawCubeWithColor(COLOR_MATTRESS);

        //chair-downs
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.5f, -1.7f, -7.5f);
        Matrix.rotateM(mModelMatrix, 0,180f,0f, 1.0f, 0.0f);//changing for colour view
       // Matrix.rotateM(mModelMatrix, 0,45, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, 0.25f, 0.03f, 0.1f);
        drawCubeWithColor(COLOR_WALL);

        // chair-right leg
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 2.6f, -1.80f, -7.6f);
        Matrix.rotateM(mModelMatrix, 0,90, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, 0.05f, 0.1f, 0.04f);
        drawCubeWithColor(COLOR_MATTRESS);


        // cube above small box
         Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -1.42f, -1.385f, -8.0f);
        Matrix.rotateM(mModelMatrix, 0,angleInDegrees, 0.0f, 1.0f, 0.0f); //changing for colour view
        Matrix.scaleM(mModelMatrix, 0, .1f, 0.1f, 0.01f);
        drawCubeWithColor(COLOR_BED);

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();
    }




    /**
     * Draws a cube.
     */
    private void drawCube()
    {
        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        /*mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                0, mCubeColors);*/

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    private void drawCubeWithColor(int val)
    {
        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information

        if(val==COLOR_WALL) {


            mCubeColorswall.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                    0, mCubeColorswall);
        }
        else if(val==COLOR_BED){

            mCubeColorsBed.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                    0, mCubeColorsBed);
        }
        else if (val==COLOR_DOOR){
            mCubeColorDoor.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                    0, mCubeColorDoor);
        }
        else if(val==COLOR_MATTRESS){
            mCubeColorMatress.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                    0, mCubeColorMatress);
        }

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // Pass in the texture coordinate information


        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
}
