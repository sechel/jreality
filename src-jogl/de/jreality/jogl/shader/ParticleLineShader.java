package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/** 
 * @author weissman
 *
 */
public class ParticleLineShader implements LineShader {
  
  private double pointRadius;
  private Color diffuseColor;
  private static float[] difCol=new float[4];
  
  static private float[] data;

  static float[] vortexData;
  
  static float[] mat = new float[16];
  static {
    mat[15] = 1;
  }
  
  boolean debug;
  
  static IndexedLineSet ils;
  static float[] particles=new float[0];
  static boolean setParticles;
  
  static double dt;
  static float velocityFator;
  
  public boolean providesProxyGeometry() {
    return true;
  }

  public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig, boolean useDisplayLists) {
    ils = (IndexedLineSet) original;
    return -1;
  }

  public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
    pointRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
    debug = eap.getAttribute(ShaderUtility.nameSpace(name, "debug"), false);
    diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR),CommonAttributes.DIFFUSE_COLOR_DEFAULT);
    diffuseColor.getComponents(difCol);
    dt = eap.getAttribute(ShaderUtility.nameSpace(name, "dt"), 0.001);
    velocityFator = (float) eap.getAttribute(ShaderUtility.nameSpace(name, "velocityFator"), 0.1);
    float[] parts = (float[]) eap.getAttribute(ShaderUtility.nameSpace(name,"particles"), particles);
    if (parts != particles) {
      particles = parts;
      setParticles = true;
    }
  }

  public void updateData(JOGLRenderer jr) {
    GpgpuViewer v = (GpgpuViewer) jr.theViewer;
    if (setParticles) {
      v.setParticles(particles);
      setParticles=false;
    }
    data = (float[]) v.getCurrentParticlePositions(data).clone();
    if (ils != null) {
      float[] d = (float[]) ils.getGeometryAttributes(Attribute.attributeForName("rungeKuttaData"));
      if (d != null && d != vortexData) {
        vortexData = d;
        v.setVortexData(d);
      }
    }
  }

  public void render(JOGLRenderer jr) {
    if (debug) {
      System.out.println("ParticleLineShader.render()");
      GpgpuViewer.dumpData(data);
    }
    updateData(jr);
    if (particles.length == 0) return;
    GL gl =   jr.globalGL;
    
      int n = data.length/4;

      int dlist = JOGLSphereHelper.getSphereDLists(0, jr);

      gl.glColor4fv(difCol);
      
      mat[0] = mat[5] = mat[10] = (float) pointRadius;
      int nanCnt=0;
      for (int i = 0; i< n; i++) {
        if (Float.isNaN(data[4*i]) || Float.isNaN(data[4*i+1]) || Float.isNaN(data[4*i]+2)) {
          nanCnt++;
          continue;
        }
        mat[3] = data[4*i];
        mat[7] = data[4*i+1];
        mat[11] = data[4*i+2];
        
        gl.glPushMatrix();
        gl.glMultTransposeMatrixf(mat);

        gl.glCallList(dlist);
        gl.glPopMatrix();
      }
      if (nanCnt > 0) System.out.println("nanCnt="+nanCnt);
    }

  public void postRender(JOGLRenderer jr) {
  }
  
}
