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


package de.jreality.jogl.pick;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;

public class JOGLPickUtility {

	private JOGLPickUtility() {
	}

	/**
	 * A convenience method
	 * @return
	 */
	public static double[] getPointObject(PickPoint pp, Viewer v) {
		double[] pointObject = null;
		Graphics3D context = new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
		if (pp.pickCoordinateSystem == PickAction.PICK_NDC)  pointObject = Rn.matrixTimesVector(null, context.getNDCToObject(), pp.getPointNDC() );
		else pointObject = Rn.matrixTimesVector(null, context.getWorldToObject(), pp.getPointWorld());
		if (pointObject.length == 4) Pn.dehomogenize(pointObject, pointObject);
		return pointObject;
	}

	/**
	 * A convenience method
	 * @return
	 */
	public double[] getPointWorld(PickPoint pp, Viewer v) {
		if (pp.pickCoordinateSystem == PickAction.PICK_WORLD) return pp.getPointWorld();
		if (pp.getPointNDC() == null)	throw new IllegalStateException("PickPoint should have non-null NDC point");
		Graphics3D context = new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
		double[] pointWorld = Rn.matrixTimesVector(null, context.getNDCToWorld(), pp.getPointNDC() );
		if (pointWorld.length == 4) Pn.dehomogenize(pointWorld, pointWorld);
		return (double[]) pointWorld.clone();
	}

	public static Graphics3D getContext(PickPoint pp, de.jreality.jogl.JOGLViewer v) {
		return new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
	}

}
