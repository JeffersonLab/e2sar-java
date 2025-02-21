package org.jlab.hpdf;

import java.util.List;
import java.time.Instant;

import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.LBOverview;
import org.jlab.hpdf.messages.LBStatus;

/**
 * This is a JNI wrapper class for e2sar::LBManager in cpp. It creates an instance of e2sar::LBManager and the pointer to this object 
 * is stored in nativeLbManager field 
 * 
 * The LBManager speaks to LB control plane over gRPC.  It can be run from Segmenter, Reassembler or a third party like the workflow manager.
 * 
 * REMEMBER TO CALL FREE() ONCE DONE WITH THIS CLASS TO FREE THE NATIVE INSTANCE CREATED. MEMORY LEAKS WILL HAPPEN IF FREE IS NOT CALLED
 */
public class LbManager {

    static{
        System.loadLibrary("jnie2sar");
    }
    /**
     *  stores the pointer of the native LBManager created.
     */
    private long nativeLbManager;
    /**
     * Internal URI object which will only be populated when getEjfatURI() is called
     */
    private EjfatURI uri;
   
    /**
     * Constructor to create LbManager
     * @param uri Java object which will be converted to CPP EjfatURI in native method, This is copied, not by reference internally, remember to free 
     * @param validateServer if false, skip server certificate validation (useful for self-signed testing)
     * @param useHostAddress even if hostname is provided, use host address as resolved by URI object (with preference for 
     * IPv4 by default or for IPv6 if explicitly requested)
     * @throws E2sarNativeException - If native e2sar::LBManager could not be created
     */
    public LbManager(EjfatURI uri, boolean validateServer, boolean useHostAddress)throws E2sarNativeException{
        this(uri,validateServer,useHostAddress, new String[3], false);
    }

    /**
     * Constructor to create LbManager with ssl credentials 
     * @param uri Java object which will be converted to CPP EjfatURI in native method, This is copied, not by reference internally, remember to free
     * @param validateServer if false, skip server certificate validation (useful for self-signed testing)
     * @param useHostAddress even if hostname is provided, use host address as resolved by URI object (with preference for 
     * IPv4 by default or for IPv6 if explicitly requested)
     * @param sslCredOpts obtaining server root certs, client key and client cert (in this order) 
     * use of SSL/TLS is governed by the URI scheme ('ejfat' vs 'ejfats').
     * @param sslCredOptsFromFile if true, assumes the contents of sslCredentialOptions are filepaths to the certificates
     * @throws E2sarNativeException - If native e2sar::LBManager could not be created
     */
    public LbManager(EjfatURI uri, boolean validateServer, boolean useHostAddress, String[] sslCredOpts, boolean sslCredOptsFromFile)throws E2sarNativeException{
        nativeLbManager = initLbManager(uri,validateServer,useHostAddress,sslCredOpts,sslCredOptsFromFile);
    }
    
    private native long initLbManager(EjfatURI uri, boolean validateServer, boolean useHostAddress, String[] sslCredOpts, boolean sslCredentialOptionsfromFile);
   
    /**
     * Reserve a new load balancer with this name until specified time. It sets the intstance
     * token on the internal URI object.
     * @param lbName - LB name internal to you
     * @param duration - for how long it is needed as String. Internally it is converted to boost::posix_time::time_duration 
     * from string like "23:59:59.000".
     * @param senders -  list of sender IP addresses
     * @return - FPGA LB ID, for use in correlating logs/metrics
     * @throws E2sarNativeException - If reserve fails it will throw this error with a message
     */
    public int reserveLB(String lbName, String duration, List<String> senders) throws E2sarNativeException{return reserveLB(nativeLbManager, lbName, duration, senders);}
    private native int reserveLB(long nativeLbManager, String lbName, String duration, List<String> senders) throws E2sarNativeException;

    
    /**
     * Reserve a new load balancer with this name of duration in seconds
     * @param lbName - LB name internal to you
     * @param seconds - for how long it is needed in seconds
     * @param senders - list of sender IP addresses
     * @return - FPGA LB ID, for use in correlating logs/metrics
     * @throws E2sarNativeException - If reserve fails it will throw this error with a message
     */
    public int reserveLB(String lbName, double seconds, List<String> senders)throws E2sarNativeException {return reserveLB(nativeLbManager, lbName, seconds, senders);}
    private native int reserveLB(long nativeLbManager, String lbName, double seconds, List<String> senders)throws E2sarNativeException;
    
