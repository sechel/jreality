/*
 * Author	gunn
 * Created on Dec 9, 2005
 *
 */
package de.jreality.shader;

import de.jreality.scene.data.AttributeEntity;

public interface RenderingHintsShader extends AttributeEntity {

	  Object CREATE_DEFAULT=new Object();
	  final static boolean LIGHTING_ENABLED_DEFAULT = true;			// do lighting or not
	  final static boolean TRANSPARENCY_ENABLED_DEFAULT = false;		// do transparency or not
	  final static boolean Z_BUFFER_ENABLED_DEFAULT = true;			// can help to switch this in transparency mode
	  final static boolean DIFFUSE_COLOR_ONLY_DEFAULT = false;		// optimize appearance rendering 
	  final static boolean IGNORE_ALPHA0_DEFAULT = true;				// pseudo-transparency available in OpenGL
	  final static boolean ANY_DISPLAY_LISTS_DEFAULT = true;			// use display lists at all?
	  final static boolean MANY_DISPLAY_LISTS_DEFAULT = true;			// if so, one display list per instance?
	  final static boolean BACK_FACE_CULLING_DEFAULT = false;			// if so, one display list per instance?
	final static double LEVEL_OF_DETAIL_DEFAULT = 			1.0;
	final static double DEPTH_FUDGE_FACTOR_DEFAULT = 1.0;

	  Boolean getLightingEnabled();
	  void setLightingEnabled(Boolean b);

	  Boolean getTransparencyEnabled();
	  void setTransparencyEnabled(Boolean d);

	  Boolean getZBufferEnabled();
	  void setZBufferEnabled(Boolean d);

	  Boolean getDiffuseColorOnly();
	  void setDiffuseColorOnly(Boolean d);

	  Boolean getIgnoreAlpha0();
	  void setIgnoreAlpha0(Boolean d);

	  Boolean getAnyDisplayLists();
	  void setAnyDisplayLists(Boolean d);

	  Boolean getManyDisplayLists();
	  void setManyDisplayLists(Boolean d);

	  Boolean getBackFaceCulling();
	  void setBackFaceCulling(Boolean d);

	  Double getLevelOfDetail();
	  void setLevelOfDetail(Double d);

	  Double getDepthFudgeFactor();
	  void setDepthFudgeFactor(Double d);

}
