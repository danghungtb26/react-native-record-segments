package com.dvhchuot.rnrecord.data;

import java.io.File;

public class RecordFragment {
    private long startTimestamp = -1;
    private long endTimestamp = -1;

    private int frameStart = -1;
    private int frameEnd;
    private int sampleStart = -1;
    private int sampleEnd;

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private File file;

    private String urlVideo;

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getDuration() {
        if(endTimestamp == -1 || startTimestamp == -1) return 0;
        return endTimestamp - startTimestamp;
    }

    public int getFrameEnd() {
        return frameEnd;
    }

    public int getFrameStart() {
        return frameStart;
    }

    public int getSampleEnd() {
        return sampleEnd;
    }

    public int getSampleStart() {
        return sampleStart;
    }

    public void setFrameEnd(int frameEnd) {
        if(frameEnd >= frameStart)
        this.frameEnd = frameEnd;
    }

    public void setFrameStart(int frameStart) {
        if(this.frameStart == -1)
        this.frameStart = frameStart;
    }

    public void setSampleEnd(int sampleEnd) {
        if(sampleEnd >= sampleStart)
        this.sampleEnd = sampleEnd;
    }

    public void setSampleStart(int sampleStart) {
        if(this.sampleStart == -1)
        this.sampleStart = sampleStart;
    }
}