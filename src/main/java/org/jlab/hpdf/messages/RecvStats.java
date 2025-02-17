package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a RecvStats for Reassembler needs to be passed to Java
 */
public class RecvStats {
    public long enqueueLoss;
    public long eventSuccess;
    public int lastErrorNo;
    public int grpcErrCount;
    public int dataErrCount;
    public int lastE2sarError;

    public RecvStats(long enqueueLoss, long eventSuccess, int lastErrorNo, int grpcErrCount, int dataErrCount, int lastE2sarError){
        this.enqueueLoss = enqueueLoss;
        this.eventSuccess = eventSuccess;
        this.lastErrorNo = lastErrorNo;
        this.grpcErrCount = grpcErrCount;
        this.dataErrCount = dataErrCount;
        this.lastE2sarError = lastE2sarError;
    }
}
