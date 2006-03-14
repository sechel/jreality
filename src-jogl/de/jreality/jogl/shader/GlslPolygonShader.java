package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Sphere;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;

/**
 * it is assumed that the shader source code stayes FIXED!
 * 
 * @author gollwas
 *
 */
public class GlslPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

  GlslProgram program;
  
  public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
	super.setFromEffectiveAppearance(eap, name);
   program = new GlslProgram(eap, name);
  }

  public void render(JOGLRenderingState jrs) {
	  JOGLRenderer jr = jrs.getRenderer();
    GlslLoader.render(program, jr.getCanvas());
	boolean useDisplayLists = jrs.isUseDisplayLists();
	Geometry g = jrs.getCurrentGeometry();
	if (g != null)	{
		if (g instanceof Sphere || g instanceof Cylinder)	{	
			int i = 3;
			if (jr.debugGL)	{
				double lod = jr.openGLState.levelOfDetail;
				i = JOGLSphereHelper.getResolutionLevel(jr.context.getObjectToNDC(), lod);
			}
			int dlist;
			if (g instanceof Sphere) dlist = jr.openGLState.getSphereDisplayLists(i);
			else 			 dlist = jr.openGLState.getCylinderDisplayLists(i);
			if (jr.pickMode) jr.globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
			jr.globalGL.glCallList(dlist);
			if (jr.pickMode) jr.globalGL.glPopName();
		}
		else if ( g instanceof IndexedFaceSet)	{
					jr.helper.drawFaces((IndexedFaceSet) g,true,1.0);			
		}
	}
  }

  public void postRender(JOGLRenderingState jrs) {
	  JOGLRenderer jr = jrs.getRenderer();
    GlslLoader.postRender(program, jr.getCanvas());
  }

public void setFrontBack(int f) {
  }

public void setProgram(GlslProgram program) {
	this.program = program;
}


}
