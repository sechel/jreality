/*
 * Created on Aug 17, 2004
 *
 */
package de.jreality.jogl.anim;

import de.jreality.scene.Camera;
import de.jreality.scene.Transformation;
import de.jreality.util.FactoredMatrix;
import de.jreality.util.Pn;
import de.jreality.util.Quaternion;

/**
 * @author gunn
 *
 */
public class AnimationUtility {

	private AnimationUtility()	{
		super();
	}
	
	// form the transformation so that when s = 0, dst = t1 and when s=1,  dst = t2
	public static Transformation linearInterpolation(Transformation dst, Transformation t1, Transformation t2, double s ) {
		if (dst == null) dst = new Transformation();
		int sig = t1.getSignature();
		if (sig != t2.getSignature())	{
			// TODO signal error
			t2.setSignature(sig);
		}
		dst.setSignature(sig);
		
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
	
	//	 form the transformation so that when s = 0, dst = t1 and when s=1,  dst = t2
	public static Camera linearInterpolation(Camera dst, Camera t1, Camera t2, double s ) {
		return dst;
	}

}
