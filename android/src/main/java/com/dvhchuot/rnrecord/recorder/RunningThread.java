package com.dvhchuot.rnrecord.recorder;

public class RunningThread extends Thread {
    public boolean isRunning;

    public boolean isRunning() {
        return isRunning;
    }

    public void stopRunning() {
        this.isRunning = false;
    }
}
