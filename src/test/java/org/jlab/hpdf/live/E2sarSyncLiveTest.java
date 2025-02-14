package org.jlab.hpdf.live;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.LbManager;
import org.jlab.hpdf.Segmenter;
import org.jlab.hpdf.config.SegmenterFlags;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.SyncStats;
import org.junit.jupiter.api.Test;

// these tests test the sync thread and the sending of
// the sync messages against live UDPLBd. 
public class E2sarSyncLiveTest {

    static{
        System.loadLibrary("jnie2sar");
    }
    
    @Test
    void SyncTest(){
        System.out.println("DPSyncLiveTest1: test sync thread against UDPLBd by sending 10 sync frames (once a second for 10 seconds)");

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

        // create a segmenter and start the threads
        // using the updated URI with sync info
        System.out.println("Creating segmenter using returned URI: " + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE)); 
        Segmenter segmenter = null;

        try{
            segmenter = new Segmenter(lbman.getEjfatURI(), dataId, eventSrcId, sFlags);
            segmenter.openAndStart();
            System.out.println("Running sync test for 10 seconds");
            Thread.sleep(10000);
        }
        catch(E2sarNativeException e){
            e.printStackTrace();
            fail("Could not open send sockets");
        }
        catch(InterruptedException e){

        }
        SyncStats syncStats = segmenter.getSyncStats();
        assertEquals(10, syncStats.syncMsgCount);
        assertEquals(0, syncStats.syncErrCount);

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
