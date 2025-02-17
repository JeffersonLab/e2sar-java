package org.jlab.hpdf.messages;

import java.time.Instant;
import java.util.List;

/**
 * This class is only instantiated in the native JNI wrapper when a LBStatus needs to be passed to Java
 */
public class LBStatus{
    /**
     * Timestamp of received LBStatus 
     */
    public Instant timestamp;
    /**
     * current Epoch
     */
    public long currentEpoch;
    /**
     * current Predicted Event Number
     */
    public long currentPredictedEventNumber;
    /**
     * List of workers associated with the LB
     */
    public List<WorkerStatus> workers;
    /**
     * List of senders associated with the LB
     */
    public List<String> senderAddresses;
    /**
     * Expiration of LB
     */
    public Instant expiresAt;

    /**
     * Constructor using Instant timestamps
     * @param timestamp - Timestamp of received LBStatus 
     * @param expiresAt - Expiration of LB
     * @param currentEpoch - current Epoch
     * @param currentPredictedEventNumber - current Predicted Event Number
     * @param workers - List of workers associated with the LB
     * @param senderAddresses - List of senders associated with the LB
     */
    public LBStatus(Instant timestamp, Instant expiresAt, long currentEpoch, long currentPredictedEventNumber, List<WorkerStatus> workers, 
    List<String> senderAddresses){
        this.timestamp = timestamp;
        this.expiresAt = expiresAt;
        this.currentEpoch = currentEpoch;
        this.currentPredictedEventNumber = currentPredictedEventNumber;
        this.workers = workers;
        this.senderAddresses = senderAddresses;
    }

    /**
     * Constructor using String timestamps
     * @param timestamp - Timestamp of received LBStatus 
     * @param expiresAt - Expiration of LB
     * @param currentEpoch - current Epoch
     * @param currentPredictedEventNumber - current Predicted Event Number
     * @param workers - List of workers associated with the LB
     * @param senderAddresses - List of senders associated with the LB
     */
    public LBStatus(String timestamp, String expiresAt, long currentEpoch, long currentPredictedEventNumber, List<WorkerStatus> workers, 
    List<String> senderAddresses){
        this(Instant.parse(timestamp), Instant.parse(expiresAt), currentEpoch, currentPredictedEventNumber, workers, senderAddresses);
    }
}