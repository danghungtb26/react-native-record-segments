package com.dvhchuot.rnrecord.ui;

import android.hardware.Camera;
import android.util.AttributeSet;
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

import java.util.List;

public class RecordView extends ConstraintLayout implements LifecycleEventListener, View.OnClickListener, CameraRecordGLSurfaceView.Cameralistener , ProgressSectionsView.ProgressSectionsProvider {
    private ThemedReactContext context;
    //view
    CameraRecordGLSurfaceView camera;
    private ImageView btnRecord;
    private ImageView btnClose;
    private ImageView btnFlip;
    private ImageView btnFlash;
    private ImageView btnDone;
    private ProgressSectionsView progressSectionsView;
    private ProgressBar spinner;

    //dialog

    private boolean isFlashOn = false;

    public RecordView(ThemedReactContext context) {
        super(context);
        init(context);
    }

    public RecordView(ThemedReactContext context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordView(ThemedReactContext context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(ThemedReactContext context) {
        this.context = context;
        this.context.addLifecycleEventListener(this);
        inflate(context, R.layout.camera_aa, this);

        camera = findViewById(R.id.myGLSurfaceView);
        btnRecord = findViewById(R.id.imv_record);
        btnClose = findViewById(R.id.imv_close);
        btnFlip = findViewById(R.id.imv_flip_camera);
        btnFlash = findViewById(R.id.imv_flash);
        btnDone = findViewById(R.id.imv_done);
        progressSectionsView = findViewById(R.id.progres_sections);
        progressSectionsView.setProvider(this);
        spinner = findViewById(R.id.spinner);

        btnRecord.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnFlip.setOnClickListener(this);
        btnFlash.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        camera.setListener(this);

        initDialog();
    }

    public void initDialog() {

    }

    public void setFilter(String filter) {
        camera.setFrameRenderer(filter);
    }

    public void setIsFocused(Boolean isFocused) {
        camera.cameraLog( "isFocused: " +String.valueOf(isFocused));
        isFlashOn = false;
        camera.setIsFocused(isFocused);
    }

    public void onClose() {
        if(checkInitOrDone()) {
            return;
        }
        onExit();
    }

    private void onExit() {
        onNativeEventPressExit();
    }

    private void onReshoot() {
        camera.reshoot();

    }

    public void onReshootSuccess() {
        updateView();
    }

    public void captureVideo() {
        if(checkInitOrDone()) {
            return;
        }
        if(camera.ismRecording()) {
            camera.setmRecording(false);
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            });
        }
        else {
            camera.setmRecording(true);
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            });
        }
    }

    public void onFlipCamera() {
        if(checkInitOrDone()) {
            return;
        }
        camera.switchCamera();
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
    }

    public void onDone() {
        if(checkInitOrDone()) {
            return;
        }
        camera.onDone();
    }

    public void onFilter() {
        if(checkInitOrDone()) {
            return;
        }
        onNativeEventPressFilter();
    }

    public void updateView() {


        if(camera.ismRecording() || camera.getDuration() > 0 ) {

        }
        else {

        }

        if(camera.ismRecording()) {
            btnClose.setVisibility(INVISIBLE);
            btnFlash.setVisibility(INVISIBLE);
            btnFlip.setVisibility(INVISIBLE);
            btnRecord.setImageResource(R.drawable.ic_recording);
        } else {
            btnClose.setVisibility(VISIBLE);
            btnFlash.setVisibility(VISIBLE);
            btnFlip.setVisibility(VISIBLE);
            btnRecord.setImageResource(R.drawable.ic_record);
        }

        if(!camera.ismRecording() && camera.getDuration() > 0) {
        }

        else {
        }

        if(camera.getDuration() > 5000L) {
            btnDone.setVisibility(VISIBLE);
        }
        else {
            btnDone.setVisibility(INVISIBLE);
        }
    }

    private boolean checkInitOrDone() {
        return camera.ismDone() || camera.getIniting();
    }

    public void onEraseSegment() {
        if(checkInitOrDone()) {
            return;
        }
        camera.onEraseSegment();
        updateView();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.imv_close) {
            onClose();
            return;
        }

        if(v.getId() == R.id.imv_record) {
            captureVideo();
            return;
        }

        if(v.getId() == R.id.imv_flip_camera) {
            onFlipCamera();
            return;
        }

        if(v.getId() == R.id.imv_done) {
            onDone();
            return;
        }

        if(v.getId() == R.id.imv_gallery_select) {
            onGalleryPress();
            return;
        }

        if(v.getId() == R.id.imv_flash) {
            onChangeFlash();
            return;
        }
    }

    @Override
    public void onHostResume() {
        camera.cameraLog("onHostPause");
        camera.onResumeCamera();
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    @Override
    public void onHostPause() {
        this.isFlashOn = false;
        camera.cameraLog("onHostPause");
        camera.onPauseCamera();
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
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
    public void onCameraOnReady() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);
            }
        });
    }

    @Override
    public void onDoneStart() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(VISIBLE);
                updateView();
            }
        });
    }

    @Override
    public void onDoneRecorder(String pathFile, long duration, String type) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);

            }
        });
        onNativeEventPressDone(pathFile, duration);
    }

    @Override
    public void onDoneRecorder(String pathFile, long duration, String type, boolean error) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(INVISIBLE);

            }
        });
        onNativeEventPressDone(pathFile, duration);
    }

    @Override
    public void onProgress(long time) {
        if(time > 5000L) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnDone.setVisibility(VISIBLE);
                }
            });

        }
    }

    @Override
    public boolean hasCurrentProgress() {
        return true;
    }

    @Override
    public List<Integer> getProgressSections() {
        return camera.getListSection();
    }

    public void onGalleryPress() {
        onNativeEventPressGallery();
    }


    public  void onNativeEventPressFilter() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onFilterTap,
                event);
    }

    public  void onNativeEventPressGallery() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onPressPhoto,
                event);
    }

    public  void onNativeEventPressExit() {
        WritableMap event = Arguments.createMap();
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onExit,
                event);
    }

    public  void onNativeEventPressDone(String result, long duration) {
        WritableMap event = Arguments.createMap();
        event.putString("url", result);
        event.putInt("duration", (int) duration);
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ReactProps.onDone,
                event);
    }
}