    /**
     * Get load balancer info - it updates the info inside the EjfatURI object just like reserveLB.
     * Uses admin token of the internal URI object. Note that unlike reserve it does NOT set
     * the instance token - it is not available.
     *  
     * @param lbid - - externally provided lb id, in this case the URI only needs to contain
     * the cp address and admin token and it will be updated to contain dataplane and sync addresses.
     * @throws E2sarNativeException - If the native method fails it will throw this error with a message
     */
    public void getLB(String lbid) throws E2sarNativeException{getLB(nativeLbManager, lbid);}
    private native void getLB(long nativeLbManager, String lbid) throws E2sarNativeException;

    /**
     * Get load balancer info using lb id in the URI object
     * 
     * @throws E2sarNativeException - If the native method fails it will throw this error with a message
     */
    public void getLB() throws E2sarNativeException{getLB(nativeLbManager);}
    private native void getLB(long nativeLbManager) throws E2sarNativeException;

    /**
     * Get load balancer status including list of workers, sender IP addresses
     * using lb id in the URI object
     * @return LBStatus instance
     * @throws E2sarNativeException - If there is an error reaching the LB or if LBStatus object could not be created
     */
    public LBStatus getStatus() throws E2sarNativeException {return getStatus(nativeLbManager);}
    private native LBStatus getStatus(long nativeLbManager) throws E2sarNativeException;

    /**
     * Get load balancer status including list of workers, sender IP addresses
     * @param lbid id of reserved LB
     * @return LBStatus instance
     * @throws E2sarNativeException - If there is an error reaching the LB or if LBStatus object could not be created
     */
    public LBStatus getStatus(String lbid) throws E2sarNativeException {return getStatus(nativeLbManager, lbid);}
    private native LBStatus getStatus(long nativeLbManager, String lbid) throws E2sarNativeException;

    /**
     * Get an 'overview' of reserved load balancer instances
     * @return List LBOverview of all reserved LB Instances
     * @throws E2sarNativeException - If there is an error reaching the LB or if LBOverview object could not be created
     */
    public List<LBOverview> getOverview() throws E2sarNativeException {return getOverview(nativeLbManager);}
    private native List<LBOverview> getOverview(long nativeLbManager) throws E2sarNativeException;

