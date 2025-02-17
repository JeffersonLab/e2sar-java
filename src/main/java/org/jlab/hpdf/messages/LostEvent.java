package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a LostEvent in Reassembler needs to be passed to Java
 */
public class LostEvent {
    public long eventNum;
    public int dataId;

    public LostEvent(long eventNum, int dataId){
        this.eventNum = eventNum;
        this.dataId = dataId;
    }
}
