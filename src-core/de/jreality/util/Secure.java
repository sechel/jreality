package de.jreality.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class Secure {
	
    public static String getProperty(final String name, final String def) {
    	String result = null;
        try{
            result = AccessController.doPrivileged(new PrivilegedAction<String>() {           
                public String run() {
                    return System.getProperty(name, def);
                }
            });
        } catch (Throwable t) {
            result = def;
        }
        return result;
    }

    public static String getProperty(final String name) {
        String result = null;
        try {
    	result =  AccessController.doPrivileged(new PrivilegedAction<String>() {
    		public String run() {
    			return System.getProperty(name);
    		}
    	});
        } catch (Throwable t) {
            
        }
        return result;
    }

	public static String setProperty(final String name, final String value) {
    	return AccessController.doPrivileged(new PrivilegedAction<String>() {
    		public String run() {
    			return System.setProperty(name, value);
    		}
    	});
	}

}
