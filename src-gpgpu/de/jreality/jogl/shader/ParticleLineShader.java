package de.jreality.jogl.shader;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.WeakHashMap;

import net.java.games.jogl.GL;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.Input;
import de.jreality.util.Rectangle3D;

/**
 * @author weissman
 * 
 */
public class ParticleLineShader implements LineShader {

  private static byte[] defaultSphereTexture = new byte[128 * 128 * 4];
  static Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", new Appearance(), true);
  static {
    for (int i = 0; i<128; ++i) {
      for (int j = 0; j< 128; ++j)  {
        int I = 4*(i*128+j);
        int sq = (i-64)*(i-64) + (j-64)*(j-64);
        //sq = i*i + j*j;
        if (sq < 4096)  
          {defaultSphereTexture[I] =  defaultSphereTexture[I+1] = defaultSphereTexture[I+2] = defaultSphereTexture[I+3] = (byte) (255- Math.abs(sq/16.0)); }
        else
          {defaultSphereTexture[I] =  defaultSphereTexture[I+1] = defaultSphereTexture[I+2] = defaultSphereTexture[I+3]  = 0;  }
      }
    }
    tex.setImage(new ImageData(defaultSphereTexture, 128, 128));
  }
  
  private double pointRadius;

  private Color diffuseColor;

  static float[] difCol = new float[4];

  static private FloatBuffer data;

  static float[] mat = new float[16];
  static {
    mat[15] = 1;
  }

  private static boolean renderCheap;
  protected static double pointSize;
  private static boolean forthOrder;
  
  boolean debug;

  private static boolean array=true;

  private static boolean sprites=true;

  private static float[] pointAttenuation = {1f, 0f, 0f};

  static float[] particles = new float[0];

  static boolean setParticles;

  static float[] vortexData = new float[0];

  static boolean setVortexData;

  static double ro = 0.01;

  static boolean setRo = true;
  
  private static Geometry ils;
  private static Rectangle3D bb=new Rectangle3D();
  
