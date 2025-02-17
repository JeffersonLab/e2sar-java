package org.jlab.hpdf.messages;

/**
 * This class is only instantiated in the native JNI wrapper when a RecvStats for Reassembler needs to be passed to Java
 */
public class RecvStats {
    /**
     * number of events received and lost on enqueue
     */
    public long enqueueLoss;
    /**
     * number of events successfully processed
     */
    public long eventSuccess;
    /**
     * last error no encountered by native reassembler
     */
    public int lastErrorNo;
    /**
     * GRPC error count
     */
    public int grpcErrCount;
    /**
     * Data error count
     */
    public int dataErrCount;
    /**
     * last native E2sar error code
     */
    public int lastE2sarError;

    /**
     * Default Constructor
     * @param enqueueLoss - number of events received and lost on enqueue
     * @param eventSuccess - number of events successfully processed
     * @param lastErrorNo - last error no encountered by native reassembler
     * @param grpcErrCount - GRPC error count
     * @param dataErrCount - Data error count
     * @param lastE2sarError - last native E2sar error code
     */
    public RecvStats(long enqueueLoss, long eventSuccess, int lastErrorNo, int grpcErrCount, int dataErrCount, int lastE2sarError){
        this.enqueueLoss = enqueueLoss;
        this.eventSuccess = eventSuccess;
        this.lastErrorNo = lastErrorNo;
        this.grpcErrCount = grpcErrCount;
        this.dataErrCount = dataErrCount;
        this.lastE2sarError = lastE2sarError;
    }
}
