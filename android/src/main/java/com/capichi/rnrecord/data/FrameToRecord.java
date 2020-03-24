package com.capichi.rnrecord.data;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by wanglei02 on 2016/1/21.
 */
public class FrameToRecord {
    private long timestamp;
    private opencv_core.IplImage frame;
    private int indexFrame;
    private int indexSample;

    public FrameToRecord(long timestamp, opencv_core.IplImage frame) {
        this.timestamp = timestamp;
        this.frame = frame;
    }

    public FrameToRecord(int indexFrame, opencv_core.IplImage frame) {
        this.indexFrame = indexFrame;
        this.frame = frame;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public opencv_core.IplImage getFrame() {
        return frame;
    }

    public void setFrame(opencv_core.IplImage frame) {
        this.frame = frame;
    }

    public int getIndexFrame() {
        return indexFrame;
    }

    public int getIndexSample() {
        return indexSample;
    }

    public void setIndexFrame(int indexFrame) {
        this.indexFrame = indexFrame;
    }

    public void setIndexSample(int indexSample) {
        this.indexSample = indexSample;
    }
}