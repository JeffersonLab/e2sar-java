package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a SendStats for Segmenter needs to be passed to Java
 */
public class SendStats{
    /**
     * Successful event datagrams sent count
     */
    public long eventDatagramCount;
    /**
     * Event Datagram error count
     */
    public long eventDatagramErrCount;
    /**
     * Last error no encountered by Segmenter
     */
    public int lastErrorNo;

    /**
     * Default constructor called by JNI
     * @param eventDatagramCount - Successful event datagrams sent count
     * @param eventDatagramErrCount - Event Datagram error count
     * @param lastErrorNo - Last error no encountered by Segmenter
     */
    public SendStats(long eventDatagramCount, long eventDatagramErrCount, int lastErrorNo){
        this.eventDatagramCount = eventDatagramCount;
        this.eventDatagramErrCount = eventDatagramErrCount;
        this.lastErrorNo = lastErrorNo;
    }
}