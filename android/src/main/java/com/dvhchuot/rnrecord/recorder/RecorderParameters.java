package com.dvhchuot.rnrecord.recorder;

import android.os.Build;

import org.bytedeco.javacpp.avcodec;

/**
 * Created by wangyang on 15/7/27.
 */


public class RecorderParameters {

    private static boolean AAC_SUPPORTED  = Build.VERSION.SDK_INT >= 10;
    //private int videoCodec = avcodec.AV_CODEC_ID_H264;
    private static int videoCodec = avcodec.AV_CODEC_ID_MPEG4;
    private static int videoFrameRate = 30;
    //private int videoBitrate = 500 *1000;
    private static int videoQuality = 12;
    private static int audioCodec = AAC_SUPPORTED ? avcodec.AV_CODEC_ID_AAC : avcodec.AV_CODEC_ID_AMR_NB;
    private static int audioChannel = 1;
    private static int audioBitrate = 96000;//192000;//AAC_SUPPORTED ? 96000 : 12200;
    private static int videoBitrate = 1000000;
    private static int audioSamplingRate = AAC_SUPPORTED ? 44100 : 8000;
    private static String videoOutputFormat = AAC_SUPPORTED ? "mp4"  : "3gp";



    public static boolean isAAC_SUPPORTED() {
        return AAC_SUPPORTED;
    }
    public static void setAAC_SUPPORTED(boolean aAC_SUPPORTED) {
        AAC_SUPPORTED = aAC_SUPPORTED;
    }
    public String getVideoOutputFormat() {
        return videoOutputFormat;
    }
    public void setVideoOutputFormat(String videoOutputFormat) {
        this.videoOutputFormat = videoOutputFormat;
    }
    public int getAudioSamplingRate() {
        return audioSamplingRate;
    }
    public void setAudioSamplingRate(int audioSamplingRate) {
        this.audioSamplingRate = audioSamplingRate;
    }

    public int getVideoCodec() {
        return videoCodec;
    }
    public void setVideoCodec(int videoCodec) {
        this.videoCodec = videoCodec;
    }
    public int getVideoFrameRate() {
        return videoFrameRate;
    }
    public void setVideoFrameRate(int videoFrameRate) {
        this.videoFrameRate = videoFrameRate;
    }



    public int getVideoQuality() {
        return videoQuality;
    }
    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }
    public int getAudioCodec() {
        return audioCodec;
    }
    public void setAudioCodec(int audioCodec) {
        this.audioCodec = audioCodec;
    }
    public int getAudioChannel() {
        return audioChannel;
    }
    public void setAudioChannel(int audioChannel) {
        this.audioChannel = audioChannel;
    }
    public int getAudioBitrate() {
        return audioBitrate;
    }
    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }
    public int getVideoBitrate() {
        return videoBitrate;
    }
    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
    }


}
