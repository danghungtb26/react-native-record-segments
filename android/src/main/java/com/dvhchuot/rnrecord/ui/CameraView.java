package com.dvhchuot.rnrecord.ui;

import android.hardware.Camera;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dvhchuot.rnrecord.R;
import com.dvhchuot.rnrecord.data.ReactProps;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class CameraView extends ConstraintLayout implements LifecycleEventListener, CameraRecordGLSurfaceView.Cameralistener{
    private ThemedReactContext context;
    //view
    CameraRecordGLSurfaceView camera;

    private boolean isFlashOn = false;

    public CameraView(ThemedReactContext context) {
        super(context);
        init(context);
    }

    public CameraView(ThemedReactContext context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraView(ThemedReactContext context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(ThemedReactContext context) {
        this.context = context;
        this.context.addLifecycleEventListener(this);
        inflate(context, R.layout.camera_a2, this);

        camera = findViewById(R.id.myGLSurfaceView);

        camera.setListener(this);
    }

    public void setFilter(String filter) {
        camera.setFrameRenderer(filter);
    }

    public void setIsFocused(Boolean isFocused) {
        camera.cameraLog( "isFocused: " +String.valueOf(isFocused));
        isFlashOn = false;
        camera.setIsFocused(isFocused);
    }

    public void reshoot() {
        camera.reshoot();

    }

    public void record() {
        if(checkInitOrDone()) {
            return;
        }
        camera.setmRecording(true);

        callbackAfterUpdate();
    }

    public void pause() {
        camera.setmRecording(false);
        callbackAfterUpdate();
    }

    public void resume() {
        camera.setmRecording(true);
        callbackAfterUpdate();
    }

    public void capture() {
        camera.capture();
    }

    public void done() {
        if(camera.ismDone()) return;
        camera.onDone();
    }

    public void onFlipCamera() {
        if(checkInitOrDone()) {
            return;
        }
        camera.switchCamera();

        callbackAfterUpdate();
    }

    public void onChangeFlash() {
        if(checkInitOrDone()) {
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
        callbackAfterUpdate();
    }

    public boolean checkInitOrDone() {
        return camera.ismDone() || camera.getIniting();
    }

    public boolean isReccoring() {
        return camera.ismRecording();
    }

    @Override
    public void onReshootSuccess() {
        callbackAfterUpdate();
    }

    public void callbackAfterUpdate ()
    {
        onNativeEventAfterUpdate();
    }
    @Override
    public void onHostResume() {
        camera.cameraLog("onHostPause");
        camera.onResumeCamera();
    }

    @Override
    public void onHostPause() {
        this.isFlashOn = false;
        camera.cameraLog("onHostPause");
        camera.onPauseCamera();
    }

    @Override
    public void onHostDestroy() {
        camera.onDestroyCamera();
    }

    @Override
    public void onCameraOnReady() {
        onNativeEventCameraReady();
    }

    @Override
    public void onDoneStart() {
        callbackAfterUpdate();
        onNativeEventDoneStart();
    }

    @Override
    public void onDoneRecorder(String pathFile, long duration, String type) {
        onNativeEventPressDone(pathFile, duration, type, false);
    }

    @Override
    public void onDoneRecorder(String pathFile, long duration, String type, boolean error) {
        onNativeEventPressDone(pathFile, duration, type, error);
    }

    @Override
    public void onProgress(long time) {
        onNativeEventProgress(time);
    }


    public  void onNativeEventPressDone(String result, long duration, String type, boolean error) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("error", error);
        event.putString("url", result);
        event.putString("type", type);
        event.putInt("duration", (int) duration);
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDoneSuccess,
                event);
    }

    public void onNativeEventDoneStart() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDoneStart,
                event);
    }

    public void onNativeEventProgress(long progress) {
        WritableMap event = Arguments.createMap();
        event.putDouble("progress",(double) progress);
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDVProgress,
                event);
    }

    public void onNativeEventCameraReady() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDVCameraReady,
                event);
    }

    public void onNativeEventAfterUpdate() {
        camera.cameraLog("onNativeEventAfterUpdate");
        WritableMap event = Arguments.createMap();
        event.putBoolean("isIniting", camera.getIniting());
        event.putBoolean("isDone", camera.ismDone());
        event.putBoolean("isRecording", camera.ismRecording());
        event.putBoolean("isFlashOn", isFlashOn);
        event.putBoolean("isDeviceBack", camera.isCameraBackForward());
        event.putBoolean("isRecorded", camera.ismRecording() || (camera.getDuration() > 0));

        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDVAfterUpdate,
                event);
    }
}
