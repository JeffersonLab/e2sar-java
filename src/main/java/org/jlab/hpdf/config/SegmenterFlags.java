package org.jlab.hpdf.config;

/**
 * Because of the large number of constructor parameters in Segmenter
 * we make this a structure with sane defaults
 * - dpV6 - prefer V6 dataplane if the URI specifies both data=<ipv4>&data=<ipv6> addresses {false}
 * - zeroCopy - use zeroCopy send optimization {false}
 * - connectedSocket - use connected sockets {true}
 * - useCP - enable control plane to send Sync packets {true}
 * - zeroRate - don't provide event number change rate in Sync {false}
 * - clockAsEventNum - use usec clock samples as event numbers in LB and Sync packets {false}
 * - syncPeriodMs - sync thread period in milliseconds {1000}
 * - syncPerods - number of sync periods to use for averaging reported send rate {2}
 * - mtu - size of the MTU to attempt to fit the segmented data in (must accommodate
 * IP, UDP and LBRE headers) {1500}
 * - numSendSockets - number of sockets/source ports we will be sending data from. The
 * more, the more randomness the LAG will see in delivering to different FPGA ports. {4}
 * - sndSocketBufSize - socket buffer size for sending set via SO_SNDBUF setsockopt. Note
 * that this requires systemwide max set via sysctl (net.core.wmem_max) to be higher. {3MB}
 */
public class SegmenterFlags{
    public boolean dpV6; 
    public boolean zeroCopy;
    public boolean connectedSocket;
    public boolean useCP;
    public boolean zeroRate;
    public boolean usecAsEventNum;
    public int syncPeriodMs;
    public int syncPeriods;
    public int mtu;
    public long numSendSockets;
    public int sndSocketBufSize;

    public SegmenterFlags(){
        this.dpV6 = false;
        this.zeroCopy = false;
        this.connectedSocket = true;
        this.useCP = true;
        this.zeroRate = false;
        this.usecAsEventNum = true;
        this.syncPeriodMs = 1000;
        this.syncPeriods = 2;
        this.mtu = 1500;
        this.numSendSockets = 4;
        this.sndSocketBufSize = 1024 * 1024 * 3;
    }
}