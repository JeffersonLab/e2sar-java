package org.jlab.hpdf;

import java.net.InetSocketAddress;

import org.jlab.hpdf.exceptions.E2sarNativeException;

/**
 * This class is a wrapper for EjfatURI present in the native cpp library. It creates an instance of e2sar::EjfatURI and the pointer to this object 
 * is stored in nativeEjfatUri field
 * The URI is of the format:
 * ejfat[s]://[&lt;token&gt;@]&lt;cp_host&gt;:&lt;cp_port&gt;/lb/&lt;lb_id&gt;[?[data=&lt;data_host&gt;[:&lt;data_port&gt;]][&amp;sync=&lt;sync_host&gt;:&lt;sync_port&gt;]][&amp;sessionid=&lt;string&gt;].
 * More than one data= address can be specified (typically an IPv4 and IPv6). For data
 * the port is optional and defaults to 19522, however for testing/debugging can be overridden
 * 
 * REMEMBER TO CALL FREE() ONCE DONE WITH THIS CLASS TO FREE THE NATIVE INSTANCE CREATED. MEMORY LEAKS WILL HAPPEN IF FREE IS NOT CALLED
 */
public class EjfatURI{

    static{
        System.loadLibrary("jnie2sar");
    }

    /**
     * Represents the privelege of the Token created.
     */
    public enum Token{
        /**
         * ADMIN privelege
         */
        ADMIN,
        /**
         * INSTANCE privelege
         */
        INSTANCE,
        /**
         * SESSION privelege
         */
        SESSION
    }
    
    private long nativeEjfatURI;

    /**
     * This function is meant to be used with other internal classes like LbManager.java. 
     * The native e2sar::LBManager has its own copy of the EjfatURI which may be useful to obtain. 
     * @param nativeEjfatURI - the native pointer obtained from internal EjfatURI object 
     */
    protected EjfatURI(long nativeEjfatURI){
        this.nativeEjfatURI = nativeEjfatURI;
    }

    /**
     * Constructor to initialise EjfatURI
     * @param uri - the URI string 
     * @param token - convert to this token type (admin, instance, session)
     * @param preferv6 - when connecting to the control plane, prefer IPv6 address 
     * if the name resolves to both (defaults to v4)
     * @throws E2sarNativeException - If the URI could not be parsed the E2sarNativeException will be thrown by the native code
     */
    public EjfatURI(String uri, Token token, boolean preferv6)throws E2sarNativeException{
        nativeEjfatURI = initEjfatUri(uri, token.ordinal(), preferv6);
    }

    /**
     * Constructor to initialise EjfatURI with default admin privelge and preferv6=false
     * @param uri - the URI string
     * @throws E2sarNativeException - If the URI could not be parsed the E2sarNativeException will be thrown by the native code
     */
    public EjfatURI(String uri) throws E2sarNativeException{
        this(uri, Token.ADMIN, false);
    }

    /**
     * Creates an EjfatURI instance by parsing ejfatURI from environment variable
     * @param envVariable - the environment variable that has the EJFAT_URI string
     * @param token - convert to this token type (admin, instance, session)
     * @param preferv6 - when connecting to the control plane, prefer IPv6 address 
     * if the name resolves to both (defaults to v4)
     * @return EjfatURI instance
     * @throws E2sarNativeException - If the environment variable does not exist or if the URI could not be parsed
     */
    public static EjfatURI getFromEnv(String envVariable, Token token, boolean preferv6) throws E2sarNativeException{
        String uri = System.getenv(envVariable);
        if(uri == null || uri.isEmpty()){
            throw new E2sarNativeException("Environment variable : " + envVariable + " is not set");
        }
        return new EjfatURI(uri,token,preferv6);
    }

