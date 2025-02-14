package org.jlab.hpdf.live;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.LbManager;
import org.jlab.hpdf.Segmenter;
import org.jlab.hpdf.config.SegmenterFlags;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.SendStats;
import org.jlab.hpdf.messages.SyncStats;
import org.junit.jupiter.api.Test;

public class E2sarSegmenterLiveTest {
    
    static{
        System.loadLibrary("jnie2sar");
    }
    
    // these tests test the sync thread and the sending of
    // the sync messages against live UDPLBd. 
    @Test
    void SegLiveTest1(){
        System.out.println("DPSegLiveTest1: test segmenter (and sync thread) against UDPLBd by sending 5 events via event queue with default MTU so 5 frames are sent");
        
        EjfatURI uri = null;
        LbManager lbman = null;
        try {
            uri = EjfatURI.getFromEnv("EJFAT_URI", EjfatURI.Token.ADMIN, false);
            lbman = new LbManager(uri, false, false);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        String duration = "01";
        String lbname = "mylb";
        List<String> senders = new ArrayList<>();
        senders.add("192.168.100.1");
        senders.add("192.168.100.2");

        try {
            int fpgaId = lbman.reserveLB(lbname, duration, senders);
            assert(lbman.getEjfatURI().getInstanceToken().length() > 0);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        assert(lbman.getEjfatURI().hasSyncAddr());
        assert(lbman.getEjfatURI().hasDataAddr());

        int dataId = 0x0505;
        long eventSrcId = 0x11223344;
        SegmenterFlags sFlags = new SegmenterFlags();
        sFlags.syncPeriodMs = 1000; // in ms
        sFlags.syncPeriods = 5; // number of sync periods to use for sync

        System.out.println("Creating segmenter using returned URI: " + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE)); 
        Segmenter segmenter = null;
        try{
            segmenter = new Segmenter(lbman.getEjfatURI(), dataId, eventSrcId, sFlags);
            segmenter.openAndStart();
            System.out.println("Running data test for 10 seconds against sync " + 
                lbman.getEjfatURI().getSyncAddr().toString() + " and data " + lbman.getEjfatURI().getDataAddrv4().toString());
        }
        catch(E2sarNativeException e){
            e.printStackTrace();
            fail("Could not open send sockets");
        }

        String eventString = "THIS IS A VERY LONG EVENT MESSAGE WE WANT TO SEND EVERY 2 SECONDS.";
        System.out.println("The event data is string '" + eventString + "' of length " + eventString.length());

        SendStats sendStats = segmenter.getSendStats();
        if(sendStats.eventDatagramErrCount != 0){
            System.out.println("Error encountered after opening send socket: " + sendStats.lastErrorNo);
        }

        try{
            for(int i=0;i<5;i++){
                byte[] bytes = eventString.getBytes(StandardCharsets.UTF_8);
                ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
                buffer.put(bytes);
                buffer.flip();

                segmenter.addToSendQueueDirect(buffer, 0, 0, 0);
                Thread.sleep(2000);
            }
        }
        catch(E2sarNativeException e){
            e.printStackTrace();
            fail();
        }
        catch (InterruptedException e){

        }
        SyncStats syncStats = segmenter.getSyncStats();
        sendStats = segmenter.getSendStats();

        if(syncStats.syncErrCount != 0){
            System.out.println("Error encountered sending sync frames:" + syncStats.lastErrorNo);
        }
        System.out.println("Sent " + syncStats.syncMsgCount + " sync frames");
        System.out.println("Sent " + sendStats.eventDatagramCount + " data frames");

        assert(syncStats.syncMsgCount >= 10);
        assert(syncStats.syncErrCount == 0);
        assert(sendStats.eventDatagramCount == 5);
        assert(sendStats.eventDatagramErrCount == 0);

        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        segmenter.free();
        uri.free();
        lbman.free();
    }

    // these tests test the sync thread and the sending of
    // the sync messages against live UDPLBd. 
    @Test
    void SegLiveTest2(){
        System.out.println("DPSegLiveTest2: test segmenter (and sync thread) against UDPLBd by sending 5 events via event queue small MTU so 20 frames are sent");
        
        EjfatURI uri = null;
        LbManager lbman = null;
        try {
            uri = EjfatURI.getFromEnv("EJFAT_URI", EjfatURI.Token.ADMIN, false);
            lbman = new LbManager(uri, false, false);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        String duration = "01";
        String lbname = "mylb";
        List<String> senders = new ArrayList<>();
        senders.add("192.168.100.1");
        senders.add("192.168.100.2");

        try {
            int fpgaId = lbman.reserveLB(lbname, duration, senders);
            assert(lbman.getEjfatURI().getInstanceToken().length() > 0);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        assert(lbman.getEjfatURI().hasSyncAddr());
        assert(lbman.getEjfatURI().hasDataAddr());

        int dataId = 0x0505;
        long eventSrcId = 0x11223344;
        SegmenterFlags sFlags = new SegmenterFlags();
        sFlags.syncPeriodMs = 500; // in ms
        sFlags.syncPeriods = 5; // number of sync periods to use for sync
        sFlags.mtu = 64 + 40;

        // create a segmenter using URI sync and data info
        // and start the threads, send MTU is set to force
        // breaking up event payload into multiple frames
        // 64 is the length of all headers (IP, UDP, LB, RE)
        System.out.println("Creating segmenter using returned URI: " + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE)); 
        Segmenter segmenter = null;
        
        try{
            segmenter = new Segmenter(lbman.getEjfatURI(), dataId, eventSrcId, sFlags);
            segmenter.openAndStart();
            System.out.println("Running data test for 10 seconds against sync " + 
                lbman.getEjfatURI().getSyncAddr().toString() + " and data " + lbman.getEjfatURI().getDataAddrv4().toString());
        }
        catch(E2sarNativeException e){
            e.printStackTrace();
            fail("Could not open send sockets");
        }

        String eventString = "THIS IS A VERY LONG EVENT MESSAGE WE WANT TO SEND EVERY 1/2 SECONDS.";
        System.out.println("The event data is string '" + eventString + "' of length " + eventString.length());

        SendStats sendStats = segmenter.getSendStats();
        if(sendStats.eventDatagramErrCount != 0){
            System.out.println("Error encountered after opening send socket: " + sendStats.lastErrorNo);
        }

        try{
            for(int i=0;i<10;i++){
                byte[] bytes = eventString.getBytes(StandardCharsets.UTF_8);
                ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
                buffer.put(bytes);
                buffer.flip();

                segmenter.addToSendQueueDirect(buffer, 0, 0, 0);
                Thread.sleep(500);
            }
        }
        catch(E2sarNativeException e){
            e.printStackTrace();
            fail();
        }
        catch (InterruptedException e){

        }
        SyncStats syncStats = segmenter.getSyncStats();
        sendStats = segmenter.getSendStats();

        if(syncStats.syncErrCount != 0){
            System.out.println("Error encountered sending sync frames:" + syncStats.lastErrorNo);
        }
        System.out.println("Sent " + syncStats.syncMsgCount + " sync frames");
        System.out.println("Sent " + sendStats.eventDatagramCount + " data frames");

        assert(syncStats.syncMsgCount >= 10);
        assert(syncStats.syncErrCount == 0);
        assert(sendStats.eventDatagramCount == 20);
        assert(sendStats.eventDatagramErrCount == 0);

        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        segmenter.free();
        uri.free();
        lbman.free();
    }
}
