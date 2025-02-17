package org.jlab.hpdf.messages;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * This class is only instantiated in the native JNI wrapper when a LBOverview needs to be passed to Java
 */
public class LBOverview {
    /**
     * Name of LB
     */
    public String name;
    /**
     * ID of LB
     */
    public String lbid;
    /**
     * Sync Address with port, can be IPv4 or IPv6
     */
    public InetSocketAddress syncAddressAndPort;
    /**
     * Dataplane IPv4 Address
     */
    public InetAddress dataIPv4;
    /**
     * Dataplane IPv6 Address
     */
    public InetAddress dataIPv6;
    /**
     * FPGA ID associated with the LB
     */
    public int fpgaLbid;
    /**
     * Status of given LB
     */
    public LBStatus status;

    /**
     * Default Constructor with InetSocketAddress for sync IP:port
     * @param name - name of LB
     * @param lbid - id of LB
     * @param syncAddressAndPort - Sync Address with port, can be IPv4 or IPv6
     * @param dataIPv4 - Dataplane IPv4 Address
     * @param dataIPv6 - Dataplane IPv6 Address
     * @param fpgaLbid - FPGA ID associated with the LB
     * @param status - Status of given LB
     */
    public LBOverview(String name, String lbid, InetSocketAddress syncAddressAndPort, InetAddress dataIPv4, 
    InetAddress dataIPv6, int fpgaLbid, LBStatus status){
        this.name = name;
        this.lbid = lbid;
        this.syncAddressAndPort = syncAddressAndPort;
        this.dataIPv4 = dataIPv4;
        this.dataIPv6 = dataIPv6;
        this.fpgaLbid = fpgaLbid;
        this.status = status;
    }

    /**
     * Default constructor with syncAddress and syncPort as separate fields
     * @param name - name of LB
     * @param lbid - id of LB
     * @param syncAddress - Sync Address
     * @param syncPort - Sync port
     * @param dataIPv4 - Dataplane IPv4 Address
     * @param dataIPv6 - Dataplane IPv6 Address
     * @param fpgaLbid - FPGA ID associated with the LB
     * @param status - Status of given LB
     * @throws UnknownHostException - if host can't be resolved
     */
    public LBOverview(String name, String lbid, String syncAddress, int syncPort, String dataIPv4, 
    String dataIPv6, int fpgaLbid, LBStatus status) throws UnknownHostException {
        this(name, lbid, InetSocketAddress.createUnresolved(syncAddress, syncPort),
        InetAddress.getByName(dataIPv4), InetAddress.getByName(dataIPv6),
        fpgaLbid, status);
    }
}
