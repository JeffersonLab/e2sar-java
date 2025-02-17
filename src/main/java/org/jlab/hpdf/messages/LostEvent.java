package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a LostEvent in Reassembler needs to be passed to Java
 */
public class LostEvent {
    /**
     * Lost Event Number
     */
    public long eventNum;
    /**
     * Lost event dataID
     */
    public int dataId;

    /**
     * Constructor
     * @param eventNum - Lost Event number
     * @param dataId - Lost Event dataId
     */
    public LostEvent(long eventNum, int dataId){
        this.eventNum = eventNum;
        this.dataId = dataId;
    }
}
