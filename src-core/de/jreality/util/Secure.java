package de.jreality.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class Secure {
	
    public static String getProperty(final String name, final String def) {
    	return AccessController.doPrivileged(new PrivilegedAction<String>() {
    		public String run() {
    			return System.getProperty(name, def);
    		}
    	});
    }

    public static String getProperty(final String name) {
    	return AccessController.doPrivileged(new PrivilegedAction<String>() {
    		public String run() {
    			return System.getProperty(name);
    		}
    	});
    }

	public static String setProperty(final String name, final String value) {
    	return AccessController.doPrivileged(new PrivilegedAction<String>() {
    		public String run() {
    			return System.setProperty(name, value);
    		}
    	});
	}

}
