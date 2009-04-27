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

import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.media.opengl.GL;

import de.jreality.geometry.SphereUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.CameraUtility;

/**
 * @author gunn
 *
 */
public class JOGLSphereHelper extends SphereUtility {

	static boolean sharedDisplayLists = JOGLConfiguration.sharedContexts;
	static WeakHashMap sphereDListsTable = new WeakHashMap();
	static int[] globalSharedSphereDisplayLists = null;
	public static void setupSphereDLists(JOGLRenderer jr)	{
		int[] dlists = null; //getSphereDLists(jr);
//		if (dlists != null)	{
//			JOGLConfiguration.theLog.log(Level.WARNING,"Already have sphere display lists for this renderer "+jr);
//		}
		GL gl = jr.globalGL;
		int n = SphereUtility.tessellatedCubes.length;
		dlists = null;
		//if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
		//else 
		dlists = new int[n];
//		JOGLConfiguration.theLog.log(Level.INFO,"Setting up sphere display lists for context "+gl);
		for (int i = 0; i<n; ++i)	{
			tessellatedCubeSphere(i);
			dlists[i] = gl.glGenLists(1);
//			LoggingSystem.getLogger(JOGLCylinderUtility.class).fine("Allocating new dlist "+dlists[i]);
			gl.glNewList(dlists[i], GL.GL_COMPILE);
			//gl.glDisable(GL.GL_SMOOTH);
			IndexedFaceSet qms = SphereUtility.cubePanels[i];
			for (int j = 0; j<SphereUtility.cubeSyms.length; ++j)	{
				gl.glPushMatrix();
				gl.glMultTransposeMatrixd(SphereUtility.cubeSyms[j].getMatrix(),0);
				JOGLRendererHelper.drawFaces(jr,qms,true, 1.0);
				gl.glPopMatrix();
			}				
			gl.glEndList();
		}
		if (!sharedDisplayLists) sphereDListsTable.put(jr.globalGL, dlists);
		else globalSharedSphereDisplayLists = dlists;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public static int getSphereDLists(int i,JOGLRenderer jr) {
		int[] dlists = getSphereDLists(jr);
		if (dlists == null) 	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Invalid sphere display lists");
			return 0;
		}
		return dlists[i];
	}

	/**
	 * @param i
	 * @return
	 */
	public static int[] getSphereDLists( JOGLRenderer jr) {
		int dlists[];
		if (!sharedDisplayLists)	
			dlists =  (int[] ) sphereDListsTable.get(jr.globalGL);
		else dlists = globalSharedSphereDisplayLists;
		if (dlists == null) 	{
			setupSphereDLists(jr);
			if (!sharedDisplayLists)	
				dlists = (int[] ) sphereDListsTable.get(jr.globalGL);
			else dlists = globalSharedSphereDisplayLists;
		}
		if (dlists == null)	{
			throw new IllegalStateException("Can't make sphere display lists successfully");
		}
		return dlists;
	}

	/**
	 * @param globalGL
	 */
	public static void disposeSphereDLists(JOGLRenderer jr) {
		int[] dlists = getSphereDLists(jr);
		if (dlists == null)	{
			throw new IllegalStateException("No such gl context");
		}
		// probably don't need to actually delete them since the context 
	   sphereDListsTable.clear(); 
	   for(int i = 0; i < tessellatedCubes.length; i++){ 
	      tessellatedCubes[i].setGeometry(null); 
	      tessellatedCubes[i] = null; 
	   } 
	   for(int i = 0; i < cubePanels.length; i++){ 
	      cubePanels[i] = null; 
	   }	
	}


	static double[] lodLevels = {.02,.08,.16,.32,.64};
	public static int getResolutionLevel(double[] o2ndc, double lod) {
		double d = lod * CameraUtility.getNDCExtent(o2ndc);
		//JOGLConfiguration.theLog.log(Level.FINE,"Distance is "+d);
		int i = 0;
		for ( i = 0; i<5; ++i)	{
			if (d < lodLevels[i]) break;
		}
		return i;
	}

}
