package org.jlab.hpdf.live;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.InetAddress;
import java.util.Optional;

import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.Reassembler;
import org.jlab.hpdf.config.ReassemblerFlags;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.LostEvent;
import org.jlab.hpdf.messages.RecvStats;
import org.junit.jupiter.api.Test;

public class E2sarReassemblerLiveTest {
    
    static{
        System.loadLibrary("jnie2sar");
    }

    // these tests test the reassembler background functionality
    // no dataplane traffic is exchanged, but it requires the UDPLBd to be up to send messages to
    @Test
    void ReasLiveTest(){
        System.out.println("DPReasTest1: test reassembler and send state thread");
        
        EjfatURI uri = null;
        try {
            uri = EjfatURI.getFromEnv("EJFAT_URI", EjfatURI.Token.ADMIN, false);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        // create reassembler with no control plane
        ReassemblerFlags rflags = new ReassemblerFlags();
        rflags.validateCert = false;

        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        int listenPort = 10000;
        Reassembler reas = null; 

        try {
            reas = new Reassembler(uri, inetAddress, listenPort, 1, rflags);
            reas.openAndStart();
            // sleep for 5 seconds
            Thread.sleep(5000);
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail("Error encountered opening sockets and starting segmenter threads");
        }
        catch(InterruptedException e){}

        RecvStats recvStats = reas.getStats();
        if(recvStats.enqueueLoss != 0 ){
            System.out.println("Unexpected enqueue loss:" + recvStats.enqueueLoss);
        }

        assert(recvStats.enqueueLoss == 0);
        assert(recvStats.grpcErrCount == 0);
        assert(recvStats.dataErrCount == 0);

        Optional<LostEvent> lostEvenOptional = reas.getLostEvent();
        if(lostEvenOptional.isPresent()){
            LostEvent lostEvent = lostEvenOptional.get();
            System.out.println("LOST EVENT " + lostEvent.eventNum + ":" + lostEvent.dataId);
        }
        else{
            System.out.println("NO EVENT LOSS");
        }

        reas.free();
        uri.free();
    }
    
}
