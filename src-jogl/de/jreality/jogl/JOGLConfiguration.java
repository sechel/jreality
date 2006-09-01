/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.jogl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;

import de.jreality.scene.data.Attribute;
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
	public static boolean testMatrices = false;
	static boolean portalUsage = false;
	public static String resourceDir = null, saveResourceDir = null;
	public static String localScratchDisk = null;
	public static boolean quadBufferedStereo = false;
	 public static String COPY_CAT, PRE_RENDER;
	 static {
	  	COPY_CAT = "copyCat";
	  	PRE_RENDER= "preRender";	  
	 }
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
	    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		try {
		    System.setProperty("sun.awt.noerasebackground", "true");
			//theLog.setLevel(Level.INFO);
			String foo = System.getProperty("jreality.jogl.debugGL");
			if (foo != null) { if (foo.equals("false")) debugGL = false; else debugGL =true;}
			foo = System.getProperty("jreality.jogl.testMatrices");
			if (foo != null) 
				if (foo.indexOf("false") != -1) testMatrices = false;
				else testMatrices = true;
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
			quadBufferedStereo = "true".equals(System.getProperty("jreality.jogl.quadBufferedStereo"));
			if (quadBufferedStereo) {
				// hack, otherwise one side of swing gui will not be drawn
				// only for windows
				System.setProperty("sun.java2d.noddraw", "true");
			}
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
