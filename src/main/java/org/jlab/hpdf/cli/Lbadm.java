package org.jlab.hpdf.cli;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jlab.hpdf.E2sarUtil;
import org.jlab.hpdf.EjfatURI;
import org.jlab.hpdf.LbManager;
import org.jlab.hpdf.exceptions.E2sarNativeException;
import org.jlab.hpdf.messages.LBOverview;
import org.jlab.hpdf.messages.LBStatus;
import org.jlab.hpdf.messages.WorkerStatus;



public class Lbadm {

    /**
     * Function to check that opt1 and opt2 are not sepcified
     */
    private static void conflictingOptions(CommandLine cmd, String opt1, String opt2){
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

    public static void reserveLB(LbManager lbman, String lbName, List<String> senders, String duration, boolean suppress){
        int fpgaLbid;
        if(!suppress){
            System.out.println("Reserving a new load balancer");
            System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.ADMIN) + " using address: " + lbman.getAddrString());
            System.out.println("   LB Name: " + lbName);
            System.out.println("   Allowed senders: ");
            for(String sender : senders){
                System.out.println(sender);
            }
            System.out.println("   Duration: " + duration);
        }
        try{
            fpgaLbid = lbman.reserveLB(lbName, duration, senders);
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP, error:"  + e.getMessage());
            return;
        }
        if(!suppress){
            System.out.println("Success. FPGA ID is (for metrics): " + fpgaLbid);
            System.out.println("Updated URI after reserve with instance token: " + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE));
        }
        else{
            System.out.println("export EJFAT_URI='" + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE) + "'");
        }
    }

    public static void freeLB(LbManager lbman, String lbid){
        System.out.println("Freeing a load balancer ");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.ADMIN) + " using address: " + lbman.getAddrString());
        System.out.println("   LB ID: " + (lbid.isEmpty() ? lbman.getEjfatURI().getLbid() : lbid));

        try{
            if(lbid.isEmpty())
                lbman.freeLB();
            else
                lbman.freeLB(lbid);

        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
            return;
        }
        System.out.println("Success");
    }

    public static void registerWorker(LbManager lbman, String nodeName, 
        String nodeIp, int nodePort, float weight, 
        int srcCount, float minFactor, float maxFactor, boolean suppress){
        if(!suppress){
            System.out.println("Registering a worker ");
            System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE) + " using address: " + lbman.getAddrString());
            System.out.println("   Worker details: " + nodeName + " at " + nodeIp + ":" + nodePort);
            System.out.println("   CP parameters: " + "w=" + weight + ",  source_count=" + srcCount);
        }

        try{
            lbman.registerWorker(nodeName, nodeIp, nodePort, weight, srcCount, minFactor, maxFactor);
            if(!suppress){
                System.out.println("Success.");
                System.out.println("Updated URI after register with session token: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION));
                System.out.println("Session id is: " + lbman.getEjfatURI().getSessionId());
            }
            else{
                System.out.println("export EJFAT_URI='" + lbman.getEjfatURI().toString(EjfatURI.Token.INSTANCE) + "'");
            }
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void deregisterWorker(LbManager lbman){
        System.out.println("De-Registering a worker");
        System.out.println("  Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + "using address: " + lbman.getAddrString());
        try{
            lbman.deregisterWorker();
            System.out.println("Success");
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void getLBStatus(LbManager lbman, String lbid){
        System.out.println("Getting LB Status ");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + " using address: " + lbman.getAddrString());
        System.out.println("   LB ID: " + (lbid.isEmpty() ? lbman.getEjfatURI().getLbid() : lbid));

        try{
            LBStatus lbStatus = lbman.getStatus(lbid);

            System.out.println("Registered sender addresses:");
            for(String s : lbStatus.senderAddresses)
                System.out.println(s);
            
            System.out.println("Registered workers: ");
            for(WorkerStatus wStatus : lbStatus.workers){
                System.out.println("[ name=" + wStatus.name + ", controlsignal=" + wStatus.controlSignal + ", fillpercent=" + wStatus.fillPercent 
                + ", slotsassigned=" + wStatus.slotsAssigned + ", lastupdated=" + wStatus.lastUpdated.toString() + "] ");
            }
            String expiresAt = lbStatus.expiresAt != null ? lbStatus.expiresAt.toString() : "This did not work";
            System.out.println("LB details: expiresat=" + expiresAt + ", currentepoch=" + lbStatus.currentEpoch + ", predictedeventnum="
            + lbStatus.currentPredictedEventNumber);
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void getOverview(LbManager lbman){
        System.out.println("Getting Overview");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + " using address: " + lbman.getAddrString());

        try{
            List<LBOverview> overviewList = lbman.getOverview();
            for(LBOverview overview : overviewList){
                System.out.println("LB" + overview.name + " ID: " + overview.lbid + " FPGA LBID: " + overview.fpgaLbid);
                System.out.println("  Registered sender addresses: ");
                for(String senderAddress : overview.status.senderAddresses){
                    System.out.println(senderAddress);
                }

                System.out.println("  Registered workers: ");
                for(WorkerStatus wStatus : overview.status.workers){
                    System.out.println("[ name=" + wStatus.name + ", controlsignal=" + wStatus.controlSignal + ", fillpercent=" + wStatus.fillPercent 
                    + ", slotsassigned=" + wStatus.slotsAssigned + ", lastupdated=" + wStatus.lastUpdated.toString() + "] ");
                }
                System.out.println("LB details: expiresat=" + overview.status.expiresAt.toString() + ", currentepoch=" + overview.status.currentEpoch + ", predictedeventnum="
                + overview.status.currentPredictedEventNumber);
            }
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void sendState(LbManager lbman, float fillPercent, float ctrlSignal, boolean isReady){
        System.out.println("Sending Worker State ");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + " using address: " + lbman.getAddrString());
        System.out.println("   LB Name: " + (lbman.getEjfatURI().getLbName().isEmpty() ? "not set" : lbman.getEjfatURI().getLbName()));

        try{
            lbman.sendState(fillPercent, ctrlSignal, isReady);
            System.out.println("Success.");
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
        
    }

    public static void removeSenders(LbManager lbman, List<String> senders){
        System.out.println("Removing senders to CP ");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + " using address: " + lbman.getAddrString());
        System.out.println("   LB Name: " + (lbman.getEjfatURI().getLbName().isEmpty() ? "not set" : lbman.getEjfatURI().getLbName()));
        System.out.println("   Sender list: ");
        for(String s : senders)
            System.out.println(s);

        try{
            lbman.removeSenders(senders);
            System.out.println("Success.");
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void addSenders(LbManager lbman, List<String> senders){
        System.out.println("Adding senders to CP ");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.SESSION) + " using address: " + lbman.getAddrString());
        System.out.println("   LB Name: " + (lbman.getEjfatURI().getLbName().isEmpty() ? "not set" : lbman.getEjfatURI().getLbName()));
        System.out.println("   Sender list: ");
        for(String s : senders)
            System.out.println(s);

        try{
            lbman.addSenders(senders);
            System.out.println("Success.");
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void getVersion(LbManager lbman){
        System.out.println("Getting load balancer version");
        System.out.println("   Contacting: " + lbman.getEjfatURI().toString(EjfatURI.Token.ADMIN) + " using address: " + lbman.getAddrString());

        try{
            List<String> versionList = lbman.version();
            System.out.println("Success.");
            System.out.println("Reported version: ");
            System.out.println("Commit: " + versionList.get(0));
            System.out.println("Build: " + versionList.get(1));
            System.out.println("CompatTag: " + versionList.get(2));
        }
        catch(E2sarNativeException e){
            System.out.println("unable to connect to Load Balancer CP" + e.getMessage());
        }
    }

    public static void main(String args[]){

        Options options = new Options();
        options.addOption("h", "help", false, "Show this help message");
        options.addOption("l", "lbname", true, "specify name of the load balancer");
        options.addOption("i", "lbid", true, "override/provide id of the loadbalancer");
        options.addOption("a", "address", true, "node IPv4/IPv6 address, can be used multiple times for 'reserve'");
        options.addOption("d", "duration", true, "specify duration as '[hh[:mm[:ss]]]'");//default value 02:00:00
        options.addOption("u", "uri", true, "specify EJFAT_URI on the command-line instead of the environment variable");
        options.addOption("n", "name", true, "specify node name for registration");
        options.addOption("p", "port", true, "node starting listening port number");
        options.addOption("w", "weight", true, "node weight");//default value 0.1
        options.addOption("c", "count", true, "node source count");//default value 1
        options.addOption("s", "session", true, "override/provide session id");
        options.addOption("q", "queue", true, "queue fill");//default value 0.0
        options.addOption("t", "ctrl", true, "control signal value");//default value 0.0
        options.addOption("r", "ready", true, "worker ready state (true or false)");//default value true
        options.addOption("v", "novalidate", false, "don't validate server certificate (conflicts with 'root')");
        options.addOption(null, "minfactor", true, "node min factor, multiplied with the number of slots that would be assigned evenly to determine min number of slots for example, 4 nodes with a minFactor of 0.5 = (512 slots / 4) * 0.5 = min 64 slots");
        //default value 0.5
        options.addOption(null, "maxfactor", true, "multiplied with the number of slots that would be assigned evenly to determine max number of slots for example, 4 nodes with a maxFactor of 2 = (512 slots / 4) * 2 = max 256 slots set to 0 to specify no maximum");
        //default value 2.0
        options.addOption("6", "ipv6", false, "force using IPv6 control plane address if URI specifies hostname (disables cert validation)");
        options.addOption("4", "ipv4", false, "force using IPv4 control plane address if URI specifies hostname (disables cert validation)");
        options.addOption("e", "export", false, "suppresses other messages and prints out 'export EJFAT_URI=<the new uri>' returned by the LB");
        
        //commands
        
        options.addOption(null, "reserve", false, "reserve a load balancer (-l, -a, -d required). Uses admin token.");
        options.addOption(null, "free", false, "free a load balancer. Uses instance or admin token.");
        options.addOption(null, "version", false, "report the version of the LB. Uses admin or instance token.");
        options.addOption(null, "register", false, "register a worker (-n, -a, -p, -w, -c required), note you must use 'state' within 10 seconds or worker is deregistered. Uses instance or admin token.");
        options.addOption(null, "deregister", false, "deregister worker. Uses instance or session token.");
        options.addOption(null, "status", false, "get and print LB status. Uses admin or instance token.");
        options.addOption(null, "state", false, "send worker state update (must be done within 10 sec of registration) (-q, -c, -r required). Uses session token.");
        options.addOption(null, "overview", false, "return metadata and status information on all registered load balancers. Uses admin token.");
        options.addOption(null, "addsenders", false, "add 'safe' sender IP addresses to CP (one or more -a required). Uses instance token.");
        options.addOption(null, "removesenders", false, "remove 'safe' sender IP addresses from CP (one or more -a required). Uses instance token.");
        
        String[] commands = {"reserve", "free", "version", "register", "deregister", "status", "state", "overview", "addsenders", "removesenders"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try{
            cmd = parser.parse(options, args);
            optionDependency(cmd, "reserve", "lbname");
            // optionDependency(cmd, "reserve", "duration");
            optionDependency(cmd, "reserve", "address");
            optionDependency(cmd, "register", "name");
            optionDependency(cmd, "register", "address");
            optionDependency(cmd, "register", "port");
            // optionDependency(cmd, "register", "weight");
            // optionDependency(cmd, "register", "count");
            // optionDependency(cmd, "register", "minfactor");
            // optionDependency(cmd, "register", "maxfactor");
            // optionDependency(cmd, "state", "queue");
            // optionDependency(cmd, "state", "ctrl");   
            // optionDependency(cmd, "state", "ready");
            optionDependency(cmd,"addsenders", "address");
            optionDependency(cmd,"removesenders", "address");
            conflictingOptions(cmd, "root", "novalidate");
            conflictingOptions(cmd, "ipv4", "ipv6");
            for(int i = 0; i < commands.length - 1; i++){
                for(int j = i+1; j < commands.length; j++){
                    conflictingOptions(cmd, commands[i], commands[j]);
                }
            }
        }
        catch(ParseException e){
            System.out.println("Unrecognized Option use -h for available options" + e.getMessage());
            return;
        }
        catch(IllegalArgumentException e){
            System.out.println("Wrong combination of options" + e.getMessage());
            return;
        }
        boolean suppress = false;
        if(cmd.hasOption("export")){
            suppress = true;
        }

        if(!suppress){
            System.out.println("E2SAR Version: " + E2sarUtil.getE2sarVersion());
            System.out.println("E2SAR Version: " + E2sarUtil.getE2sarVersion());
        }

        if (cmd.hasOption("help") || cmd.getOptions().length == 0){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2sar", options);
            return;
        }

        // make sure the token is interpreted as the correct type, depending on the call
        EjfatURI.Token tt = EjfatURI.Token.ADMIN;
        if (cmd.hasOption("reserve") || cmd.hasOption("free") || cmd.hasOption("status") || cmd.hasOption("version")) 
            tt = EjfatURI.Token.ADMIN;
        else if (cmd.hasOption("register") || cmd.hasOption("addsenders") || cmd.hasOption("removesenders")) 
            tt = EjfatURI.Token.INSTANCE;
        else if (cmd.hasOption("deregister") || cmd.hasOption("state"))
            tt = EjfatURI.Token.SESSION;

        boolean preferV6 = false;
        if (cmd.hasOption("ipv6"))
        {
            preferV6 = true;
        }

        // if ipv4 or ipv6 requested explicitly
        boolean preferHostAddr = false;
        if (cmd.hasOption("ipv6") || cmd.hasOption("ipv4"))
            preferHostAddr = true;
        
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

        // remember to override session if provided
        if (cmd.hasOption("session")) 
            uri.setSessionId(cmd.getOptionValue("session"));

        // remember to override lbid if provided
        if (cmd.hasOption("lbid")) 
            uri.setLbid(cmd.getOptionValue("lbid"));

        LbManager lbManager;
        try{
            lbManager = new LbManager(uri, true, preferHostAddr);
            if (cmd.hasOption("root") && !uri.getUseTls()){
                System.out.println("Root certificate passed in, but URL doesn't require TLS/SSL, ignoring");
            }
            else{
                if (cmd.hasOption("root")) {
                    String rootFile = cmd.getOptionValue("root");
                    String[] sslCredOpts = new String[3];
                    sslCredOpts[0] = rootFile;
                    lbManager = new LbManager(uri, true, preferHostAddr, sslCredOpts, true);
                }
                else{
                    if (cmd.hasOption("novalidate")){
                        System.out.println("Skipping server certificate validation");
                        lbManager = new LbManager(uri, false, preferHostAddr);
                    }
                }
            }
        }
        catch(E2sarNativeException e){
            System.out.println("Unable to initialize LbManager" + e.getMessage());
            return;
        }
        

        if(cmd.hasOption("reserve")){
            String lbname = cmd.getOptionValue("lbname", "");
            ArrayList<String> senders = new ArrayList<String>(Arrays.asList(cmd.getOptionValues("address")));
            String duration = cmd.getOptionValue("duration","02:00:00");
            reserveLB(lbManager, lbname, senders, duration, suppress);
        }
        else if(cmd.hasOption("free")){
            String lbid = cmd.getOptionValue("lbid", "");
            freeLB(lbManager, lbid);
        }
        else if(cmd.hasOption("version")){
            getVersion(lbManager);
        }
        else if(cmd.hasOption("register")){
            String lbName = cmd.getOptionValue("name");
            String address = cmd.getOptionValue("address");
            int port = Integer.parseInt(cmd.getOptionValue("port"));
            float weight = Float.parseFloat(cmd.getOptionValue("weight", "1.0"));
            int count = Integer.parseInt(cmd.getOptionValue("count", "1"));
            float minFactor = Float.parseFloat(cmd.getOptionValue("minfactor", "0.5"));
            float maxFactor = Float.parseFloat(cmd.getOptionValue("maxfactor", "2.0" ));
            registerWorker(lbManager, lbName, address, port, weight, count, minFactor, maxFactor, suppress);
        }
        else if(cmd.hasOption("deregister")){
            deregisterWorker(lbManager);
        }
        else if(cmd.hasOption("status")){
            String lbid = cmd.getOptionValue("lbid", "");
            getLBStatus(lbManager, lbid);
        }
        else if(cmd.hasOption("state")){
            float queue = Float.parseFloat(cmd.getOptionValue("queue", "0.0"));
            float ctrl = Float.parseFloat(cmd.getOptionValue("ctrl", "0.0"));
            boolean ready = Boolean.parseBoolean(cmd.getOptionValue("ready", "true"));

            sendState(lbManager, queue, ctrl, ready);
        }
        else if(cmd.hasOption("overview")){
            getOverview(lbManager);
        }
        else if(cmd.hasOption("addSenders")){
            ArrayList<String> senders = new ArrayList<String>(Arrays.asList(cmd.getOptionValues("address")));
            addSenders(lbManager, senders);
        }
        else if(cmd.hasOption("removesenders")){
            ArrayList<String> senders = new ArrayList<String>(Arrays.asList(cmd.getOptionValues("address")));
            removeSenders(lbManager, senders);
        }
        else{
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2sar", options);
        }

    }
}
