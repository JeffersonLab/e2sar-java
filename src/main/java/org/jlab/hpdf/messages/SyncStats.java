package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a SyncStats for Segmenter needs to be passed to Java
 */
public class SyncStats{
    /**
     * Sync msg Count
     */
    public long syncMsgCount;
    /**
     * Sync err Count
     */
    public long syncErrCount;
    /**
     * Last error no encountered by Segmenter
     */
    public int lastErrorNo;

    /**
     * Default constructor called by JNI
     * @param syncMsgCount - Sync msg Count
     * @param syncErrCount - Sync err Count
     * @param lastErrorNo - Last error no encountered by Segmenter
     */
    public SyncStats(long syncMsgCount, long syncErrCount, int lastErrorNo){
        this.syncMsgCount = syncMsgCount;
        this.syncErrCount = syncErrCount;
        this.lastErrorNo = lastErrorNo;
    }
}