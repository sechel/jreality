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


package de.jreality.util;


/**
 * jReality system property keys and default values
 * used in {@link System#getProperty(String, String)}
 * and {@link Secure#getProperty(String, String)}.
 * 
 * @author msommer
 */
public class SystemProperties {

	private SystemProperties() {}
	
	
	/** 
	 * Specifies whether {@link de.jreality.ui.viewerapp.ViewerApp} initializes a {@link RenderTrigger}
	 * for the displayed scene.<br> 
	 * Values: <code>true | false</code>.
	 * @see SystemProperties#SYNCH_RENDER   
	 */
	public final static String AUTO_RENDER = "de.jreality.ui.viewerapp.autoRender";
	public final static String AUTO_RENDER_DEFAULT = "true";
	
	/** 
	 * Specifies whether the {@link RenderTrigger} initialized by {@link de.jreality.ui.viewerapp.ViewerApp}
	 * dispatches synchronous render requests.<br>
	 * Values: <code>true | false</code>.
	 * @see SystemProperties#AUTO_RENDER
	 */
	public final static String SYNCH_RENDER = "de.jreality.ui.viewerapp.synchRender";
	public final static String SYNCH_RENDER_DEFAULT = "true";

	/** 
	 * Specifies the default JrScene used by {@link de.jreality.ui.viewerapp.ViewerApp}.<br>
	 * Values: <code>desktop | portal | portal-remote</code>.
	 */
	public final static String ENVIRONMENT = "de.jreality.viewerapp.env";
	public final static String ENVIRONMENT_DEFAULT = "desktop";
	
	/** 
	 * Specifies the {@link de.jreality.toolsystem.config.ToolSystemConfiguration} to be used by a {@link de.jreality.toolsystem.ToolSystem}.<br>
	 * Values: <code>default | portal | portal-remote | desfault+portal</code>.
	 * @see SystemProperties#TOOL_CONFIG_FILE 
	 */
	public final static String TOOL_CONFIG = "de.jreality.scene.tool.Config";
	public final static String TOOL_CONFIG_DEFAULT = "default";
	
	/**
	 * Specifies the {@link de.jreality.toolsystem.config.ToolSystemConfiguration} to be used by a {@link de.jreality.toolsystem.ToolSystem}.<br>
	 * Value: file name of a tool system cofiguration xml-file
	 * @see SystemProperties#TOOL_CONFIG
	 */
	public final static String TOOL_CONFIG_FILE = "jreality.toolconfig";
	
	/**
	 * Specifies the viewer(s) to be initialized by {@link de.jreality.ui.viewerapp.ViewerApp}.<br>
	 * Values: class names of {@link de.jreality.scene.Viewer} implementations separated by space character. 
	 */
	public final static String VIEWER = de.jreality.scene.Viewer.class.getName();
	public final static String VIEWER_DEFAULT_JOGL = "de.jreality.jogl.Viewer";  //de.jreality.jogl.Viewer.class.getName();
	public final static String VIEWER_DEFAULT_SOFT = "de.jreality.softviewer.SoftViewer";  //de.jreality.softviewer.SoftViewer.class.getName();
	
	/**
	 * Specifies the path of the jReality data directory.
	 */
	public final static String JREALITY_DATA = "jreality.data";
	
	
	
//	NO DEFAULTS:
//	
//	de.jreality.soft.Polygon: "jreality.soft.maxpolyvertex" - int
//	de.jreality.soft.NewPolygonRasterizer: "jreality.soft.imager" - hatch | toon
//	
//	CHARLES:
//	"jreality.jogl.debugGL" - boolean
//	"jreality.jogl.portalUsage" - boolean
//	"jreality.jogl.loggingLevel" - public finals of java.util.logging.Level
//	"jreality.jogl.quadBufferedStereo" - boolean
//	"discreteGroup.copycat" - boolean
//	"jreality.jogl.resourceDir" - directory path
//	-------------------------------
	
	
//	WITH DEFAULTS:
//	
//	"jreality.config" - ?, def: "jreality.props"
//	"de.jreality.ui.viewerapp.SelectionManagerInterface" - class, def: "de.jreality.ui.viewerapp.SelectionManager"
//	"de.jreality.portal.HeadTrackedViewer" - viewer interface class, def: "de.jreality.jogl.Viewer"
//	-------------------------------
	
//	JAVA
//	os.name - string
//  user.dir - directory path, def: "/"	
//	
}
