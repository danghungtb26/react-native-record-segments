package com.dvhchuot.rnrecord.ui;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dvhchuot.rnrecord.data.ReactProps;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.dvhchuot.rnrecord.R;

public class PicktureView extends ConstraintLayout implements LifecycleEventListener, View.OnClickListener, CameraPictureGLSurfaceView.cameraPictureListener {

    private ThemedReactContext context;
    private CameraPictureGLSurfaceView camera;
    private ImageView btnRecord;
    private ImageView btnClose;
    private ImageView btnFlip;
    private ImageView btnFlash;
    private ImageView btnDone;
    private ProgressBar spinner;

    private boolean isFlashOn = false;
    public PicktureView(ThemedReactContext context) {
        super(context);
        init(context);
    }

    public PicktureView(ThemedReactContext context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PicktureView(ThemedReactContext context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(ThemedReactContext context) {
        Log.e("TAG_AAA", "init");
        this.context = context;
        this.context.addLifecycleEventListener(this);
        inflate(context, R.layout.picture_aa, this);
//        Log.e("TAG_AAA", "init");
        camera = findViewById(R.id.myGLSurfaceView);
        btnRecord = findViewById(R.id.imv_record);
        btnClose = findViewById(R.id.imv_close);
        btnFlip = findViewById(R.id.imv_flip_camera);
        btnFlash = findViewById(R.id.imv_flash);
        btnDone = findViewById(R.id.imv_done);
        spinner = findViewById(R.id.spinner);

        btnRecord.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnFlip.setOnClickListener(this);
        btnFlash.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        camera.setListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.imv_close) {
            onClose();
            return;
        }

        if(v.getId() == R.id.imv_record) {
            takePicture();
            return;
        }

        if(v.getId() == R.id.imv_flip_camera) {
            onFlipCamera();
            return;
        }

        if(v.getId() == R.id.imv_flash) {
            onChangeFlash();
            return;
        }
    }

    public void takePicture() {
        camera.takePicture();
    }

    public void setIsFocused(boolean isFocused) {
        camera.setIsFocused(isFocused);
    }

    public void onChangeFlash() {
        if(camera.checkInitingOrTaking()) {
            return;
        }
        if(isFlashOn) {
            isFlashOn = false;
            camera.setFlashLightMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        else {
            isFlashOn = true;
            camera.setFlashLightMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
    }

    public void onFlipCamera() {
        if(camera.checkInitingOrTaking()) {
            return;
        }
        camera.switchCamera();
    }

    public void onClose() {
        if(camera.checkInitingOrTaking()) {
            return;
        }
        onExit();
    }

    private void onExit() {
        onNativeEventPressExit();
    }

    @Override
    public void onHostResume() {
        camera.onResumeCamera();
    }

    @Override
    public void onHostPause() {
        camera.onDestroyCamera();
    }

    @Override
    public void onHostDestroy() {
        camera.onDestroyCamera();
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(VISIBLE);
            }
        });
    }

    @Override
    public void onCameraReady() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);
            }
        });
    }

    @Override
    public void onTakePictureStart() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(VISIBLE);
            }
        });
    }

    @Override
    public void onTakePictureError() {
        onNativeEventPressDone("", false);
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);
            }
        });
    }

    @Override
    public void onTakePictureDone(String file) {
        Log.e("TAG_AAAAA", file);
        onNativeEventPressDone(file, true);
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);
            }
        });
    }

    public  void onNativeEventPressExit() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onTakeExit,
                event);
    }

    public  void onNativeEventPressDone(String result, boolean success) {
        WritableMap event = Arguments.createMap();
        event.putString("url", result);
        event.putBoolean("error", !success);
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onTakeDone,
                event);
    }
}
