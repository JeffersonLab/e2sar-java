package org.jlab.hpdf.messages;

import java.nio.ByteBuffer;

/**
 * This class is only instantiated in the native JNI wrapper when a ReassembledEvent needs to be passed to Java
 */
public class ReassembledEvent {
    public ByteBuffer byteBuffer;
    public long eventNum;
    public int dataId;

    public ReassembledEvent(ByteBuffer byteBuffer, long eventNum, int dataId){
        this.byteBuffer = byteBuffer;
        this.eventNum = eventNum;
        this.dataId = dataId;
    }
}
