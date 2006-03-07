package de.jreality.jogl;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.util.LoggingSystem;

/*
 * Author	gunn
 * Created on Apr 7, 2005
 *
 */

/**
 * @author gunn
 *
 */
public class JOGLConfiguration {

	public static Logger theLog;
	static boolean debugGL = false;
	static boolean sharedContexts = false;
	static boolean isLinux = false;
	static boolean multiSample = true;
	static boolean portalUsage = false;
	public static String resourceDir = null, saveResourceDir = null;
	/**
	 * 
	 */
	static JOGLConfiguration sharedInstance = new JOGLConfiguration();
	private JOGLConfiguration() { 
		super(); 
		theLog	= LoggingSystem.getLogger(this);
//	    AccessController.doPrivileged(new PrivilegedAction() {
//	        public Object run() {
//	          if (System.getProperty("os.name").indexOf("Linux") != -1) isLinux = true; else isLinux = false;
//	          return null;
//	        }
//	      });
		try {
			//theLog.setLevel(Level.INFO);
			String foo = System.getProperty("jreality.jogl.debugGL");
			if (foo != null) { if (foo.equals("false")) debugGL = false; else debugGL =true;}
			foo = System.getProperty("jreality.jogl.multisample");
			if (foo != null) 
				if (foo.indexOf("false") != -1) multiSample = false;
			foo = System.getProperty("jreality.jogl.portalUsage");
			if (foo != null) 
				if (foo.indexOf("true") != -1) portalUsage = true;
			foo = System.getProperty("jreality.jogl.loggingLevel");
			if (foo != null)  {
				Level level = Level.INFO;
				if (foo.indexOf("finest") != -1) level = Level.FINEST;
				else if (foo.indexOf("finer") != -1) level = Level.FINER;
				else if (foo.indexOf("fine") != -1) level = Level.FINE;
				else if (foo.indexOf("info") != -1) level = Level.INFO;
				theLog.setLevel(level);
			}
			foo = System.getProperty("os.name");
			if (foo != null && foo.indexOf("Linux") != -1) isLinux = true;
			// allocate a GLCanvas to be the "sharer": it will never be destroyed
//			foo = System.getProperty("jreality.jogl.sharedContexts");
//			if (foo != null && foo.indexOf("true") != -1) sharedContexts = true;
//			if (sharedContexts)	{
//				GLCapabilities capabilities = new GLCapabilities();
//				firstOne = GLDrawableFactory.getFactory().createGLCanvas(capabilities, null, null);	
//				JOGLConfiguration.theLog.log(Level.WARNING,"Not allowing shared contexts now");
//				sharedContexts=false;
//				theLog.log(Level.INFO,"Using shared contexts: "+sharedContexts);
//			}
			foo = System.getProperty("jreality.jogl.resourceDir");
			if (foo != null) saveResourceDir = resourceDir = foo; 			
		} catch(SecurityException se)	{
			theLog.log(Level.WARNING,"Security exception in setting configuration options",se);
		}
	}

	public static JOGLConfiguration sharedInstance()	{
		if (sharedInstance == null) {
			sharedInstance = new JOGLConfiguration();
		}
		return sharedInstance;
	}
	
	public static Logger getLogger()	{
		return theLog;
	}
}
