package org.jlab.hpdf.live;

import static org.junit.jupiter.api.Assertions.fail;

import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.LbManager;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.LBOverview;
import org.jlab.hpdf.messages.LBStatus;
import org.jlab.hpdf.messages.WorkerStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

/**
 * these tests necessarily depend on EJFAT_URI environment variable setting as information about live LBCP can't be baked into the test itself
 */
public class E2sarLbCpLiveTest {

    static{
        System.loadLibrary("jnie2sar");
    }
    
    @Test
    void LBMTest1(){
        // reserve then free
        // parse URI from env variable

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

        // call free - this will correctly use the admin token (even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
    }

    @Test
    void LBMTest2(){
        // reseserve, get, then free
        // parse URI from env variable

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

        //Creating another LbManafer to getLb
        LbManager lbman2 = null;
        try {
            lbman2 = new LbManager(uri, false, false);
            lbman2.getLB(lbman.getEjfatURI().getLbid());
            assert(lbman.getEjfatURI().getSyncAddr().equals(lbman2.getEjfatURI().getSyncAddr()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        // call free - this will correctly use the admin token (even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
        lbman2.free();
    }

    @Test
    void LBMTest3(){
        // reserve, register worker, send state, unregister worker, free
        // parse URI from env variable

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
            assert(lbman.getEjfatURI().hasSyncAddr());
            assert(lbman.getEjfatURI().hasDataAddr());
            lbman.registerWorker("my_node", "192.168.101.5", 10000, 0.5f, 10, 1.0f, 1.0f);
            assert(lbman.getEjfatURI().getSessionToken().length() > 0);
            assert(lbman.getEjfatURI().getSessionId().length() > 0);

            // send state - every registered worker must do that every 100ms or be auto-deregistered
            lbman.sendState(0.8f, 1.0f, true);
            // unregister (should use session token and session id)
            lbman.deregisterWorker();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }


        // call free - this will correctly use the admin token (even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
    }

    @Test
    void LBMTest4(){
        // reserve, register worker, send state, unregister worker, free
        // parse URI from env variable

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
        senders.add("192.168.20.1");
        senders.add("192.168.20.2");

        try {
            int fpgaId = lbman.reserveLB(lbname, duration, senders);
            assert(lbman.getEjfatURI().hasSyncAddr());
            assert(lbman.getEjfatURI().hasDataAddr());
            lbman.registerWorker("my_node", "192.168.101.5", 10000, 0.5f, 10, 1.0f, 1.0f);
            assert(lbman.getEjfatURI().getSessionToken().length() > 0);
            assert(lbman.getEjfatURI().getSessionId().length() > 0);

            // send state - every registered worker must do that every 100ms or be auto-deregistered after 10s of silence
            // first 2 seconds of the state are discarded as too noisy
            for (int i = 25; i > 0; i--)
            {
                lbman.sendState(0.8f, 1.0f, true);
                Thread.sleep(100);
            }
            LBStatus lbStatus = lbman.getStatus();
            assert(lbStatus.senderAddresses.size() == 2);
            assert(lbStatus.senderAddresses.get(0).equals("192.168.20.1"));
            assert(lbStatus.workers.size() == 1);
            WorkerStatus wStatus = lbStatus.workers.get(0);
            assert(wStatus.name.equals("my_node"));
            double deltaD = 0.000001;
            assert(Math.abs(wStatus.fillPercent - 0.8) < deltaD);
            assert(Math.abs(wStatus.controlSignal - 1.0) < deltaD);
            System.out.println("Last Updated" + wStatus.lastUpdated.toString());
            
            //unregister
            lbman.deregisterWorker();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        catch(InterruptedException e){

        }

        // call free - this will correctly use the admin token (even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
    }

    @Test
    void LBMTest4_1(){
        // reserve with empty senders, register worker, add senders, get status, 
        // remove senders, get_status, unregister worker, get status, free
        // parse URI from env variable

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
        List<String> emptySenders = new ArrayList<>();
        List<String> senders = new ArrayList<>();
        senders.add("192.168.20.1");
        senders.add("192.168.20.2");

        try {
            int fpgaId = lbman.reserveLB(lbname, duration, emptySenders);
            assert(lbman.getEjfatURI().hasSyncAddr());
            assert(lbman.getEjfatURI().hasDataAddr());
            lbman.registerWorker("my_node", "192.168.101.5", 10000, 0.5f, 10, 1.0f, 1.0f);
            assert(lbman.getEjfatURI().getSessionToken().length() > 0);
            assert(lbman.getEjfatURI().getSessionId().length() > 0);

            // send state - every registered worker must do that every 100ms or be auto-deregistered after 10s of silence
            // first 2 seconds of the state are discarded as too noisy
            for (int i = 25; i > 0; i--)
            {
                lbman.sendState(0.8f, 1.0f, true);
                Thread.sleep(100);
            }

            // call addSenders 
            lbman.addSenders(senders);
            LBStatus lbStatus = lbman.getStatus();
            assert(lbStatus.senderAddresses.size() == 2);
            assert(lbStatus.senderAddresses.get(0).equals("192.168.20.1"));
            assert(lbStatus.workers.size() == 1);
            WorkerStatus wStatus = lbStatus.workers.get(0);
            assert(wStatus.name.equals("my_node"));
            double deltaD = 0.000001;
            assert(Math.abs(wStatus.fillPercent - 0.8) < deltaD);
            assert(Math.abs(wStatus.controlSignal - 1.0) < deltaD);
            System.out.println("Last Updated" + wStatus.lastUpdated.toString());
            
            lbman.removeSenders(senders);
            lbStatus = lbman.getStatus();
            assert(lbStatus.senderAddresses.size() == 0);
            
            //unregister
            lbman.deregisterWorker();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        catch(InterruptedException e){

        }

        // call free - this will correctly use the admin token (even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
    }

    @Test
    void LBMTest5(){
        // version
        // parse URI from env variable

        EjfatURI uri = null;
        LbManager lbman = null;
        try {
            uri = EjfatURI.getFromEnv("EJFAT_URI", EjfatURI.Token.ADMIN, false);
            lbman = new LbManager(uri, false, false);
            List<String> lbVersion = lbman.version();
            assert(lbVersion.size() == 3);
            System.out.println("Version String: " + lbVersion.get(0) + "," + lbVersion.get(1) + "," + lbVersion.get(2));
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }

        uri.free();
        lbman.free();
    }

    @Test
    void LBMTest6(){
        // reserve, register worker, get status, unregister worker, get status, overview, free
        // parse URI from env variable

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
        senders.add("192.168.20.1");
        senders.add("192.168.20.2");

        try {
            int fpgaId = lbman.reserveLB(lbname, duration, senders);
            assert(lbman.getEjfatURI().hasSyncAddr());
            assert(lbman.getEjfatURI().hasDataAddr());
            lbman.registerWorker("my_node", "192.168.101.5", 10000, 0.5f, 10, 1.0f, 1.0f);
            assert(lbman.getEjfatURI().getSessionToken().length() > 0);
            assert(lbman.getEjfatURI().getSessionId().length() > 0);

            // send state - every registered worker must do that every 100ms or be auto-deregistered after 10s of silence
            // first 2 seconds of the state are discarded as too noisy
            for (int i = 25; i > 0; i--)
            {
                lbman.sendState(0.8f, 1.0f, true);
                Thread.sleep(100);
            }
            LBStatus lbStatus = lbman.getStatus();
            assert(lbStatus.senderAddresses.size() == 2);
            assert(lbStatus.senderAddresses.get(0).equals("192.168.20.1"));
            assert(lbStatus.workers.size() == 1);
            WorkerStatus wStatus = lbStatus.workers.get(0);
            assert(wStatus.name.equals("my_node"));
            double deltaD = 0.000001;
            assert(Math.abs(wStatus.fillPercent - 0.8) < deltaD);
            assert(Math.abs(wStatus.controlSignal - 1.0) < deltaD);
            System.out.println("Last Updated" + wStatus.lastUpdated.toString());

            List<LBOverview> overviews = lbman.getOverview();
            LBOverview overview = overviews.get(0);
            assert(overview.name.equals(lbname));
            assert(overview.status.senderAddresses.size() == 2);
            assert(overview.status.senderAddresses.get(0).equals("192.168.20.1"));
            assert(overview.status.workers.size() == 1);
            assert(overview.status.workers.get(0).name.equals("my_node"));
            assert(Math.abs(overview.status.workers.get(0).fillPercent - 0.8) < deltaD);
            assert(Math.abs(overview.status.workers.get(0).controlSignal - 1.0) < deltaD);
            
            //unregister
            lbman.deregisterWorker();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        catch(InterruptedException e){

        }

        // call free - this will correctly use the admin token even though instance token
        // is added by reserve call and updated URI inside with LB ID added to it
        try {
            lbman.freeLB();
        } catch (E2sarNativeException e) {
            e.printStackTrace();
            fail();
        }
        uri.free();
        lbman.free();
    }
}
