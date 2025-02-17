package org.jlab.hpdf;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import org.jlab.hpdf.config.ReassemblerFlags;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.LostEvent;
import org.jlab.hpdf.messages.ReassembledEvent;
import org.jlab.hpdf.messages.RecvStats;

/**
 * This is a JNI wrapper class for e2sar::Reassembler in cpp. It creates an instance of e2sar::Reassembler and the pointer to this object 
 * is stored in nativeReassembler field. Events that are reassembled are dynamically allocated in native code and DirectByteBuffers are used
 * to move it to Java. You have to call freeDirectBytebBuffer(ReassembledEvent) to free this memory or leaks will happen.
 * 
 * The Reassembler class knows how to reassemble the events back. It relies
 * on the RE header structure to reassemble the event, because the LB portion
 * of LBRE header is stripped off by the load balancer. 
 * It runs on or next to the worker performing event processing
 * 
 * REMEMBER TO CALL FREE() ONCE DONE WITH THIS CLASS TO FREE THE NATIVE INSTANCE CREATED. MEMORY LEAKS WILL HAPPEN IF FREE IS NOT CALLED
 */
public class Reassembler {
    
    static{
        System.loadLibrary("jnie2sar");
    }

    /**
     *  stores the pointer of the native Segmenter created.
     */
    private long nativeReassembler;
    /**
     *  stores the DirectByteBuffers created by the native code that have not yet been freed.
     */
    private HashSet<ByteBuffer> allocatedBuffers;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList, ReassemblerFlags rFlags) throws E2sarNativeException;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList, String iniFile) throws E2sarNativeException;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList) throws E2sarNativeException;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, long numReceiveThreads, ReassemblerFlags rFlags) throws E2sarNativeException;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, long numReceiveThreads, String iniFile) throws E2sarNativeException;

    private native long initReassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, long numReceiveThreads) throws E2sarNativeException;

    /**
     * Create a reassembler object to run receive on a specific set of CPU cores
     * We assume you picked the CPU core list by studying CPU-to-NUMA affinity for the receiver
     * NIC on the target system. The number of started receive threads will match
     * the number of cores on the list. For the started receive threads affinity will be 
     * set to these cores. 
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param cpuCoreList - list of core identifiers to be used for receive threads
     * @param rFlags - ReassemblerFlags object with reassembler config
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList, ReassemblerFlags rFlags) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, cpuCoreList, rFlags);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Create a reassembler object to run receive on a specific set of CPU cores
     * We assume you picked the CPU core list by studying CPU-to-NUMA affinity for the receiver
     * NIC on the target system. The number of started receive threads will match
     * the number of cores on the list. For the started receive threads affinity will be 
     * set to these cores. 
     * ReassemblerFlags are parsed through INI file
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param cpuCoreList - list of core identifiers to be used for receive threads
     * @param iniFile - file path for Segmenter config.ini
     * @throws E2sarNativeException - If there is an error in reading iniFile or dpUri not in expected format or any other error caused in native code
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList, String iniFile) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, cpuCoreList, iniFile);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Create a reassembler object to run receive on a specific set of CPU cores
     * We assume you picked the CPU core list by studying CPU-to-NUMA affinity for the receiver
     * NIC on the target system. The number of started receive threads will match
     * the number of cores on the list. For the started receive threads affinity will be 
     * set to these cores. 
     * Default ReassemblerFlags used
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param cpuCoreList - list of core identifiers to be used for receive threads
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort,  List<Integer> cpuCoreList) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, cpuCoreList);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Create a reassembler object to run on a specified number of receive threads
     * without taking into account thread-to-CPU and CPU-to-NUMA affinity.
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param numReceiveThreads - number of threads
     * @param rFlags - ReassemblerFlags object with reassembler config
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code 
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, int numReceiveThreads, ReassemblerFlags rFlags) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, numReceiveThreads, rFlags);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Create a reassembler object to run on a specified number of receive threads
     * without taking into account thread-to-CPU and CPU-to-NUMA affinity.
     * ReassemblerFlags are parsed through INI file
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param numReceiveThreads - number of threads
     * @param iniFile - file path for Reassembler config.ini
     * @throws E2sarNativeException - If there is an error in reading iniFile or dpUri not in expected format or any other error caused in native code
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, int numReceiveThreads, String iniFile) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, numReceiveThreads, iniFile);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Create a reassembler object to run on a specified number of receive threads
     * without taking into account thread-to-CPU and CPU-to-NUMA affinity.
     * Default ReassemblerFlags used
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param ipAddress - IP address (v4 or v6) on which we are listening
     * @param startingPort - starting port number on which we are listening
     * @param numReceiveThreads - number of threads
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code
     */
    public Reassembler(EjfatURI dpUri, InetAddress ipAddress, int startingPort, int numReceiveThreads) throws E2sarNativeException{
        nativeReassembler = initReassembler(dpUri, ipAddress, startingPort, numReceiveThreads);
        allocatedBuffers = new HashSet<>();
    }

    /**
     * Register a worker with the control plane
     * @param nodeName - name of this node (any unique string)
     * @throws E2sarNativeException - If there is an error contacting the LB
     */
    public void registerWorker(String nodeName) throws E2sarNativeException { registerWorker(nativeReassembler, nodeName);}
    private native void registerWorker(long nativeReassembler, String nodeName) throws E2sarNativeException;

    /**
     * Deregister this worker
     * @throws E2sarNativeException - If there is an error contacting the LB
     */
    public void deregisterWorker() throws E2sarNativeException { deregisterWorker(nativeReassembler);}
    private native void deregisterWorker(long nativeReassembler) throws E2sarNativeException;

    /**
     * Open sockets and start the threads - this marks the moment
     * from which we are listening for incoming packets, assembling
     * them into event buffers and putting them into the queue.
     * @throws E2sarNativeException - If there is an error open sockets or starting the internal threads
     */
    public void openAndStart() throws E2sarNativeException { openAndStart(nativeReassembler);} 
    private native void openAndStart(long nativeReassembler) throws E2sarNativeException;

    /**
     * A non-blocking call to get an assembled event off a reassembled event queue
     * @return if an error occurs or if there's no event available Optional.empty() is returned otherwise the ReassembledEvent
     * Need to call freeDirectBytebBuffer(ReassembledEvent) to free the buffer created 
     */
    public Optional<ReassembledEvent> getEvent(){ return getEvent(nativeReassembler);}
    private native Optional<ReassembledEvent> getEvent(long nativeReassembler);

    /**
     * Blocking variant of getEvent() 
     * @param waitMs - how long to block before giving up, defaults to 0 - forever
     * @return if an error occurs or if there's no event available Optional.empty() is returned otherwise the ReassembledEvent
     * Need to call freeDirectBytebBuffer(ReassembledEvent) to free the buffer created 
     */
    public Optional<ReassembledEvent> recvEvent(long waitMs) { return recvEvent(nativeReassembler, waitMs);}
    private native Optional<ReassembledEvent> recvEvent(long nativeReassembler, long waitMs);

    /**
     * Get an Instance of RecvStats containing - enqueueLoss, eventSuccess, lastErrno, grpcErrCnt, dataErrCnt, lastE2SARError
     * @return RecvStats instance
     */
    public RecvStats getStats() { return getStats(nativeReassembler);}
    private native RecvStats getStats(long nativeReassembler);

    /**
     * Try to pop an event number of a lost event from the queue that stores them
     * @return if an error occurs or if there's no event available Optional.empty() is returned otherwise the LostEvent
     */
    public Optional<LostEvent> getLostEvent() { return getLostEvent(nativeReassembler);}
    private native Optional<LostEvent> getLostEvent(long nativeReassembler);

    /**
     * Get the number of threads this Reassembler is using
     * @return number of receiver threads
     */
    public long getNumRecvThreads() {return getNumRecvThreads(nativeReassembler);}
    private native long getNumRecvThreads(long nativeReassembler);

    /**
     * Get the ports this reassembler is listening on, returned as a List with two items <start port, end port>
     * @return List<Integer> of size 2 containing {start port, end port} 
     */
    public List<Integer> getRecvPorts() { return getRecvPorts(nativeReassembler);}
    private native List<Integer> getRecvPorts(long nativeReassembler);

    /**
     * Get the port range that will be communicated to CP (this is either specified explicitly
     * as part of ReassemblerFlags or computed from the number of cores or threads requested)
     * @return port range as integer
     */
    public int getPortRange() { return getPortRange(nativeReassembler);}
    private native int getPortRange(long nativeReassembler);

    /**
     * This will free the ByteBuffer associated with a ReassembledEvent. It will be a noop if called twice
     * @param event - ReassembledEvent obtained from getEvent() or recvEvent()
     */
    public void freeDirectBytebBuffer(ReassembledEvent event){
        if(allocatedBuffers.contains(event.byteBuffer)){
            freeDirectBytebBuffer(nativeReassembler, event.byteBuffer);
            allocatedBuffers.remove(event.byteBuffer);
        }
    }
    private native void freeDirectBytebBuffer(long nativeReassembler, ByteBuffer buffer);
    
    /**
     * Method to free the native instance. If called multiple time it will be a noop
     * This method also frees all Buffers that have yet to be freed
     */
    public void free(){
        if(nativeReassembler != 0){
            for(ByteBuffer buffer : allocatedBuffers){
                freeDirectBytebBuffer(nativeReassembler, buffer);
            }
            freeNativePointer(nativeReassembler);
            nativeReassembler = 0;
        }
    }
    private native void freeNativePointer(long nativeReassembler);
    
}
