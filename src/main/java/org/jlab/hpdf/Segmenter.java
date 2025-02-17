package org.jlab.hpdf;

import org.jlab.hpdf.config.SegmenterFlags;
import org.jlab.hpdf.messages.SyncStats;
import org.jlab.hpdf.messages.SendStats;
import org.jlab.hpdf.exceptions.E2sarNativeException;

import java.nio.ByteBuffer;

/**
 * This is a JNI wrapper class for e2sar::Segmneter in cpp. It creates an instance of e2sar::Segmenter and the pointer to this object 
 * is stored in nativeSegmenter field. This class takes in events only as DirectByteBuffers to reduce copying between native code.
 * A globalReference is created in the native code and freed after the segmentation proces is done. 
 * No explicit memory management of events is needed, the DirectByteBuffer will eventually go out of scope in native code.
 * 
 * The Segmenter class knows how to break up the provided
 * events into segments consumable by the hardware loadbalancer.
 * It relies on header structures to segment into UDP packets and 
 * follows other LB rules while doing it.
 * It runs on or next to the source of events.
 * 
 * REMEMBER TO CALL FREE() ONCE DONE WITH THIS CLASS TO FREE THE NATIVE INSTANCE CREATED. MEMORY LEAKS WILL HAPPEN IF FREE IS NOT CALLED
 */
public class Segmenter{

    static{
        System.loadLibrary("jnie2sar");
    }

    /**
     *  stores the pointer of the native Segmenter created.
     */
    private long nativeSegmenter;

    private native long initSegmentor(EjfatURI dpUri, int dataId, long eventSrcId, SegmenterFlags sFlags) throws E2sarNativeException;

    private native long initSegmentor(EjfatURI dpUri, int dataId, long eventSrcId, String iniFile) throws E2sarNativeException;

    private native long initSegmentor(EjfatURI dpUri, int dataId, long eventSrcId) throws E2sarNativeException;

    /**
     * Constructor to create segmenter with SegmenterFlags
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param dataId - unique identifier of the originating segmentation point (e.g. a DAQ), carried in SAR header
     * @param eventSrcId - unique identifier of an individual LB packet transmitting host/daq, 
     * 32-bit(long is used here because in native code u_int_32 is used) to accommodate IP addresses more easily, carried in Sync header
     * @param sFlags - SegmenterFlags
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code
     */
    public Segmenter(EjfatURI dpUri, int dataId, long eventSrcId, SegmenterFlags sFlags) throws E2sarNativeException{
        nativeSegmenter = initSegmentor(dpUri, dataId, eventSrcId, sFlags);
    }

    /**
     * Constructor to create segmenter with ini filepath for SegmenterFlags
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param dataId - unique identifier of the originating segmentation point (e.g. a DAQ), carried in SAR header
     * @param eventSrcId - unique identifier of an individual LB packet transmitting host/daq, 
     * 32-bit(long is used here because in native code u_int_32 is used) to accommodate IP addresses more easily, carried in Sync header
     * @param iniFile - file path for Segmenter config.ini
     * @throws E2sarNativeException - If there is an error in reading iniFile or dpUri not in expected format or any other error caused in native code
     */
    public Segmenter(EjfatURI dpUri, int dataId, long eventSrcId, String iniFile) throws E2sarNativeException{
        nativeSegmenter = initSegmentor(dpUri, dataId, eventSrcId, iniFile);
    }

    /**
     * Constructor to create segmenter with default SegmenterFlags
     * @param dpUri - Should be an instance URI. Java object which will be converted to CPP EjfatURI in native method.
     * This is copied, not by reference internally, remember to free 
     * @param dataId - unique identifier of the originating segmentation point (e.g. a DAQ), carried in SAR header
     * @param eventSrcId - unique identifier of an individual LB packet transmitting host/daq, 
     * 32-bit(long is used here because in native code u_int_32 is used) to accommodate IP addresses more easily, carried in Sync header
     * @throws E2sarNativeException - If dpUri is not in expected format or any other error caused in native code
     */
    public Segmenter(EjfatURI dpUri, int dataId, long eventSrcId) throws E2sarNativeException{
        nativeSegmenter = initSegmentor(dpUri, dataId, eventSrcId);
    }