    /**
     * Creates an EjfatURI instance by parsing ejfatURI from file
     * @param envVariable - the environment variable that has the EJFAT_URI string
     * @param token - convert to this token type (admin, instance, session)
     * @param preferv6 - when connecting to the control plane, prefer IPv6 address 
     * if the name resolves to both (defaults to v4)
     * @return EjfatURI instance
     * @throws E2sarNativeException - If the environment variable does not exist or if the URI could not be parsed
     */
    /**
     * Creates an EjfatURI instance by parsing ejfatURI from file
     * @param fileName - file containing URI string
     * @param token - convert to this token type (admin, instance, session)
     * @param preferv6 - when connecting to the control plane, prefer IPv6 address 
     * if the name resolves to both (defaults to v4)
     * @return EjfatURI instance
     * @throws E2sarNativeException - If the URI could not be parsed the E2sarNativeException will be thrown by the native code
     */
    public static EjfatURI getFromFile(String fileName, Token token, boolean preferv6) throws E2sarNativeException{
        return new EjfatURI(getUriFromFile(fileName, token.ordinal(), preferv6));
    }

    private static native long initEjfatUri(String envVariable, int token, boolean preferv6);

    private static native long getUriFromFile(String fileName, int t, boolean preferv6) throws E2sarNativeException;

    /**
     * Check if TLS should be used
     * @return true/false
     */
    public boolean getUseTls(){return this.getUseTls(nativeEjfatURI);}
    private native boolean getUseTls(long nativeEjfatURI);

    /**
     * Set instance token of internal e2sar::EjfatURI instance
     * @param t - the related instance Token
     */
    public void setInstanceToken(String t){setInstanceToken(nativeEjfatURI, t);}
    private native void setInstanceToken(long nativeEjfatURI, String t);

    /**
     * Set session token of internal e2sar::EjfatURI instance
     * @param t - the related session Token
     */
    public void setSessionToken(String t){setInstanceToken(nativeEjfatURI, t);}
    private native void setSessionToken(long nativeEjfatURI, String t);

