package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a SendStats for Segmenter needs to be passed to Java
 */
public class SendStats{
    public long eventDatagramCount;
    public long eventDatagramErrCount;
    public int lastErrorNo;

    public SendStats(long eventDatagramCount, long eventDatagramErrCount, int lastErrorNo){
        this.eventDatagramCount = eventDatagramCount;
        this.eventDatagramErrCount = eventDatagramErrCount;
        this.lastErrorNo = lastErrorNo;
    }
}