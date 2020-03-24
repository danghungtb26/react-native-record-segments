package com.dvhchuot.rnrecord.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.dvhchuot.rnrecord.camera.CameraInstance;
import com.dvhchuot.rnrecord.texUtils.TextureRenderer;
import com.dvhchuot.rnrecord.texUtils.TextureRendererBlur;
import com.dvhchuot.rnrecord.texUtils.TextureRendererDrawOrigin;
import com.dvhchuot.rnrecord.texUtils.TextureRendererEdge;
import com.dvhchuot.rnrecord.texUtils.TextureRendererEmboss;
import com.dvhchuot.rnrecord.texUtils.TextureRendererLerpBlur;
import com.dvhchuot.rnrecord.texUtils.TextureRendererWave;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.dvhchuot.rnrecord.texUtils.Filter.*;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_Blur;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_BlurLerp;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_Edge;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_Emboss;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_Origin;
import static com.dvhchuot.rnrecord.texUtils.Filter.Filter_Wave;


public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public static final String LOG_TAG = "asdasdas";

    public int viewWidth;
    public int viewHeight;

    private TextureRenderer mMyRenderer;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureID;

    private Context mContext;

    private float[] mTransformMatrix = new float[16];
    public TextureRenderer.Viewport drawViewport = new TextureRenderer.Viewport();

    public class ClearColor {
        public float r, g, b, a;
    }

    public ClearColor clearColor;

    public void setClearColor(float r, float g, float b, float a) {
        clearColor.r = r;
        clearColor.g = g;
        clearColor.b = b;
        clearColor.a = a;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
            }
        });
    }

    public void setFilter(String filterID) {
        setFrameRenderer(filterID);
    }

    public synchronized void setFrameRenderer(final String filterID) {
        Log.i(LOG_TAG, "setFrameRenderer to " + filterID);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                TextureRenderer renderer = null;
                boolean isExternalOES = true;
                switch (filterID) {
                    case Filter_Origin:
                        renderer = TextureRendererDrawOrigin.create(isExternalOES);
                        break;
                    case Filter_Wave:
                        renderer = TextureRendererWave.create(isExternalOES);
                        if (renderer != null)
                            ((TextureRendererWave) renderer).setAutoMotion(0.4f);
                        break;
                    case Filter_Blur:
                        renderer = TextureRendererBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((TextureRendererBlur) renderer).setSamplerRadius(50.0f);
                        }
                        break;
                    case Filter_Edge:
                        renderer = TextureRendererEdge.create(isExternalOES);
                        break;
                    case Filter_Emboss:
                        renderer = TextureRendererEmboss.create(isExternalOES);
                        break;
                    case Filter_BlurLerp:
                        renderer = TextureRendererLerpBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((TextureRendererLerpBlur) renderer).setIntensity(16);
                        }
                        break;
                    default:
                        break;
                }

                if (renderer != null) {
                    mMyRenderer.release();
                    mMyRenderer = renderer;
                    mMyRenderer.setTextureSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
                    mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
                    mMyRenderer.setFlipscale(1.0f, 1.0f);
                }

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

//                Common.checkGLError("setFrameRenderer...");
            }
        });

    }

    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    public CameraGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        Log.e(LOG_TAG, "MyGLSurfaceView Construct...");

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
//        setZOrderOnTop(true);

        clearColor = new ClearColor();

        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(LOG_TAG, "onSurfaceCreated...");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);

        mTextureID = genSurfaceTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        TextureRendererDrawOrigin rendererWave = new TextureRendererDrawOrigin();
        if(!rendererWave.init(true)) {
            Log.e(LOG_TAG, "init filter failed!\n");
        }

