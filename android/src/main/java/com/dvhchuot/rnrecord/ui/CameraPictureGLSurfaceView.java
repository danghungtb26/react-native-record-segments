package com.dvhchuot.rnrecord.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;

import com.dvhchuot.rnrecord.texUtils.FrameBufferObject;
import com.dvhchuot.rnrecord.utils.CameraHelper;
import com.dvhchuot.rnrecord.utils.Common2;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraPictureGLSurfaceView extends CameraGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {
    Context context;

    private cameraPictureListener listener;

    private boolean mIniting = true;

    private boolean isTaking = false;

    private boolean isFocused = true;

    public CameraPictureGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e("TAG_AA", "init 3");
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        mIniting = false;
        if(listener != null ) {
            listener.onCameraReady();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    public synchronized void takePicture() {
        if(isTaking) return;
        isTaking = true;
        if(listener != null) {
            listener.onTakePictureStart();
        }
//        new TakePictureTask().execute();

        queueEvent(new Runnable() {
            @Override
            public void run() {
                FrameBufferObject frameBufferObject = new FrameBufferObject();
                int bufferTexID;
                IntBuffer buffer;
                Bitmap bmp;

//                bufferTexID = Common2.genBlankTextureID(drawViewport.width, drawViewport.height);
//                frameBufferObject.bindTexture(bufferTexID);
                buffer = IntBuffer.allocate(drawViewport.cutX * drawViewport.cutY);

                GLES20.glReadPixels(drawViewport.startX, drawViewport.startX, drawViewport.cutX,  drawViewport.cutY, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
                bmp = Bitmap.createBitmap(drawViewport.cutX,  drawViewport.cutY, Bitmap.Config.ARGB_8888);
                buffer.rewind();
                bmp.copyPixelsFromBuffer(buffer);

                Matrix matrix = new Matrix();

                matrix.postScale(1, -1, bmp.getWidth() / 2f, bmp.getHeight() / 2f);

                Bitmap bm2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                bmp.recycle();
//                frameBufferObject.release();
//                GLES20.glDeleteTextures(1, new int[]{bufferTexID}, 0);

                String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File mFile = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_IMAGE, context);
                try {
                    mFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //write the bytes in file

                try {
                    FileOutputStream fileout = new FileOutputStream(mFile);
                    BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
                    bm2.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream);
                    bufferOutStream.flush();
                    bufferOutStream.close();
                    bm2.recycle();
                    if(listener != null) {
                        listener.onTakePictureDone(mFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    if(listener != null) {
                        listener.onTakePictureError();
                    }
                    e.printStackTrace();
                }

            }
        });
    }

    class TakePictureTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            FrameBufferObject frameBufferObject = new FrameBufferObject();
            int bufferTexID;
            IntBuffer buffer;
            Bitmap bmp;

            bufferTexID = Common2.genBlankTextureID(drawViewport.width, drawViewport.height);
            frameBufferObject.bindTexture(bufferTexID);
            buffer = IntBuffer.allocate(drawViewport.width * drawViewport.height);

            GLES20.glReadPixels(0, 0, drawViewport.width,  drawViewport.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
            bmp = Bitmap.createBitmap(drawViewport.width,  drawViewport.height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buffer);

            frameBufferObject.release();
            GLES20.glDeleteTextures(1, new int[]{bufferTexID}, 0);

            String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mFile = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_IMAGE);
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //write the bytes in file

            try {
                FileOutputStream fos = new FileOutputStream(mFile.getAbsolutePath());
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
                bos.flush();
                bos.close();
                bmp.recycle();

                if(listener != null) {
                    listener.onTakePictureDone(mFile.getAbsolutePath());
                }
            } catch (IOException e) {
                if(listener != null) {
                    listener.onTakePictureError();
                }
                e.printStackTrace();
            }



            return null;
        }
    }

    public void onResumeCamera() {
        if(!mIniting && this.isFocused) {
            reCreateCamera();
        }
    }

    public void onDestroyCamera() {
        cameraInstance().stopCamera();
    }

    public void setIsFocused(boolean isFocused) {
        if(isFocused) {
            if(!this.isFocused) {
                this.isTaking = false;
                reCreateCamera();
            }
        }
        else {
            if(this.isFocused) {
                onDestroyCamera();
            }
        }
        this.isFocused = isFocused;
    }

    public boolean ismIniting() {
        return mIniting;
    }

    public void setmIniting(boolean mIniting) {
        this.mIniting = mIniting;
    }

    public void setListener(cameraPictureListener listener) {
        this.listener = listener;
    }

    public boolean checkInitingOrTaking() {
        return ismIniting() || isTaking;
    }

    public interface cameraPictureListener {
        public void onCameraReady();
        public void onTakePictureStart();
        public void onTakePictureError();
        public void onTakePictureDone(String file);
    }


}
