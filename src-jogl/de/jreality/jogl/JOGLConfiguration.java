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

import de.jreality.util.LoggingSystem;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

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
	//static boolean debugGL = true;
	static boolean debugGL = false;
	static boolean sharedContexts = false;
	static boolean isLinux = false;
	static boolean multiSample = true;
	static boolean portalUsage = false;
	public static String resourceDir = null, saveResourceDir = null;
	public static String localScratchDisk = null;
	public static boolean quadBufferedStereo = false;
	public static String COPY_CAT = "copyCat", FORCE_RENDER = "preRender";
	static JOGLConfiguration ss = new JOGLConfiguration();
	static Class<? extends GoBetween> goBetweenClass = null;
	static Class<? extends JOGLPeerComponent> peerClass = null;
	
	@SuppressWarnings("unchecked")
	private JOGLConfiguration() { 
		super(); 
		theLog	= LoggingSystem.getLogger(this);
	    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		try {
		    System.setProperty("sun.awt.noerasebackground", "true");
			//theLog.setLevel(Level.INFO);
			String foo = Secure.getProperty(SystemProperties.JOGL_DEBUG_GL);
			if (foo != null) { if (foo.equals("false")) debugGL = false; else debugGL =true;}
			foo = Secure.getProperty(SystemProperties.JOGL_PORTAL_USAGE);
			if (foo != null) 
				if (foo.indexOf("true") != -1) portalUsage = true;
			foo = Secure.getProperty(SystemProperties.JOGL_LOGGING_LEVEL);
			if (foo != null)  {
				Level level = Level.INFO;
				if (foo.indexOf("finest") != -1) level = Level.FINEST;
				else if (foo.indexOf("finer") != -1) level = Level.FINER;
				else if (foo.indexOf("fine") != -1) level = Level.FINE;
				else if (foo.indexOf("info") != -1) level = Level.INFO;
				theLog.setLevel(level);
			}
			foo = Secure.getProperty("os.name");
			if (foo != null && foo.indexOf("Linux") != -1) isLinux = true;
			// allocate a GLCanvas to be the "sharer": it will never be destroyed
//			foo = Secure.getProperty("jreality.jogl.sharedContexts");  //TODO: move to de.jreality.util.SystemProperties
//			if (foo != null && foo.indexOf("true") != -1) sharedContexts = true;
//			if (sharedContexts)	{
//				GLCapabilities capabilities = new GLCapabilities();
//				firstOne = GLDrawableFactory.getFactory().createGLCanvas(capabilities, null, null);	
//				JOGLConfiguration.theLog.log(Level.WARNING,"Not allowing shared contexts now");
//				sharedContexts=false;
//				theLog.log(Level.INFO,"Using shared contexts: "+sharedContexts);
//			}
			foo = Secure.getProperty(SystemProperties.JOGL_RESOURCE_DIR);
			if (foo != null) saveResourceDir = resourceDir = foo;
			quadBufferedStereo = "true".equals(Secure.getProperty(SystemProperties.JOGL_QUAD_BUFFERED_STEREO));
			if (quadBufferedStereo) {
				// hack, otherwise one side of swing gui will not be drawn
				// only for windows
				Secure.setProperty("sun.java2d.noddraw", "true");
			}
			// this doesn't really belong here but it's important that it gets evaluated
			// before jogl backend classes begin to be instantiated, and this is the best place to guarantee that.
			boolean copycat = "true".equals(Secure.getProperty(SystemProperties.JOGL_COPY_CAT));
			if (copycat)
				try {
					peerClass = (Class<? extends JOGLPeerComponent>) Class.forName("de.jreality.jogl.DiscreteGroupJOGLPeerComponent");
					ConstructPeerGraphVisitor.setPeerClass(peerClass);
					System.err.println("Got peer class "+peerClass);
					goBetweenClass = (Class<? extends GoBetween>) Class.forName("de.jreality.jogl.DiscreteGroupGoBetween");
					JOGLRenderer.setGoBetweenClass(goBetweenClass);
					System.err.println("Got go betwen class "+goBetweenClass);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		} catch(SecurityException se)	{
			theLog.log(Level.WARNING,"Security exception in setting configuration options",se);
		}
//		catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	public static Logger getLogger()	{
		return theLog;
	}

	public static Class<? extends JOGLPeerComponent> getPeerClass() {
		System.err.println("JOGLConfiguation 2: peer class is "+peerClass);
		return peerClass;
	}

	public static Class<? extends GoBetween> getGoBetweenClass() {
		return goBetweenClass;
	}

}