    /**
     * get the Internal Instance Token
     * @return - Instance Token
     * @throws E2sarNativeException - If Instance token is not available throws this exception
     */
    public String getInstanceToken() throws E2sarNativeException{return getInstanceToken(nativeEjfatURI);}
    private native String getInstanceToken(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * get the Internal Session Token
     * @return - Session Token
     * @throws E2sarNativeException - If Session token is not available throws this exception
     */
    public String getSessionToken() throws E2sarNativeException{return getSessionToken(nativeEjfatURI);}
    private native String getSessionToken(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * get the Internal admin Token
     * @return - Admin Token
     * @throws E2sarNativeException - If Admin token is not available throws this exception
     */
    public String getAdminToken() throws E2sarNativeException{return getAdminToken(nativeEjfatURI);}
    private native String getAdminToken(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Set LB name
     * @param lbName - Name of the LB
     */
    public void setLbName(String lbName){setLbName(nativeEjfatURI, lbName);}
    private native void setLbName(long nativeEjfatURI, String lbName);

    /**
     * Set LB id
     * @param lbid - ID of LB
     */
    public void setLbid(String lbid){setLbid(nativeEjfatURI, lbid);}
    private native void setLbid(long nativeEjfatURI, String lbid);

    /**
     * Set Session id 
     * @param sessionId - sessionId
     */
    public void setSessionId(String sessionId){setSessionId(nativeEjfatURI, sessionId);}
    private native void setSessionId(long nativeEjfatURI, String sessionId);

    /**
     * Set the SyncAddres and port, could be ipv4 or ipv6 
     * @param socketAddress - ip:port of sync address of LB
     */
    public void setSyncAddr(InetSocketAddress socketAddress){setSyncAddr(nativeEjfatURI, socketAddress);}
    private native void setSyncAddr(long nativeEjfatURI, InetSocketAddress socketAddress);

    /**
     * Set the Dataplane address and port, could be ipv4 or ipv6 
     * @param socketAddress - ip:port of dataplane address
     */
    public void setDataAddr(InetSocketAddress socketAddress){setDataAddr(nativeEjfatURI, socketAddress);}
    private native void setDataAddr(long nativeEjfatURI, InetSocketAddress socketAddress);

    /**
     * Get name associated with LB
     * @return LB Name
     */
    public String getLbName(){return getLbName(nativeEjfatURI);}
    private native String getLbName(long nativeEjfatURI);

    /**
     * Get ID associated with LB
     * @return LB ID
     */
    public String getLbid(){return getLbid(nativeEjfatURI);}
    private native String getLbid(long nativeEjfatURI);

    /**
     * Get Session ID associated with LB
     * @return Session ID
     */
    public String getSessionId(){return getSessionId(nativeEjfatURI);}
    private native String getSessionId(long nativeEjfatURI);

    /**
     * Get control plane ip address and port
     * @return ip:port of control plane
     * @throws E2sarNativeException - if InetSocketAddress could not be created
     */
    public InetSocketAddress getCpAddr() throws E2sarNativeException{return getCpAddr(nativeEjfatURI);}
    private native InetSocketAddress getCpAddr(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Get control plane hostname and port
     * @return hostname:port of control plane
     * @throws E2sarNativeException - if InetSocketAddress could not be created
     */
    public InetSocketAddress getCpHost() throws E2sarNativeException {return getCpHost(nativeEjfatURI);}
    private native InetSocketAddress getCpHost(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Does the URI contain a v4 dataplane address?
     * @return true/false
     */
    public boolean hasDataAddrv4(){return hasDataAddrv4(nativeEjfatURI);}
    private native boolean hasDataAddrv4(long nativeEjfatURI);

    /**
     * Does the URI contain a v6 dataplane address?
     * @return true/false
     */
    public boolean hasDataAddrv6(){return hasDataAddrv6(nativeEjfatURI);}
    private native boolean hasDataAddrv6(long nativeEjfatURI);

    /**
     * Does the URI contain any dataplane address?
     * @return true/false
     */
    public boolean hasDataAddr(){return hasDataAddr(nativeEjfatURI);}
    private native boolean hasDataAddr(long nativeEjfatURI);

    /**
     * Does the URI contain a sync address?
     * @return true/false
     */
    public boolean hasSyncAddr(){return hasSyncAddr(nativeEjfatURI);}
    private native boolean hasSyncAddr(long nativeEjfatURI);

    /**
     * Get data plane v4 address and port
     * @return ip:port of data plane
     * @throws E2sarNativeException - If it does not exist or could not be parsed into InetSocketAddress
     */
    public InetSocketAddress getDataAddrv4() throws E2sarNativeException{return getDataAddrv4(nativeEjfatURI);}
    private native InetSocketAddress getDataAddrv4(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Get data plane v6 address and port
     * @return ip:port of data plane
     * @throws E2sarNativeException - If it does not exist or could not be parsed into InetSocketAddress
     */
    public InetSocketAddress getDataAddrv6() throws E2sarNativeException{return getDataAddrv6(nativeEjfatURI);}
    private native InetSocketAddress getDataAddrv6(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Get sync address and port
     * @return ip:port of sync
     * @throws E2sarNativeException - If it does not exist or could not be parsed into InetSocketAddress
     */
    public InetSocketAddress getSyncAddr() throws E2sarNativeException{return getSyncAddr(nativeEjfatURI);}
    private native InetSocketAddress getSyncAddr(long nativeEjfatURI) throws E2sarNativeException;

    /**
     * Get the EJFAT_URI string
     * @param t - Privelege type
     * @return the uri as a String
     */
    public String toString(Token t){
        return toString(nativeEjfatURI, t.ordinal());
    }

    /**
     * Get the EJFAT_URI string with default ADMIN privlege
     * @return the uri as a String
     */
    @Override
    public String toString(){
        return toString(EjfatURI.Token.ADMIN);
    }

    private native String toString(long nativeEjfatURI, int t);

    private native void freeNativePointer(long nativeEjfatURI);

    /**
     * Method to free the native instance. If called multiple time it will be a noop
     */
    public void free(){
        if(nativeEjfatURI != 0){
            freeNativePointer(nativeEjfatURI);
            nativeEjfatURI = 0;
        }
    }



}