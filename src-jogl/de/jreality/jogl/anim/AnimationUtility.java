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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.jogl.anim;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.scene.Camera;


/**
 * @author gunn
 *
 */
public class AnimationUtility {

	private AnimationUtility()	{
		super();
	}
	
	public static FactoredMatrix linearInterpolation(FactoredMatrix dst, FactoredMatrix t1, FactoredMatrix t2, double s ) {
		if (dst == null) dst = new FactoredMatrix(t1.getSignature());
		int sig = t1.getSignature();
		if (sig != t2.getSignature() || sig != dst.getSignature())	{
			throw new IllegalArgumentException("Differing signatures ");
		}
		
		double[] trans1 = t1.getTranslation();
		double[] trans2 = t2.getTranslation();
		double[] trans = Pn.linearInterpolation(null,trans1, trans2, s, sig);
		
//		double[] center1 = t1.getCenter();
//		double[] center2 = t2.getCenter();
//		double[] center = null;
//		if (center1 != null && center2 != null)
//			center = Pn.linearInterpolation(null,center1, center2, s, sig);
//		
		double[] stretch1 = t1.getStretch();
		double[] stretch2 = t2.getStretch();
		double[] stretch = Pn.linearInterpolation(null,stretch1, stretch2, s, Pn.EUCLIDEAN);
		
		Quaternion rot1 = t1.getRotationQuaternion();
		Quaternion rot2 = t2.getRotationQuaternion();
		Quaternion rot = Quaternion.linearInterpolation(null, rot1, rot2, s);

		dst.setTranslation(trans);
//		if (center != null) dst.setCenter(center);
		dst.setStretch(stretch);
		dst.setRotation(rot);
		return dst;
	}
	
	//	 form the FactoredMatrix so that when s = 0, dst = t1 and when s=1,  dst = t2
	public static Camera linearInterpolation(Camera dst, Camera t1, Camera t2, double s ) {
		return dst;
	}

}
