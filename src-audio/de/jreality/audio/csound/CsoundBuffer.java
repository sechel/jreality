package de.jreality.audio.csound;

import csnd.MyfltVector;
import csnd.SWIGTYPE_p_float;

/**
 * Silly hack to get access to protected methods in {@link SWIGTYPE_p_float} and {@link MyfltVector}.
 * 
 * @author brinkman
 *
 */
public class CsoundBuffer extends MyfltVector {
	
	private static class FloatPtr extends SWIGTYPE_p_float {
		static long cPtr(SWIGTYPE_p_float ptr) {
			return SWIGTYPE_p_float.getCPtr(ptr);
		}
	}
	
	public CsoundBuffer(SWIGTYPE_p_float ptr) {
		super(FloatPtr.cPtr(ptr), false);
	}
}