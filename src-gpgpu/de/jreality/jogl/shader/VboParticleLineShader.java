package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;

/**
 * @author weissman
 * 
 */
public class VboParticleLineShader extends ParticleLineShader {
  
  public void updateData(JOGLRenderer jr) {
    super.updateData(jr);
    GpgpuViewer gpgpuViewer = (GpgpuViewer)jr.theViewer;
    if (gpgpuViewer.isReadData()) gpgpuViewer.setReadData(false);
  }
  
  public void render(JOGLRenderer jr) {
    if (renderCheap) {
      super.render(jr);
    } else {
      updateData(jr);
      GL gl = jr.globalGL;
      gl.glColor3fv(difCol);
      gl.glPointSize((float) pointSize);
      gl.glPushAttrib(GL.GL_LIGHTING_BIT);
       gl.glDisable(GL.GL_LIGHTING);
       if (sprites) {
         gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation);
         gl.glEnable(GL.GL_POINT_SPRITE_ARB);
         gl.glActiveTexture(GL.GL_TEXTURE0);
         gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
         gl.glEnable(GL.GL_TEXTURE_2D);
         Texture2DLoaderJOGL.render(jr.getCanvas(), tex);
       }
      ((GpgpuViewer)jr.theViewer).renderPoints(jr);
      gl.glPopAttrib();
    }
  }
  public void postRender(JOGLRenderer jr) {
    if (sprites) {
      GL gl = jr.globalGL;
      gl.glDisable(GL.GL_POINT_SPRITE_ARB);
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
      gl.glDisable(GL.GL_TEXTURE_2D);
    }
  }

}