    /**
     * Add 'safe' sender addresses to CP to allow these sender to send data to the LB
     * @param senders - list of sender IP addresses
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void addSenders(List<String> senders) throws E2sarNativeException {addSenders(nativeLbManager, senders);}
    private native void addSenders(long nativeLbManager, List<String> senders) throws E2sarNativeException;

    /**
     * Remove 'safe' sender addresses from CP to disallow these senders to send data
     * @param senders - list of sender IP addresses
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void removeSenders(List<String> senders) throws E2sarNativeException {removeSenders(nativeLbManager, senders);}
    private native void removeSenders(long nativeLbManager, List<String> senders) throws E2sarNativeException;

    /**
     * Free previously reserved load balancer. Uses admin token.
     * @param lbid - externally provided lbid, in this case the URI only needs to contain
     * cp address and admin token
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void freeLB(String lbid) throws E2sarNativeException {freeLB(nativeLbManager, lbid);}
    private native void freeLB(long nativeLbManager, String lbid) throws E2sarNativeException;

    /**
     * Free previously reserved load balancer. Uses admin token and uses LB ID obtained
     * from reserve call on the same LBManager object.
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void freeLB() throws E2sarNativeException {freeLB(nativeLbManager);}
    private native void freeLB(long nativeLbManager) throws E2sarNativeException;
    
    /**
     * Register a workernode/backend with an allocated loadbalancer. Note that this call uses
     * instance token. It sets session token and session id on the internal
     * URI object. Note that a new worker must send state immediately (within 10s)
     * or be automatically deregistered.
     * @param nodeName - name of the node (can be FQDN)
     * @param nodeIP - String represeting IP address of the worker
     * @param port - starting UDP port on which it listens
     * @param weight - weight given to this node in terms of processing power
     * @param source_count - how many sources we can listen to (gets converted to port range [0,14])
     * @param min_factor - multiplied with the number of slots that would be assigned evenly to determine min number of slots
     * for example, 4 nodes with a minFactor of 0.5 = (512 slots / 4) * 0.5 = min 64 slots
     * @param max_factor - multiplied with the number of slots that would be assigned evenly to determine max number of slots
     * for example, 4 nodes with a maxFactor of 2 = (512 slots / 4) * 2 = max 256 slots set to 0 to specify no maximum
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void registerWorker(String nodeName, String nodeIP, int port, float weight, int source_count, float min_factor, float max_factor) throws E2sarNativeException{
        registerWorker(nativeLbManager, nodeName, nodeIP, port, weight, source_count, min_factor, max_factor);
    }
    private native void registerWorker(long nativeLbManager, String nodeName, String nodeIP, int port, float weight, int source_count, float min_factor, float max_factor) throws E2sarNativeException;
    
    /**
     * Deregister worker using session ID and session token from the register call
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void deregisterWorker() throws E2sarNativeException {deregisterWorker(nativeLbManager);}
    private native void deregisterWorker(long nativeLbManager) throws E2sarNativeException;

    /**
     * Send worker state update using session ID and session token from register call. Automatically
     * uses localtime to set the timestamp. Workers are expected to send state every 100ms or so.
     * @param fill_percent - [0:1] percentage filled of the queue
     * @param control_signal - change to data rate
     * @param isReady - if true, worker ready to accept more data, else not ready
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void sendState(float fill_percent, float control_signal, boolean isReady) throws E2sarNativeException {
        sendState(nativeLbManager, fill_percent, control_signal, isReady);
    }
    private native void sendState(long nativeLbManager, float fill_percent, float control_signal, boolean isReady) throws E2sarNativeException;

    /**
     * Send worker state update using session ID and session token from register call. Allows to explicitly
     * set the timestamp.
     * @param fill_percent - [0:1] percentage filled of the queue
     * @param control_signal - change to data rate
     * @param isReady - if true, worker ready to accept more data, else not ready
     * @param timestamp - this will be converted to google::protobuf::Timestamp timestamp for this message (if you want to explicitly not use localtime)
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public void sendState(float fill_percent, float control_signal, boolean isReady, Instant timestamp) throws E2sarNativeException {
        sendState(nativeLbManager, fill_percent, control_signal, isReady, timestamp);
    }
    private native void sendState(long nativeLbManager, float fill_percent, float control_signal, boolean isReady, Instant timestamp) throws E2sarNativeException;

    /**
     * Get the version of the load balancer (the commit string)
     * @return the result with commit string, build tag and compatTag in
     * @throws E2sarNativeException - If there is an error reaching the LB
     */
    public List<String> version() throws E2sarNativeException {return version(nativeLbManager);}
    private native List<String> version(long nativeLbManager) throws E2sarNativeException;

    /**
     * Return the address string used by gRPC to connect to control plane. Can be
     * in the format of hostname:port or ipv4:///W.X.Y.Z:port or ipv6:///[XXXX::XX:XXXX]:port
     * @return the string containing the address
     */
    public String getAddrString(){return getAddrString(nativeLbManager);}
    private native String getAddrString(long nativeLbManager);

    private native long getInternalUri(long nativeLbManager);

    /**
     * Gets the Internal EjfatURI used by the LBManager. The first time this is called a new EjfatURI() object is created
     * Do not free this EjfatURI instance it will fail. 
     * @return EjfatURI instance
     */
    public EjfatURI getEjfatURI(){
        if(uri == null){
            long internalUri = getInternalUri(nativeLbManager);
            uri = new EjfatURI(internalUri);
        }
        return uri;
    }
    
    private native void freeNativePointer(long nativeLbManager);

    /**
     * Method to free the native instance. If called multiple time it will be a noop
     */
    public void free(){
        if(nativeLbManager != 0){
            freeNativePointer(nativeLbManager);
            nativeLbManager = 0;
        }
    }
    
}
