package com.dvhchuot.rnrecord.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;

import com.dvhchuot.rnrecord.data.FrameToRecord;
import com.dvhchuot.rnrecord.data.RecordFragment;
import com.dvhchuot.rnrecord.recorder.AudioRecordThread;
import com.dvhchuot.rnrecord.recorder.FFmpegFrameRecorder;
import com.dvhchuot.rnrecord.recorder.RunningThread;
import com.dvhchuot.rnrecord.utils.CameraHelper;
import com.dvhchuot.rnrecord.utils.VideoUtils;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRecordGLSurfaceView extends CameraGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, AudioRecordThread.AudioListener {


    private FFmpegFrameRecorder recorder;
    private File mVideo;
    private int minTime = VideoUtils.MIN_RECORD_TIME;
    private int maxTime = VideoUtils.LIMIT_RECORD_TIME;
    private int videowidth = 540;
    private int videoHeight = 960;
    private int videoBitrate = VideoUtils.MAXBITRATE;
    private int videoFramerate = VideoUtils.FRAME_RATE;
    private String videoFormat = VideoUtils.VIDEO_FORMAT;
    private int frameDepth = Frame.DEPTH_BYTE;
    private int sampleAudioRateInHz = VideoUtils.AUDIO_RATE_HZ;

    private boolean mRecording = false;
    private boolean mIniting = true;



    private boolean mDone = false;
    private boolean isFocused = true;


    private LinkedBlockingQueue<FrameToRecord> mFrameToRecordQueue;
    private LinkedBlockingQueue<FrameToRecord> mRecycledFrameQueue;
    private int mFrameToRecordCount;
    private int mFrameRecordedCount;
    private int mFrameCount = 0;
    private Stack<RecordFragment> mRecordFragments;
    private List<Integer> listSections;

    private int[] mRecordStateLock = new int[0];

    private AudioRecordThread mAudioRecordThread;
    private RecordingThread mRecordingThread;

    private Cameralistener cameralistener;

    public CameraRecordGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mFrameToRecordQueue = new LinkedBlockingQueue<>(10);
        // At most recycle 2 Frame
        mRecycledFrameQueue = new LinkedBlockingQueue<>(4);
        mRecordFragments = new Stack<>();
        listSections = new ArrayList<>();
        try {
            FFmpegFrameRecorder.tryLoad();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initRecorder();
        startRecorder();
        startAudio();
        startRecording();
        mIniting = false;
        if(cameralistener != null) {
            cameralistener.onCameraOnReady();
        }
    }

    public void stop() {
        Log.e(LOG_TAG, "onStop");
        stopRecorder();
        stopRecording();
        releaseRecorder(false);
    }

    public void initRecorder() {
        recorder = new FFmpegFrameRecorder(onGetVideoPath(), videowidth, videoHeight, 1);
        recorder.setFormat(videoFormat);
        recorder.setSampleRate(sampleAudioRateInHz);
        recorder.setFrameRate(videoFramerate);

        recorder.setVideoBitrate(videoBitrate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoOption("crf", "22");
        recorder.setVideoOption("preset", "superfast");
        recorder.setVideoOption("tune", "zerolatency");

        Log.e(LOG_TAG, "mFrameRecorder initialize success");
    }

    private void releaseRecorder(boolean deleteFile) {

        if (recorder != null) {
            try {
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            recorder = null;

            if (deleteFile && mVideo != null) {
                mVideo.delete();
            }
        }
    }



    private void startRecorder() {
        if(recorder != null)
            try {
                recorder.start();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
    }

    private void stopRecorder() {

            if (recorder != null) {
                try {
                    synchronized (recorder) {
                        recorder.stop();
                    }
                } catch (FFmpegFrameRecorder.Exception e) {
                    e.printStackTrace();
                }

        }
//        mRecordFragments.clear();
    }

    public void startAudio() {
        mAudioRecordThread = new AudioRecordThread();
        mAudioRecordThread.start();
        mAudioRecordThread.setListener(this);
    }

    private void startRecording() {
        mRecordingThread = new RecordingThread();
        mRecordingThread.start();
    }

    private void stopRecording() {
        if (mAudioRecordThread != null) {
            mAudioRecordThread.setListener(null);
            if (mAudioRecordThread.isRunning()) {
                mAudioRecordThread.stopRunning();
            }
        }

        if (mRecordingThread != null) {
            if (mRecordingThread.isRunning()) {
                mRecordingThread.stopRunning();
            }
        }



        try {
            if (mAudioRecordThread != null) {
                mAudioRecordThread.join();
            }
            if (mRecordingThread != null) {
                mRecordingThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAudioRecordThread = null;
        mRecordingThread = null;

        releaseQueue();

    }

    public void releaseQueue() {
        Log.e("mFrameToRecordQueue", String.valueOf(mRecycledFrameQueue.size()));
        while (!mFrameToRecordQueue.isEmpty()) {
            FrameToRecord frameToRecord =  mFrameToRecordQueue.poll();
            frameToRecord.getFrame().release();
            frameToRecord = null;
            Log.e("mFrameToRecordQueue", String.valueOf(mFrameToRecordQueue.size()));
        }

        while(!mRecycledFrameQueue.isEmpty()) {
            FrameToRecord frameToRecord =  mRecycledFrameQueue.poll();
            frameToRecord.getFrame().release();
            frameToRecord = null;
        }

        mFrameToRecordQueue.clear();
        mRecycledFrameQueue.clear();
    }

    private long calculateTotalRecordedTime(Stack<RecordFragment> recordFragments) {
        long recordedTime = 0;
        try {
            for (RecordFragment recordFragment : recordFragments) {
                recordedTime += recordFragment.getDuration();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return recordedTime;
    }

    public void onProgress() {
        long curRecordedTime = 0;
        if(mRecordFragments.size() > 0)
        {
            RecordFragment curFragment = mRecordFragments.pop();
            long recordedTime = calculateTotalRecordedTime(mRecordFragments);
            // push it back after calculation
            mRecordFragments.push(curFragment);
            curRecordedTime = System.currentTimeMillis()
                    - curFragment.getStartTimestamp() + recordedTime;

        }
        if(cameralistener!= null) {
            cameralistener.onProgress(curRecordedTime);
        }
        if(curRecordedTime >= maxTime * 1000L) {
            onDone();
        }
    }

    public void onDone() {
        mDone = true;
        if(mRecording) {
            setmRecording(false);
        }
        if(cameralistener!=null) {
            cameralistener.onDoneStart();
        }

        new FinishRecordingTask().execute();

    }

    class FinishRecordingTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            while (!mFrameToRecordQueue.isEmpty()) {
                // do nothing
                Log.e(LOG_TAG, String.valueOf(mFrameToRecordQueue.size()));
            }


            stopRecording();
            stopRecorder();
            releaseRecorder(false);

            onDoneSuccess();

            return null;
        }
    }


    public void onDoneSuccess() {

        Log.e(LOG_TAG, "onDoneSuccess");

        FFmpegFrameRecorder resultRecorder;
        FFmpegFrameGrabber grabber;
        Frame frame;

        String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File newFile = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_VIDEO);
        Log.e(LOG_TAG, "Output Video: " + newFile);
        resultRecorder = new FFmpegFrameRecorder(newFile, videowidth, videoHeight, 1);
        resultRecorder.setFormat(videoFormat);
        resultRecorder.setSampleRate(sampleAudioRateInHz);
        resultRecorder.setFrameRate(videoFramerate);

        resultRecorder.setVideoBitrate(videoBitrate);
        resultRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        resultRecorder.setVideoOption("crf", "22");
        resultRecorder.setVideoOption("preset", "superfast");
        resultRecorder.setVideoOption("tune", "zerolatency");

        try {
            resultRecorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        for (RecordFragment recordFragment: mRecordFragments) {
            grabber = new FFmpegFrameGrabber(recordFragment.getFile());
            try {
                grabber.start();
                while ((frame = grabber.grabFrame(true, true, true, false)) != null) {
                    try {
                        resultRecorder.record(frame);
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
                grabber.stop();
                grabber.release();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }

        }

        try {
            resultRecorder.stop();
            resultRecorder.release();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

//        stopRecording();

        long duration = getDuration();
        if(cameralistener!=null) {
            cameraLog(newFile.getAbsolutePath() + "  " + String.valueOf(duration));
            cameralistener.onDoneRecorder(newFile.getAbsolutePath(), duration);
        }
    }

    public void setListener(Cameralistener listener) {
        this.cameralistener = listener;
    }

    public boolean ismRecording() {
        return mRecording;
    }

    public void setmRecording(Boolean mRecording) {

        if(!mRecording) {


            if(mRecordFragments.size() > 0) {
                mRecordFragments.peek().setEndTimestamp(System.currentTimeMillis());
                mRecordFragments.peek().setFile(mVideo);
                mRecordFragments.peek().setUrlVideo(mVideo.getAbsolutePath());
                int test = (int) mRecordFragments.peek().getDuration();
                listSections.add(test);

            }
            this.mRecording = false;
            new PauseTask().execute();

        }
        else {
            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setStartTimestamp(System.currentTimeMillis());
            recordFragment.setFrameStart(mFrameCount);
            mRecordFragments.push(recordFragment);
                if(recorder == null) {
                    mVideo = null;

                    initRecorder();
                    startRecorder();
                    if(mRecordingThread == null) {
                        mRecordingThread = new RecordingThread();
                        mRecordingThread.start();
                    }
                }
            this.mRecording = mRecording;
        }

    }

    class PauseTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            while (!mFrameToRecordQueue.isEmpty()) {
                // do nothing
                Log.e(LOG_TAG, String.valueOf(mFrameToRecordQueue.size()));
            }
            if (mRecordingThread != null) {
                if (mRecordingThread.isRunning()) {
                    mRecordingThread.stopRunning();
                }
                try {
                    mRecordingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRecordingThread = null;
            }
            cameraLog(" PauseTask ");
            stopRecorder();
            releaseRecorder(false);
            return null;
        }
    }

    private File onGetVideoPath(){
        if (mVideo== null ||!mVideo.exists()){
            String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            mVideo = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_VIDEO);
            Log.e(LOG_TAG, "Output Video: " + mVideo);
        }
        return mVideo;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    public void pushCachedFrame(FrameToRecord frameToRecord) {
        if (mFrameToRecordQueue.offer(frameToRecord)) {
            mFrameToRecordCount++;
            mFrameCount ++;
        }
    }

    private opencv_core.IplImage image1;

    @Override
    public void calcViewport() {
        super.calcViewport();
        image1 = opencv_core.IplImage.create(drawViewport.cutX, drawViewport.cutY, opencv_core.IPL_DEPTH_8U, 4);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        synchronized (mRecordStateLock) {
//            Log.e("mRecording", String.valueOf(mRecording));
            if (mRecording && recorder != null && !mDone && mFrameCount < videoFramerate * maxTime) {
//                onProgress();
                FrameToRecord frameToRecord = mRecycledFrameQueue.poll();

                if (frameToRecord != null) {
                    frameToRecord.setIndexFrame(mFrameCount);
                } else {
                    Log.e("frameToRecord", "null");
                    opencv_core.IplImage image = opencv_core.IplImage.create(videowidth , videoHeight , opencv_core.IPL_DEPTH_8U, 4);
                    frameToRecord = new FrameToRecord(mFrameCount, image);

                }
                if (frameToRecord.getFrame() != null) {
                    GLES20.glReadPixels(drawViewport.startX, drawViewport.startY, drawViewport.cutX, drawViewport.cutY , GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, image1.getByteBuffer());
                    // scale and flip image
                    opencv_imgproc.cvResize(image1, frameToRecord.getFrame());
                    opencv_core.cvFlip(frameToRecord.getFrame());
//                    Log.e(LOG_TAG, "nhan frame...");
                    if(recorder != null && !mDone && mRecording) {
                        pushCachedFrame(frameToRecord);
                    }


                } else {
                    Log.d(LOG_TAG, "Frame loss...");
                }
            }
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    @Override
    public ShortBuffer onAudioBuffer(ShortBuffer shortBuffer) {
        if(mRecording && recorder!= null && !mDone) {
            onProgress();
            try {
                if(recorder != null && mRecording)
                    recorder.recordSamples(shortBuffer);

            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
        return shortBuffer;
    }


    class RecordingThread extends RunningThread{
        @Override
        public void run() {
            isRunning = true;
            FrameToRecord recordedFrame;
            while (isRunning || !mFrameToRecordQueue.isEmpty()) {

                try {
                    recordedFrame = mFrameToRecordQueue.take();
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                    break;
                }
                if(recorder != null) {
                    try {
                        Log.e(LOG_TAG, "hayasdas: " + String.valueOf(recordedFrame.getIndexFrame()));
                        recorder.record(recordedFrame.getFrame());
                    }
                    catch (Exception e) {
                        Log.e(LOG_TAG, "hay: " + e.getMessage());
                    }
                }

                if(mRecycledFrameQueue.size() >= 3) {
                    recordedFrame.getFrame().release();
                    recordedFrame.setFrame(null);
                    recordedFrame = null;
                }
                else {
                    mRecycledFrameQueue.offer(recordedFrame);
                }
            }
        }

        public void stopRunning() {
            super.stopRunning();
            if (getState() == Thread.State.WAITING) {
                interrupt();
            }
        }
    }

    public void onPauseCamera() {
        if(mRecording) {
            setmRecording(false);
        }
        cameraInstance().stopCamera();
        stopRecording();
    }

    public void onResumeCamera() {
        if(!mIniting && this.isFocused) {
            cameraLog("resume");
            reCreateCamera();
            startAudio();
            startRecording();
        }
    }

    public void onDestroyCamera() {
        if(mRecording) {
            setmRecording(false);
        }
        cameraInstance().stopCamera();
        stopRecording();
        stopRecorder();
        releaseRecorder(!mDone);
    }


    public void reshoot() {
        releaseQueue();
        for (RecordFragment recordFragment: mRecordFragments) {
            if(recordFragment.getFile() != null) {
                recordFragment.getFile().delete();
            }
        }
        mVideo = null;
        mRecordFragments.clear();
        mFrameCount = 0;
        mDone = false;
        listSections.clear();
        try {
            if(mVideo != null && mVideo.createNewFile() ) {

            }
            else {
                restart();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            restart();
        }

        if(cameralistener != null) {
            cameralistener.onReshootSuccess();
        }
    }

    public void restart() {
        stopRecorder();
        releaseRecorder(true);

        initRecorder();
        startRecorder();
        if(mRecordingThread == null || !mRecordingThread.isRunning()) {
            startRecording();
        }
    }

    public void onEraseSegment() {
        RecordFragment recordFragment = mRecordFragments.pop();
        recordFragment.getFile().delete();
        mFrameCount = recordFragment.getFrameStart();
        recordFragment = null;
        listSections.remove(listSections.size() - 1);
        long curRecordedTime = 0;
        if(mRecordFragments.size() > 0)
        {
            RecordFragment curFragment = mRecordFragments.pop();
            long recordedTime = calculateTotalRecordedTime(mRecordFragments);
            // push it back after calculation
            mRecordFragments.push(curFragment);
            curRecordedTime = System.currentTimeMillis()
                    - curFragment.getStartTimestamp() + recordedTime;
        }
        if(cameralistener!= null) {
            cameralistener.onProgress(curRecordedTime);
        }
    }


    public interface Cameralistener {
        public void onCameraOnReady();
        public void onDoneStart();
        public void onDoneRecorder(String pathFile, long duration);
        public void onProgress(long time);
        public void onReshootSuccess();
    }

    public List<Integer> getListSection () {
        List<Integer> progressList = new ArrayList<>();
        progressList.addAll(listSections);
        if(mRecording && mRecordFragments.size() > 0)
        {
            try {
                int currentTime =(int) ((long)System.currentTimeMillis() - mRecordFragments.peek().getStartTimestamp());
                progressList.add(currentTime);
            }
            catch (Exception e) {

            }
        }
        return progressList;
    }

    public boolean getIniting () {
        return this.mIniting;
    }

    public boolean getFocused() {
        return this.isFocused;
    }
    public void setIsFocused(boolean isFocused) {
        if(isFocused) {
            if(!this.isFocused) {
                reshoot();
                reCreateCamera();
//                start();
                startAudio();
            }
        }
        else {
            if(this.isFocused) {
                onDestroyCamera();
            }
        }
        this.isFocused = isFocused;
    }

    public boolean ismDone() {
        return mDone;
    }

    public long getDuration() {
        return calculateTotalRecordedTime(mRecordFragments);
    }

    public void cameraLog(String log) {
        Log.e(LOG_TAG, log);
    }
}