    /**
     * Open sockets and start the threads - this marks the moment
     * from which sync packets start being sent.
     * @throws E2sarNativeException - If there is an error open sockets or starting the internal threads
     */
    public void openAndStart() throws E2sarNativeException{ openAndStart(this.nativeSegmenter);}
    private native void openAndStart(long nativeSegmenter) throws E2sarNativeException;

    /**
     * Send immediately overriding event number.
     * @param buffer - event buffer which should be a Direct ByteBuffer
     * @param eventNumber - override the internal event number (0 is default and will use the internal eventNumber)
     * @param dataId - override the dataId (0 id default and will use the internal dataId passed in constructor)
     * @param entropy - optional event entropy value (0 is default and random will be generated otherwise)
     * @throws E2sarNativeException - If there is an error sending this event to LB
     */
    public void sendEventDirect(ByteBuffer buffer, long eventNumber, int dataId, int entropy) throws E2sarNativeException{
        if(!buffer.isDirect()){
            throw new E2sarNativeException("This method only supports direct ByteBuffers");
        }
        sendEventDirect(nativeSegmenter, buffer, buffer.capacity(), eventNumber, dataId, entropy);
    }
    private native void sendEventDirect(long nativeSegmenter, ByteBuffer buffer, int capacity, long eventNumber, int dataId, int entropy) throws E2sarNativeException;
    
    /**
     * Add to send queue in a nonblocking fashion, overriding internal event number
     * @param buffer - event buffer which should be a Direct ByteBuffer. This will remain as a Global reference in native code and deleted after segmentation
     * @param eventNumber - override the internal event number (0 is default and will use the internal eventNumber)
     * @param dataId - override the dataId (0 id default and will use the internal dataId passed in constructor)
     * @param entropy - optional event entropy value (0 is default and random will be generated otherwise)
     * @throws E2sarNativeException - If there is an error adding this event to the queue
     */
    public void addToSendQueueDirect(ByteBuffer buffer, long eventNumber, int dataId, int entropy) throws E2sarNativeException{
        if(!buffer.isDirect()){
            throw new E2sarNativeException("This method only supports direct ByteBuffers");
        }
        addToSendQueueDirect(nativeSegmenter, buffer, buffer.capacity(), eventNumber, dataId, entropy);
    }   
    private native void addToSendQueueDirect(long nativeSegmenter, ByteBuffer buffer, int capacity, long eventNumber, int dataId, int entropy) throws E2sarNativeException;

    /**
     * Get the MTU currently in use by segmenter
     * @return the MTU as int
     */
    public int getMTU() { return getMTU(nativeSegmenter);}
    private native int getMTU(long nativeSegmenter);

    /**
     * get the maximum payload length used by the segmenter
     * @return maximum payload length internally is size_t so might be -ve
     */
    public long getMaxPayloadLength() { return getMaxPayloadLength(nativeSegmenter);}
    private native long getMaxPayloadLength(long nativeSegmenter);

    /**
     * Get the SyncStats - sync msg cnt, sync err cnt, last errno
     * @return SyncStats instance
     */
    public SyncStats getSyncStats() {return getSyncStats(nativeSegmenter);}
    private native SyncStats getSyncStats(long nativeSegmenter);
    
    /**
     * Get the SendStats - event datagrams cnt, event datagrams err cnt, last errno
     * @return SendStats instance
     */
    public SendStats getSendStats() { return getSendStats(nativeSegmenter);}
    private native SendStats getSendStats(long nativeSegmenter);

    private native void freeNativePointer(long nativeSegmenter);

    /**
     * Method to free the native instance. If called multiple time it will be a noop
     */
    public void free(){
        if(nativeSegmenter != 0){
            freeNativePointer(nativeSegmenter);
            nativeSegmenter = 0;
        }
    }
}