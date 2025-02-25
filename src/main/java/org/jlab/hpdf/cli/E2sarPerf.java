package org.jlab.hpdf.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.hpdf.E2sarUtil;
import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.Reassembler;
import org.jlab.hpdf.Segmenter;
import org.jlab.hpdf.config.ReassemblerFlags;
import org.jlab.hpdf.config.SegmenterFlags;
import org.jlab.hpdf.LbManager;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.SendStats;
import org.jlab.hpdf.messages.LostEvent;
import org.jlab.hpdf.messages.ReassembledEvent;
import org.jlab.hpdf.messages.RecvStats;

public class E2sarPerf {
    

    // // prepare a pool
    // boost::pool<> *evtBufferPool;
    // // to avoid locking the pool we use the return queue
    // boost::lockfree::queue<u_int8_t*> returnBufferQueue{10000};
    // app-level stats

    static AtomicLong mangledEvents;
    static AtomicLong receivedWithError;

    // event payload
    static byte[] eventPldStart;
    static byte[] eventPldEnd;

    static boolean threadsRunning;
    static int reportThreadSleepMs;
    static Reassembler reas;
    static Segmenter seg;
    static LbManager lbman;
    static List<String> senders;

    static{
        mangledEvents = new AtomicLong();
        receivedWithError = new AtomicLong();
        eventPldStart = "Start".getBytes(Charset.forName("UTF-8"));
        eventPldEnd = "End".getBytes(Charset.forName("UTF-8"));
        threadsRunning = true;
        reportThreadSleepMs = 1000;
        reas = null;
        seg = null;
        lbman = null;
        senders = new ArrayList<>();
    }
     /**
     * Function to check that opt1 and opt2 are not sepcified
     */

