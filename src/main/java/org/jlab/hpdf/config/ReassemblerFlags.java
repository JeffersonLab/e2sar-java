package org.jlab.hpdf.config;

/**
 * flags governing Reassembler behavior with sane defaults
 * - useCP - whether to use the control plane (sendState, registerWorker) {true}
 * - useHostAddress - use IPv4 or IPv6 address for gRPC even if hostname is specified (disables cert validation) {false}
 * - period_ms - period of the send state thread in milliseconds {100}
 * - epoch_ms - period of one epoch in milliseconds {1000}
 * - Ki, Kp, Kd - PID gains (integral, proportional and derivative) {0., 0., 0.}
 * - setPoint - setPoint queue occupied percentage to which to drive the PID controller {0.0}
 * - validateCert - validate control plane TLS certificate {true}
 * - portRange - 2^portRange (0<=portRange<=14) listening ports will be open starting from dataPort. If -1, 
 * then the number of ports matches either the number of CPU cores or the number of threads. Normally
 * this value is calculated based on the number of cores or threads requested, but
 * it can be overridden here. Use with caution. {-1}
 * - withLBHeader - expect LB header to be included (mainly for testing, as normally LB strips it off in
 * normal operation) {false}
 * - eventTimeout_ms - how long (in ms) we allow events to remain in assembly before we give up {500}
 * - rcvSocketBufSize - socket buffer size for receiving set via SO_RCVBUF setsockopt. Note
 * that this requires systemwide max set via sysctl (net.core.rmem_max) to be higher. {3MB}
 * - weight - weight given to this node in terms of processing power
 * - min_factor - multiplied with the number of slots that would be assigned evenly to determine min number of slots
 * for example, 4 nodes with a minFactor of 0.5 = (512 slots / 4) * 0.5 = min 64 slots
 * - max_factor - multiplied with the number of slots that would be assigned evenly to determine max number of slots
 * for example, 4 nodes with a maxFactor of 2 = (512 slots / 4) * 2 = max 256 slots set to 0 to specify no maximum
 */
public class ReassemblerFlags {
    public boolean useCP;
    public boolean useHostAddress;
    public int period_ms;
    public boolean validateCert;
    public float Ki, Kp, Kd, setPoint;
    public long epoch_ms;
    public int portRange; 
    public boolean withLBHeader;
    public int eventTimeout_ms;
    public int rcvSocketBufSize; 
    public float weight, min_factor, max_factor;

    public ReassemblerFlags(){
        useCP = true;
        useHostAddress = false;
        period_ms = 100;
        validateCert = true;
        Ki = 0.0f;
        Kd = 0.0f;
        Kp = 0.0f;
        setPoint = 0.0f;
        epoch_ms = 1000;
        portRange = -1;
        withLBHeader = false;
        eventTimeout_ms = 500;
        rcvSocketBufSize = 1024 * 1024 * 3;
        weight = 1.0f;
        min_factor = 0.5f;
        max_factor = 2.0f;    }
}