//        rendererWave.setAutoMotion(0.0f);

        mMyRenderer = rendererWave;
        mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
        mMyRenderer.setFlipscale(1.0f, 1.0f);

        requestRender();
        createCamera();

    }

    private int mRecordWidth = 540;
    private int mRecordHeight = 960;

    protected int mMaxPreviewWidth = 1280;
    protected int mMaxPreviewHeight = 1280;

    public void presetRecordingSize(int width, int height) {
        if (width > mMaxPreviewWidth || height > mMaxPreviewHeight) {
            float scaling = Math.min(mMaxPreviewWidth / (float) width, mMaxPreviewHeight / (float) height);
            width = (int) (width * scaling);
            height = (int) (height * scaling);
        }

        mRecordWidth = width;
        mRecordHeight = height;
        cameraInstance().setPreferPreviewSize(width, height);
    }

    public void createCamera() {
        int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
            @Override
            public void cameraReady() {
                Log.e(LOG_TAG, "tryOpenCamera OK...");
                mMyRenderer.setTextureSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
            }
        }, facing);
    }

    public void reCreateCamera() {
        int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
            @Override
            public void cameraReady() {
                Log.e(LOG_TAG, "tryOpenCamera OK...");
//                setPreview();
                resumePreview();
            }
        }, facing);
    }


    public void calcViewport() {
        float camHeight = (float)cameraInstance().previewWidth();
        float camWidth = (float)cameraInstance().previewHeight();
        float scaling = mRecordWidth / (float) mRecordHeight;
        float viewRatio = viewWidth / (float) viewHeight;
        float s = scaling / viewRatio;

        int h = viewHeight;
        int w = (int) (viewHeight * camWidth / camHeight);

        Log.e("camera size ", "camera: w: " + camWidth +  "  h: " + camHeight + "  preview: w: " + viewWidth + " h: " + viewHeight);
        int x = (int) ((viewWidth - w) / 2) ;
        drawViewport.width =(int) w;
        drawViewport.height = (int)h;
        drawViewport.x = x;
        drawViewport.y = 0;

        int startX = 0, startY = 0;
        int cutX = mRecordWidth, cutY = mRecordHeight;

        if(s > 1) {
            cutX = viewWidth;
            cutY = (int) (viewWidth / scaling);
            startY = (int) (viewHeight - cutY) / 2;
        }
        else {
            cutX = (int) (viewHeight * scaling);
            cutY = viewHeight;
            startX = (int) (viewWidth - cutX) / 2;
        }

        drawViewport.startX = startX;
        drawViewport.startY = startY;
        drawViewport.cutX = cutX;
        drawViewport.cutY = cutY;

//        float camHeight = (float)cameraInstance().previewWidth();
//        float camWidth = (float)cameraInstance().previewHeight();
//
//        drawViewport = new TextureRenderer.Viewport();
//
//        float scale = Math.min(viewWidth / camWidth, viewHeight / camHeight);
//        drawViewport.width = (int)(camWidth * scale);
//        drawViewport.height = (int)(camHeight * scale);
//        drawViewport.x = (viewWidth - drawViewport.width) / 2;
//        drawViewport.y = (viewHeight - drawViewport.height) / 2;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(LOG_TAG, String.format("onSurfaceChanged: %d x %d", width, height));

        GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

        viewWidth = width;
        viewHeight = height;

        setPreview();
        calcViewport();
    }

    public void setPreview() {
        if(!cameraInstance().isPreviewing()) {
            cameraInstance().startPreview(mSurfaceTexture);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
//        mMyRenderer.release();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        if(mMyRenderer == null || mSurfaceTexture == null) {
            Log.e(LOG_TAG, "Invalid Texture Renderer!");
            return;
        }

        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        mMyRenderer.setTransform(mTransformMatrix);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mMyRenderer.renderTexture(mTextureID, drawViewport);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        requestRender();
    }

    private int genSurfaceTextureID() {
        int[] texID = new int[1];
        GLES20.glGenTextures(1, texID, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texID[0];
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    protected boolean mIsCameraBackForward = true;

    public boolean isCameraBackForward() {
        return mIsCameraBackForward;
    }

    //should be called before 'onSurfaceCreated'.
    public void presetCameraForward(boolean isBackForward) {
        mIsCameraBackForward = isBackForward;
    }

    public synchronized boolean setFlashLightMode(String mode) {

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(LOG_TAG, "No flash light is supported by current device!");
            return false;
        }

        if (!mIsCameraBackForward) {
            return false;
        }

        Camera.Parameters parameters = cameraInstance().getParams();

        if (parameters == null)
            return false;

        try {

            if (!parameters.getSupportedFlashModes().contains(mode)) {
                Log.e(LOG_TAG, "Invalid Flash Light Mode!!!");
                return false;
            }

            parameters.setFlashMode(mode);
            cameraInstance().setParams(parameters);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Switch flash light failed, check if you're using front camera.");
            return false;
        }

        return true;
    }

    public void stopPreview() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                cameraInstance().stopPreview();
            }
        });
    }

    protected void onSwitchCamera() {

    }

    public synchronized void switchCamera() {
        if(mMyRenderer != null) {
            mIsCameraBackForward = !mIsCameraBackForward;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(mMyRenderer == null) {
                        Log.e(LOG_TAG, "Error: switchCamera after release!!");
                        mIsCameraBackForward = !mIsCameraBackForward;
                        return;
                    }
                    cameraInstance().stopCamera();
                    onSwitchCamera();
                    int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
                    mMyRenderer.setFlipscale(1.0f, 1.0f);


                    cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                        @Override
                        public void cameraReady() {
                            resumePreview();
                        }
                    }, facing);

                    requestRender();
                }
            });
        }
    }

    public void resumePreview() {
        cameraInstance().startPreview(mSurfaceTexture);
        mMyRenderer.setTextureSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
    }
}
