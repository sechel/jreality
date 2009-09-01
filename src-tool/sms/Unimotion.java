//Unimotion.java
//Unimotion

// Created by Daniel Shiffman

package sms;

/**
 * This class gives access to the SMS sensor of Mac Hardware. This class will move to a suitable package as soon
 * as we have recompiled the corresponding jni lib...
 * 
 * @author weissman
 *
 */
public class Unimotion {

    // Load the JNI Interface
    static {    	
        System.loadLibrary("UnimotionLib");
    }

    // Native function
    private static native int[] readSMS();

    // Return three values as array
    public static int[] getSMSArray() {
        return readSMS(); 
    }
    
}

