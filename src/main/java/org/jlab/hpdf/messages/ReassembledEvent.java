package org.jlab.hpdf.messages;

import java.nio.ByteBuffer;

/**
 * This class is only instantiated in the native JNI wrapper when a ReassembledEvent needs to be passed to Java
 */
public class ReassembledEvent {
    /**
     * Dynamically allocated datat from JNI
     */
    public ByteBuffer byteBuffer;
    /**
     * Event number associated with the Event
     */
    public long eventNum;
    /**
     * Data ID associated with the Event
     */
    public int dataId;

    /**
     * Default Constructor
     * @param byteBuffer - Dynamically allocated datat from JNI
     * @param eventNum - Event number associated with the Event
     * @param dataId - Data ID associated with the Event
     */
    public ReassembledEvent(ByteBuffer byteBuffer, long eventNum, int dataId){
        this.byteBuffer = byteBuffer;
        this.eventNum = eventNum;
        this.dataId = dataId;
    }
}