    private static void shutdownHook(){
        Thread shutdownHook = new Thread(() -> {
            System.out.println("Shutdown hook executed. Performing cleanup...");
            E2sarPerf.threadsRunning = false;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
            if(E2sarPerf.seg != null){
                if(E2sarPerf.lbman != null){
                    System.out.println("Removing senders: ");
                    for(String s : senders){
                        System.out.print(s + " ");
                    }
                    System.out.println();
                    try {
                        lbman.removeSenders(senders);
                    } catch (E2sarNativeException e) {
                        System.out.println("Unable to remove sender from list on exit: " + e.getMessage());
                    }
                }
                seg.free();
            }
            if(E2sarPerf.reas != null){
                System.out.println("Deregistering worker");
                try {
                    reas.deregisterWorker();
                } catch (Exception e) {
                    System.out.println("Unable to deregister worker on exit: " + e.getMessage());
                }
                reas.free();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook); 
    }

    private static void conflictingOptions(CommandLine cmd, String opt1, String opt2)throws IllegalArgumentException{
        if(cmd.hasOption(opt1) && cmd.hasOption(opt2)){
            throw new IllegalArgumentException("Conflicting options '" + opt1 + "' and '" + opt2 + "'.");
        }
    }
    
    /**
     * Function used to check that of 'for_what' is specified, then 'required_option' is specified too. throws an exception
     */
    private static void optionDependency(CommandLine cmd, final String forWhat, final String requiredOption) throws IllegalArgumentException{
        if(cmd.hasOption(forWhat) && !cmd.hasOption(requiredOption))
            throw new IllegalArgumentException("Option '" + forWhat + "' requires option '" + requiredOption + "'.");
    }


    
    private static ByteBuffer createByteBuffer(int size, boolean isDirect){
        ByteBuffer buffer;
        if(isDirect){
            buffer = ByteBuffer.allocateDirect(size);
        }
        else{
            buffer = ByteBuffer.allocate(size);
        }

        buffer.position(0);
        buffer.put(eventPldStart);
        buffer.position(buffer.capacity() - (eventPldEnd.length + 1));
        buffer.put(eventPldEnd);
        buffer.flip();
        return buffer;
    }

    private static void sendEvents(long startEventNum, long numEvents, 
        long eventBufSize, float rateGbps, boolean isDirect){
        // convert bit rate to event rate
        float eventRate = rateGbps*1000000000/(eventBufSize*8);
        long interEventSleepUsec = (long)(eventBufSize*8/(rateGbps * 1000));

        System.out.println("Sending bit rate is " + rateGbps + " Gbps");
        System.out.println("Event size is " + eventBufSize + " bytes or " + eventBufSize*8 + " bits");
        System.out.println("Event rate is " + eventRate + " Hz");
        System.out.println("Inter-event sleep time is " + interEventSleepUsec + " microseconds");
        System.out.println("Sending " + numEvents + " event buffers");
        int mtu = seg.getMTU();
        System.out.println("Using MTU " + mtu);

        long expectedFrames = (long) (numEvents * Math.ceil((eventBufSize * 1.0)/(mtu - E2sarUtil.getTotalHeaderLength())));

        if(seg.getMaxPayloadLength() < (eventPldStart.length + eventPldEnd.length)){
            System.out.println("MTU is too short to send needed payload");
            return;
        }

        try {
            seg.openAndStart();
        } catch (E2sarNativeException e) {
            System.out.println("Could not open segmenter sockets: " + e.getMessage());
            return;
        }

        for(long evt = 0; evt < numEvents; evt++){
            // Get the current time point in microseconds
            long nowT = System.nanoTime() / 1000;

            // send the event
            ByteBuffer event = createByteBuffer((int)eventBufSize, isDirect);
            long until = nowT + interEventSleepUsec;
            if(nowT > until){
                System.out.println("Clock overrun, either event buffer length too short or requested sending rate too high");
                return;
            }
            try {
                if(isDirect){
                    seg.addToSendQueueDirect(event, evt, 0, 0);
                }
                else{
                    seg.addToSendQueue(event.array(), evt, 0, 0);
                }
            } catch (E2sarNativeException e) {
                System.out.println("Error adding item to send queue: " + e.getMessage());
            }
            
            
            while((System.nanoTime()/1000) < until){
                LockSupport.parkNanos(1_000);//sleep for 1 microsecond and yield thread. Will cause deadlocks if this is not here
                //Since microsecond sleep is not available in java this is the current option
            }
        }
        
        SendStats sendStats = seg.getSendStats();
        if(expectedFrames > sendStats.eventDatagramCount){
            System.out.println("WARNING: Fewer frames than expected have been sent (" + sendStats.eventDatagramCount + " of " + 
            expectedFrames + "), sender is not keeping up with the requested send rate.");
        }

        System.out.println("Completed, " + sendStats.eventDatagramCount + " frames sent, " + sendStats.eventDatagramErrCount + " errors");
    }

    private static void prepareToReceive(String hostname){

        System.out.print("Receiving on ports: ");
        for(int port : reas.getRecvPorts()){
            System.out.print(port + " ");
        }
        System.out.println();

        try {
            reas.registerWorker(hostname);
        } catch (E2sarNativeException e) {
            System.out.println("Unable to register worker node due to " + e.getMessage());
            System.exit(-1);
        }
        System.out.println("Registered the worker");

        try {
            reas.openAndStart();
        } catch (E2sarNativeException e) {
            System.out.println("Could not open recv sockets: " + e.getMessage());
            System.exit(-1);
        }
    }

    private static void recvEvents(int duration){
        long nowT = System.currentTimeMillis() / 1000;
        long nextT;
        while(threadsRunning){
            Optional<ReassembledEvent> rEvent;
            try{
                rEvent = reas.recvEvent(1000);
                nextT = System.currentTimeMillis() / 1000;
            }
            catch(E2sarNativeException e){
                receivedWithError.incrementAndGet();
                continue;
            }
            if((duration != 0) && (nextT - nowT > duration))  
                break;
            if(rEvent.isEmpty()){
                continue;
            }
            else{
                ReassembledEvent event = rEvent.get();
                byte[] startArray = new byte[eventPldStart.length];
                event.byteBuffer.get(startArray, 0, eventPldStart.length);
                byte[] endArray = new byte[eventPldEnd.length];
                event.byteBuffer.get(endArray, event.byteBuffer.capacity() - (eventPldEnd.length + 1), eventPldEnd.length);

                if(!Arrays.equals(startArray, eventPldStart) ||
                    !Arrays.equals(endArray, eventPldEnd)){
                        mangledEvents.incrementAndGet();
                    }
                reas.freeDirectBytebBuffer(event);
            }
        }
        System.out.println("Completed");
    }

    private static void recvStatsThread(){
        List<LostEvent> lostEvents = new ArrayList<>();
        Thread thread = new Thread(() -> {
            while(E2sarPerf.threadsRunning){
                RecvStats recvStats = reas.getStats();
                while(true){
                    Optional<LostEvent> lOptional = reas.getLostEvent();
                    if(lOptional.isPresent())
                        lostEvents.add(lOptional.get());
                    else
                        break;
                }
                System.out.println("Stats:");
                System.out.println("\tEvents Received: " + recvStats.eventSuccess);
                System.out.println("\tEvents Mangled: " + mangledEvents);
                System.out.println("\tEvents Lost: " + recvStats.enqueueLoss);
                System.out.println("\tData Errors: " + recvStats.dataErrCount);
                if(recvStats.dataErrCount > 0){
                    System.out.println("\t Last Data Error: " + recvStats.lastErrorNo);
                }
                System.out.println("\tgRPC errors: " + recvStats.grpcErrCount);
                if(recvStats.lastE2sarError != 0){
                    System.out.println("\tLast E2SARError code: " + recvStats.lastE2sarError);
                }

                try {
                    Thread.sleep(reportThreadSleepMs);
                } catch (InterruptedException e) {
                }
            }
        });
        thread.start();
    }

    public static void main(String args[]){


        Options options = new Options();

        options.addOption("h", "help", false, "Show this help message");
        options.addOption("s", "send", false,"send traffic");
        options.addOption("r", "recv", false,"receive traffic");

        options.addOption("l", "length", true, "event buffer length (defaults to 1024^2) [s]");
        options.addOption("u", "uri", true, "specify EJFAT_URI on the command-line instead of the environment variable");
        options.addOption("n", "num", true, "number of event buffers to send (defaults to 10) [s]");
        options.addOption("e", "enum", true, "starting event number (defaults to 0) [s]");
        options.addOption("m", "mtu", true, "MTU (default 1500) [s]");
        options.addOption(null, "src", true, "Event source (default 1234) [s]");
        options.addOption(null, "dataid", true, "Data id (default 4321) [s]");
        options.addOption(null, "threads", true, "number of receive threads (defaults to 1) [r]");
        options.addOption(null, "sockets", true, "number of receive threads (defaults to 1) [r]");
        options.addOption(null, "rate", true, "send rate in Gbps (defaults to 1.0)");
        options.addOption("p", "period", true, "receive side reporting thread sleep period in ms (defaults to 1000) [r]");
        options.addOption("b", "bufsize", true, "send or receive socket buffer size (default to 3MB)");
        options.addOption("d", "duration", true, "duration for receiver to run for (defaults to 0 - until Ctrl-C is pressed)[s]");
        options.addOption("c", "withcp", true, "enable control plane interactions");
        options.addOption("i", "ini", true, "INI file to initialize SegmenterFlags [s]] or ReassemblerFlags [r].\n" +
                        " Values found in the file override --withcp, --mtu and --bufsize");
        options.addOption(null, "ip",  true, "IP address (IPv4 or IPv6) from which sender sends from or on which receiver listens. Defaults to 127.0.0.1. [s,r]");
        options.addOption(null, "port", true, "Starting UDP port number on which receiver listens. Defaults to 10000. [r] ");
        options.addOption("6", "ipv6", false, "force using IPv6 control plane address if URI specifies hostname (disables cert validation) [s,r]");
        options.addOption("4", "ipv4", false, "force using IPv4 control plane address if URI specifies hostname (disables cert validation) [s,r]");
        options.addOption("v", "novalidate", false, "don't validate server certificate");
        options.addOption("z", "zerorate", false, "report zero event number change rate in Sync messages [s]");
        options.addOption(null, "seq", false, "use sequential numbers as event numbers in Sync and LB messages instead of usec [s]");
        // options.addOption(null, "deq", true, "number of dequeue read threads in receiver (defaults to 1) [r]");
        options.addOption(null, "cores", true, "optional list of cores to bind receiver threads to; number of threads is equal to the number of cores [r]");
        options.addOption(null, "indirect", false, "use byte[] to send data instead of Direct ByteBuffer [s]");
        options.addOption(null, "hostname", true, "hostname of reassembler. Default is 'E2sarPerfRecevr'[r]");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            conflictingOptions(cmd, "send", "recv");
            conflictingOptions(cmd, "recv", "num");
            conflictingOptions(cmd, "recv", "enum");
            conflictingOptions(cmd, "recv", "length");
            conflictingOptions(cmd, "recv", "mtu");
            conflictingOptions(cmd, "recv", "src");
            conflictingOptions(cmd, "recv", "dataid");
            conflictingOptions(cmd, "recv", "rate");
            conflictingOptions(cmd, "recv", "rate");
            conflictingOptions(cmd, "ipv4", "ipv6");
            conflictingOptions(cmd, "send", "threads");
            conflictingOptions(cmd, "send", "period");

            conflictingOptions(cmd, "send", "duration");
            conflictingOptions(cmd, "send", "port");
            conflictingOptions(cmd, "deq", "send");
            conflictingOptions(cmd, "seq", "recv");
            conflictingOptions(cmd, "cores", "send");
            conflictingOptions(cmd, "cores", "threads");
        } 
        catch(ParseException e){
            System.out.println("Unrecognized Option use -h for available options" + e.getMessage());
            return;
        }
        catch(IllegalArgumentException e){
            System.out.println("Wrong combination of options" + e.getMessage());
            return;
        }

        System.out.println("E2SAR version: " + E2sarUtil.getE2sarVersion());
        
        if (cmd.hasOption("help") || cmd.getOptions().length == 0){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2sar", options);
            return;
        }

        
        long numEvents = Long.parseLong(cmd.getOptionValue("num", "10"));
        long startingEventNum = Long.parseLong(cmd.getOptionValue("enum", "0"));
        long eventBufferSize = cmd.hasOption("length") ? Long.parseLong(cmd.getOptionValue("length")) : 1024*1024;
        long eventSourceId = Integer.parseInt(cmd.getOptionValue("src", "1234"));
        
        int mtu = Integer.parseInt(cmd.getOptionValue("mtu", "1500"));
        int dataId = Integer.parseInt(cmd.getOptionValue("dataid", "4321"));
        int numThreads = Integer.parseInt(cmd.getOptionValue("threads", "1"));
        int numSockets = Integer.parseInt(cmd.getOptionValue("sockets", "1"));
        int readThreads = Integer.parseInt(cmd.getOptionValue("deq", "1"));
        int sockBufSize = cmd.hasOption("bufsize")? Integer.parseInt(cmd.getOptionValue("bufsize")) : 1024*1024*3;
        int durationSec = Integer.parseInt(cmd.getOptionValue("duration", "0"));
        int recvStartPort = Integer.parseInt(cmd.getOptionValue("port", "10000"));
        
        float rateGbps = Float.parseFloat(cmd.getOptionValue("rate", "1.0"));
        
        String sndrcvIP = cmd.getOptionValue("ip", "127.0.0.1");
        String iniFile = cmd.getOptionValue("ini", "");
        String hostname = cmd.getOptionValue("hostname", "E2sarPerfRecevr");

        List<Integer> coreList = new ArrayList<>() ;
        if(cmd.hasOption("cores")){
            for(String core : cmd.getOptionValues("cores")){
                coreList.add(Integer.parseInt(core));
            }
        }

        boolean withCP = cmd.hasOption("withcp") ? true : false;
        boolean zeroRate = cmd.hasOption("zerorate") ? true : false;
        boolean usecAsEventNum = cmd.hasOption("seq") ? false: true;
        boolean preferV6 = cmd.hasOption("ipv6") ? true : false;
        boolean preferHostAddr = (cmd.hasOption("ipv6") || cmd.hasOption("ipv4")) ? true : false;
        boolean validate = cmd.hasOption("novalidate") ? false: true;
        boolean isDirect = cmd.hasOption("indirect") ? false : true;

        EjfatURI.Token tt = EjfatURI.Token.INSTANCE;

        if(cmd.hasOption("send") || cmd.hasOption("recv")){
            EjfatURI uri;
            try{
                if(cmd.hasOption("uri")){
                    uri = new EjfatURI(cmd.getOptionValue("uri"), tt, preferV6);       
                }
                else{
                    uri = EjfatURI.getFromEnv("EJFAT_URI", tt, preferV6);
                }
            }
            catch(E2sarNativeException e){
                System.out.println("EJFAT_URI env variable not set" + e.getMessage());
                return;
            }

            if(cmd.hasOption("send")){
                if(withCP){
                    senders.add(sndrcvIP);
                    try { 
                        lbman = new LbManager(uri, validate, preferV6);       
                    } catch (E2sarNativeException e) {
                        System.out.println("Could not init LbManager(): " + e.getMessage());
                        System.exit(-1);
                    }
                    System.out.print("Adding senders to LB: ");
                    for(String s : senders){
                        System.out.print(s);
                    }
                    System.out.println();
                    try {
                        lbman.addSenders(senders);
                    } catch (E2sarNativeException e) {
                        System.out.println("Unable to add senders: " + e.getMessage());
                        System.exit(-1);
                    }
                }
                if(iniFile.length() > 0){
                    try {
                        System.out.println("Loading SegmenterFlags from " + iniFile);
                        seg = new Segmenter(uri, dataId, eventSourceId, iniFile);
                    } catch (E2sarNativeException e) {
                        System.out.println("Unable to init segmenter: " + e.getMessage());
                        System.exit(-1);
                    }
                }
                else{
                    SegmenterFlags sFlags = new SegmenterFlags();  
                    sFlags.useCP = withCP; 
                    sFlags.mtu = mtu;
                    sFlags.sndSocketBufSize = sockBufSize;
                    sFlags.numSendSockets = numSockets;
                    sFlags.zeroRate = zeroRate;
                    sFlags.usecAsEventNum = usecAsEventNum;
                    try {
                        seg = new Segmenter(uri, dataId, eventSourceId, sFlags);
                    } catch (E2sarNativeException e) {
                        System.out.println("Unable to init segmenter: " + e.getMessage());
                        System.exit(-1);
                    }
                    System.out.println("Control plane                " + (sFlags.useCP ? "ON" : "OFF") );
                    System.out.println("Event rate reporting in Sync " + (sFlags.zeroRate ? "OFF" : "ON"));
                    System.out.println("Using usecs as event numbers " + (sFlags.usecAsEventNum ? "ON" : "OFF") );
                    if(sFlags.useCP)
                        System.out.println("*** Make sure the LB has been reserved and the URI reflects the reserved instance information.");
                    else
                        System.out.println("*** Make sure the URI reflects proper data address, other parts are ignored.");
                    
                    sendEvents(startingEventNum, numEvents, eventBufferSize, rateGbps, isDirect);
                }
            }
            else if(cmd.hasOption("recv")){
                if(iniFile.length() > 0){
                    System.out.println("Loading ReassemblerFlags from " + iniFile);
                    try {
                        InetAddress ipAddress = InetAddress.getByName(sndrcvIP);
                        if(cmd.hasOption("cores"))
                            reas = new Reassembler(uri, ipAddress, recvStartPort, coreList, iniFile);
                        else
                            reas = new Reassembler(uri, ipAddress, recvStartPort, numThreads, iniFile);
                    } catch (E2sarNativeException e) {
                        System.out.println("Could not init Reassembler: " + e.getMessage());
                        System.exit(-1);
                    }
                    catch (UnknownHostException e){
                    }
                }
                else{
                    ReassemblerFlags rFlags = new ReassemblerFlags();
                    rFlags.useCP = withCP;
                    rFlags.withLBHeader = !withCP;
                    rFlags.rcvSocketBufSize = sockBufSize;
                    rFlags.useHostAddress = preferHostAddr;
                    rFlags.validateCert = validate;
                    try {
                        InetAddress ipAddress = InetAddress.getByName(sndrcvIP);
                        if(cmd.hasOption("cores"))
                            reas = new Reassembler(uri, ipAddress, recvStartPort, coreList, rFlags);
                        else
                            reas = new Reassembler(uri, ipAddress, recvStartPort, numThreads, rFlags);
                    } catch (E2sarNativeException e) {
                        System.out.println("Could not init Reassembler: " + e.getMessage());
                        System.exit(-1);
                    }
                    catch (UnknownHostException e){
                    }
                    System.out.println("Control plane                " + (rFlags.useCP ? "ON" : "OFF") );
                    System.out.println("Using " + (cmd.hasOption("cores") ? "Assigned Threads To Cores" : "Unassigned Threads"));
                    System.out.println("Will run " + (durationSec != 0 ? "for " + durationSec + " sec" : "until Ctrl-C"));
                    if(rFlags.useCP)
                        System.out.println("*** Make sure the LB has been reserved and the URI reflects the reserved instance information.");
                    else
                        System.out.println("*** Make sure the URI reflects proper data address, other parts are ignored.");

                    prepareToReceive(hostname);
                    recvStatsThread();
                    recvEvents(durationSec);
                }
            }
        }
        else{
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2sar", options);
            return;
        }
    }
}
