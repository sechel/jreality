package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.scene.Geometry;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Rectangle3D;

/**
 * @author weissman
 * 
 */
public class ParticleLineShader implements LineShader {

  private double pointRadius;

  private int sphereDetail = 0;

  private Color diffuseColor;

  private static float[] difCol = new float[4];

  static private float[] data = new float[0];

  static float[] mat = new float[16];
  static {
    mat[15] = 1;
  }

  private boolean renderCheap;
  private double pointSize=1;
  private boolean forthOrder;
  
  boolean debug;

  static float[] particles = new float[0];

  static boolean setParticles;

  static float[] vortexData = new float[0];

  static boolean setVortexData;

  static double ro = 0.01;

  static boolean setRo = true;
  
  private static Geometry ils;
  private static Rectangle3D bb=new Rectangle3D();
  
  public boolean providesProxyGeometry() {
    return true;
  }

  public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig,
      boolean useDisplayLists) {
    ils=original;
    if (original.getGeometryAttributes(GeometryUtility.BOUNDING_BOX) != bb)
      original.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, bb);
    return -1;
  }

  public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
    pointRadius = eap.getAttribute(ShaderUtility.nameSpace(name,
        CommonAttributes.POINT_RADIUS), CommonAttributes.POINT_RADIUS_DEFAULT);
    sphereDetail = eap.getAttribute(ShaderUtility.nameSpace(name,
        "sphereDetail"), 0);
    double curRo = eap.getAttribute(ShaderUtility.nameSpace(name, "ro"), ro);
    pointSize = eap.getAttribute(ShaderUtility.nameSpace(name, "pointSize"), pointSize);
    debug = eap.getAttribute(ShaderUtility.nameSpace(name, "debug"), false);
    forthOrder = eap.getAttribute(ShaderUtility.nameSpace(name, "forthOrder"), forthOrder);
    renderCheap = eap.getAttribute(
        ShaderUtility.nameSpace(name, "renderCheap"), false);
    diffuseColor = (Color) eap
        .getAttribute(ShaderUtility.nameSpace(name,
            CommonAttributes.DIFFUSE_COLOR),
            CommonAttributes.DIFFUSE_COLOR_DEFAULT);
    diffuseColor.getComponents(difCol);
    float[] parts = (float[]) eap.getAttribute(ShaderUtility.nameSpace(name,
        "particles"), particles);
    if (parts != particles) {
      particles = parts;
      setParticles = true;
    }
    float[] rkData = (float[]) eap.getAttribute(ShaderUtility.nameSpace(name,
        "rungeKuttaData"), vortexData);
    if (rkData != vortexData) {
      vortexData = rkData;
      setVortexData = true;
    }
    if (curRo != ro) {
      ro = curRo;
      setRo = true;
    }
  }

  public void updateData(JOGLRenderer jr) {
    GpgpuViewer v = (GpgpuViewer) jr.theViewer;
    if (v.isForthOrder() != forthOrder) v.setForthOrder(forthOrder);
    if (setParticles) {
      v.setParticles(particles);
      setParticles = false;
    }
    if (setVortexData) {
      v.setVortexData(vortexData);
      setVortexData = false;
    }
    if (setRo) {
      v.setRo(ro);
      setRo = false;
    }
    data = v.getCurrentParticlePositions(data);
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
    int nanCnt=0;

    gl.glColor4fv(difCol);
    
    float[][] bounds=new float[2][3];
    
    bounds[0][0]=Float.MAX_VALUE;
    bounds[0][1]=Float.MAX_VALUE;
    bounds[0][2]=Float.MAX_VALUE;
    bounds[1][0]=Float.MIN_VALUE;
    bounds[1][1]=Float.MIN_VALUE;
    bounds[1][2]=Float.MIN_VALUE;
    
    if (!renderCheap) {
      int dlist = JOGLSphereHelper.getSphereDLists(sphereDetail, jr);
      mat[0] = mat[5] = mat[10] = (float) pointRadius;
      for (int i = 0; i< n; i++) {
        if (Float.isNaN(data[4*i]) || Float.isNaN(data[4*i+1]) || Float.isNaN(data[4*i]+2)) {
          nanCnt++;
          continue;
        }
        mat[3] = data[4*i];
        mat[7] = data[4*i+1];
        mat[11] = data[4*i+2];
        
        bounds[0][0]=Math.min(bounds[0][0], data[4*i]);
        bounds[0][1]=Math.min(bounds[0][1], data[4*i+1]);
        bounds[0][2]=Math.min(bounds[0][2], data[4*i+2]);
        bounds[1][0]=Math.max(bounds[1][0], data[4*i]);
        bounds[1][1]=Math.max(bounds[1][1], data[4*i+1]);
        bounds[1][2]=Math.max(bounds[1][2], data[4*i+2]);
        
        gl.glPushMatrix();
        gl.glMultTransposeMatrixf(mat);
  
        gl.glCallList(dlist);
        gl.glPopMatrix();
      }
    } else {
      gl.glDisable(GL.GL_LIGHTING);
      gl.glBegin(GL.GL_POINTS);
      gl.glPointSize((float) pointSize);
      for (int i = 0; i< n; i++) {
        if (Float.isNaN(data[4*i]) || Float.isNaN(data[4*i+1]) || Float.isNaN(data[4*i]+2)) {
          nanCnt++;
          continue;
        }
        gl.glVertex3f(data[4*i], data[4*i+1], data[4*i+2]);
        bounds[0][0]=Math.min(bounds[0][0], data[4*i]);
        bounds[0][1]=Math.min(bounds[0][1], data[4*i+1]);
        bounds[0][2]=Math.min(bounds[0][2], data[4*i+2]);
        bounds[1][0]=Math.max(bounds[1][0], data[4*i]);
        bounds[1][1]=Math.max(bounds[1][1], data[4*i+1]);
        bounds[1][2]=Math.max(bounds[1][2], data[4*i+2]);
      }
      gl.glEnd(); 
    }
    double[][] bds = new double[2][3];
    bds[0][0]=(double) bounds[0][0];
    bds[0][1]=(double) bounds[0][1];
    bds[0][2]=(double) bounds[0][2];
    bds[1][0]=(double) bounds[1][0];
    bds[1][1]=(double) bounds[1][1];
    bds[1][2]=(double) bounds[1][2];
    bb.setBounds(bds);
    if (nanCnt > 0) System.out.println("nanCnt="+nanCnt);
  }  

  public void postRender(JOGLRenderer jr) {
  }

}