  private static float[][] bounds=new float[2][3];

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
    double curRo = eap.getAttribute(ShaderUtility.nameSpace(name, "ro"), ro);
    pointSize = eap.getAttribute(ShaderUtility.nameSpace(name, "size"), 2.);
    debug = eap.getAttribute(ShaderUtility.nameSpace(name, "debug"), false);
    forthOrder = eap.getAttribute(ShaderUtility.nameSpace(name, "forthOrder"), forthOrder);
    renderCheap = eap.getAttribute(
        ShaderUtility.nameSpace(name, "renderCheap"), false);
    sprites = eap.getAttribute(
        ShaderUtility.nameSpace(name, "sprites"), sprites);
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
    System.out.println("pointSize="+pointSize);
  }

  public void updateData(JOGLRenderer jr) {
    GpgpuViewer v = (GpgpuViewer) jr.theViewer;
    if (v.isForthOrder() != forthOrder) v.setForthOrder(forthOrder);
    if (setParticles) {        tex.setImage(new ImageData(defaultSphereTexture, 128, 128));

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
    data = v.getCurrentParticlePositions();
  }

  static WeakHashMap displayLists=new WeakHashMap();
  
  private static int getDisplayList(JOGLRenderer jr) {
    if (displayLists.get(jr) != null) return ((int[])displayLists.get(jr))[0];
    GL gl = jr.getCanvas().getGL();
    int[] dlist = new int[]{gl.glGenLists(1)};
    displayLists.put(jr, dlist);
    gl.glNewList(dlist[0], GL.GL_COMPILE);
    gl.glBegin(GL.GL_TRIANGLE_FAN);
    gl.glColor3d(1,1,1);
    gl.glVertex3d(1,1,1);
    gl.glColor3d(0,0,.5);
    gl.glVertex3d(1,-1,1);
    gl.glColor3d(0,0,0);
    gl.glVertex3d(-1,-1,1);
    gl.glColor3d(0,0,.5);
    gl.glVertex3d(-1,1,1);
    gl.glColor3d(0,0,0);
    gl.glVertex3d(-1,1,-1);
    gl.glColor3d(0,0,.5);
    gl.glVertex3d(1,1,-1);
    gl.glColor3d(0,0,0);
    gl.glVertex3d(1,-1,-1);
    gl.glColor3d(0,0,.5);
    gl.glVertex3d(1,-1,1);
    gl.glEnd();
    gl.glEndList();
    return dlist[0];
  }
  
public void render(JOGLRenderer jr) {
    updateData(jr);
    if (particles.length == 0) return;
    if (data == null) return;

    int n = data.remaining()/4;
    int nanCnt=0;
    
    GL gl =   jr.globalGL;
    
    gl.glPushAttrib(GL.GL_LIGHTING_BIT);
    
    gl.glDisable(GL.GL_LIGHTING);
    
    if (!renderCheap) {
      resetBounds();
      double[] orientation = P3.extractOrientationMatrix(null, jr.getContext().getCameraToObject(), Pn.originP3, Pn.EUCLIDEAN);
      MatrixBuilder.euclidean(new Matrix(orientation)).rotateY(-Math.PI/4).rotateX(Math.PI/4);
      int dlist = getDisplayList(jr); //JOGLSphereHelper.getSphereDLists(sphereDetail, jr);
      mat[0] = mat[5] = mat[10] = (float) pointRadius;
      for (int i = 0; i< n; i++) {
        if (Float.isNaN(data.get(4*i)) || Float.isNaN(data.get(4*i+1)) || Float.isNaN(data.get(4*i+2)) ) {
          nanCnt++;
          continue;
        }
        mat[3] = data.get(4*i);
        mat[7] = data.get(4*i+1);
        mat[11] = data.get(4*i+2);
        
        bounds(i);
        
        gl.glPushMatrix();
        gl.glMultTransposeMatrixf(mat);
        gl.glMultTransposeMatrixd(orientation);
        gl.glCallList(dlist);
        gl.glPopMatrix();
      }
      setBounds();    
    } else {
      gl.glColor3fv(difCol);
      gl.glPointSize((float) pointSize);
      if (sprites) {
        gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation);
        gl.glEnable(GL.GL_POINT_SPRITE_ARB);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        Texture2DLoaderJOGL.render(jr.getCanvas(), tex);
      }
      int cnt=data.remaining();
      if (array) {
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(4, GL.GL_FLOAT, 0, data);
        gl.glDrawArrays(GL.GL_POINTS, 0, cnt/4);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
      } else {
        gl.glBegin(GL.GL_POINTS);
        for (int i=0; i<cnt; ) {
          gl.glVertex4f(data.get(i++), data.get(i++), data.get(i++), data.get(i++));
        }
        gl.glEnd();
      }
    } 
    if (nanCnt > 0) System.out.println("nanCnt="+nanCnt);
    gl.glPopAttrib();
  }

private void setBounds() {
	double[][] bds = new double[2][3];
    bds[0][0]=(double) bounds[0][0];
    bds[0][1]=(double) bounds[0][1];
    bds[0][2]=(double) bounds[0][2];
    bds[1][0]=(double) bounds[1][0];
    bds[1][1]=(double) bounds[1][1];
    bds[1][2]=(double) bounds[1][2];
    bb.setBounds(bds);
}

private void bounds(int i) {
	bounds[0][0]=Math.min(bounds[0][0], data.get(4*i));
	bounds[0][1]=Math.min(bounds[0][1], data.get(4*i+1));
	bounds[0][2]=Math.min(bounds[0][2], data.get(4*i+2));
	bounds[1][0]=Math.max(bounds[1][0], data.get(4*i));
	bounds[1][1]=Math.max(bounds[1][1], data.get(4*i+1));
	bounds[1][2]=Math.max(bounds[1][2], data.get(4*i+2));
}

private void resetBounds() {
	bounds[0][0]=Float.MAX_VALUE;
    bounds[0][1]=Float.MAX_VALUE;
    bounds[0][2]=Float.MAX_VALUE;
    bounds[1][0]=Float.MIN_VALUE;
    bounds[1][1]=Float.MIN_VALUE;
    bounds[1][2]=Float.MIN_VALUE;
}  

  public void postRender(JOGLRenderer jr) {
    if (renderCheap && sprites) {
      GL gl = jr.globalGL;
      gl.glDisable(GL.GL_POINT_SPRITE_ARB);
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
      gl.glDisable(GL.GL_TEXTURE_2D);
    }
  }

}
