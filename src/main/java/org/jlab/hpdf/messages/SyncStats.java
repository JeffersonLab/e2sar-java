package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a SyncStats for Segmenter needs to be passed to Java
 */
public class SyncStats{
    public long syncMsgCount;
    public long syncErrCount;
    public int lastErrorNo;

    public SyncStats(long syncMsgCount, long syncErrCount, int lastErrorNo){
        this.syncMsgCount = syncMsgCount;
        this.syncErrCount = syncErrCount;
        this.lastErrorNo = lastErrorNo;
    }
}