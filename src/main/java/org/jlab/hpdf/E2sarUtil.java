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

    /**
     * Static call to get e2sar::TOTAL_HDR_LENGTH form e2sarHeaders.hpp
     * @return total header length
     */
    public static native long getTotalHeaderLength();
    
}