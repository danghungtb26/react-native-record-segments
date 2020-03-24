package com.capichi.rnrecord.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.capichi.rnrecord.utils.VideoUtils;

import java.nio.ShortBuffer;

public class AudioRecordThread extends RunningThread {
    private AudioRecord mAudioRecord;
    private ShortBuffer audioData;
    private String LOG_TAG = "Audio_thread";
    private int sampleAudioRateInHz = VideoUtils.AUDIO_RATE_HZ;

    private AudioListener listener;

    public AudioRecordThread() {
        int bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioData = ShortBuffer.allocate(bufferSize);
    }

    public void setListener(AudioListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        Log.e(LOG_TAG, "mAudioRecord startRecording");
        mAudioRecord.startRecording();

        isRunning = true;
        /* ffmpeg_audio encoding loop */
        while (isRunning) {
            int bufferReadResult = mAudioRecord.read(audioData.array(), 0, audioData.capacity());
            audioData.limit(bufferReadResult);
            if(listener != null) {
                listener.onAudioBuffer(audioData);
            }
        }
        Log.e(LOG_TAG, "mAudioRecord stopRecording");
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        Log.e(LOG_TAG, "mAudioRecord released");
    }

    public interface AudioListener {
        public ShortBuffer onAudioBuffer(ShortBuffer shortBuffer);
    }
}