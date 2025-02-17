package org.jlab.hpdf.messages;

import java.time.Instant;

/**
 * This class is only instantiated in the native JNI wrapper when a WorkerStatus needs to be passed to Java
 */
public class WorkerStatus {
    /**
     * Name of worker
     */
    public String name;
    /**
     * Fill percent of worker
     */
    public float fillPercent;
    /**
     * Control Signal of worker
     */
    public float controlSignal;
    /**
     * Slots assigned to worker
     */
    public int slotsAssigned;
    /**
     * last updated Timestamp 
     */
    public Instant lastUpdated;

    /**
     * Constructor with Instant timetsamp
     * @param name - Name of worker
     * @param fillPercent - Fill percent of worker
     * @param controlSignal - Control Signal of worker
     * @param slotsAssigned - Slots assigned to worker
     * @param lastUpdated - last updated Timestamp 
     */
    public WorkerStatus(String name, float fillPercent, float controlSignal, int slotsAssigned, Instant lastUpdated){
        this.name = name;
        this.fillPercent = fillPercent;
        this.controlSignal = controlSignal;
        this.slotsAssigned = slotsAssigned;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Constructor with String timetsamp
     * @param name - Name of worker
     * @param fillPercent - Fill percent of worker
     * @param controlSignal - Control Signal of worker
     * @param slotsAssigned - Slots assigned to worker
     * @param lastUpdated - last updated Timestamp 
     */
    public WorkerStatus(String name, float fillPercent, float controlSignal, int slotsAssigned, String lastUpdated){
        this(name,fillPercent,controlSignal,slotsAssigned,Instant.parse(lastUpdated));
    }
}
