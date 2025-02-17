package org.jlab.hpdf;

/**
 * This class has some util functions for the native E2SAR library
 */
public class E2sarUtil{

    static{
        System.loadLibrary("jnie2sar");
    }

    /**
     * This is native function that gets the version of the native E2SAR that is linked
     * @return String - E2sarVersion
     */
    public static native String getE2sarVersion();
    
}