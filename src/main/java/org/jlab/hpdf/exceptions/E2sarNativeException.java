package org.jlab.hpdf.exceptions;

/**
 * This exception class is used whenever an error is created in the native JNI wrapper. 
 */
public class E2sarNativeException extends Exception{
    public E2sarNativeException(String msg){
        super(msg);
    }
}