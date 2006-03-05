package de.jreality.jogl.shader;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.WeakHashMap;

import net.java.games.jogl.GL;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.SmokeCalculation;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.Rectangle3D;

/**
 * @author weissman
 * 
 */
public class ParticleLineShader implements LineShader {

  static Texture2D tex;
  
  private double pointRadius;

  private Color diffuseColor;

  static float[] difCol = new float[4];

  static private FloatBuffer data;

  static float[] mat = new float[16];
  static {
    mat[15] = 1;
  }

  protected static boolean renderCheap;
  protected static double pointSize;
  
  boolean debug;
  private boolean newFrame;
  private boolean write;

  private static boolean array=true;

  protected static boolean sprites=true;

  protected static float[] pointAttenuation = {1f, 0f, 0f};

  static float[] particles = new float[0];

  static boolean setParticles;

  static float[] vortexData = new float[0];

  static boolean setVortexData;

  static double ro = 0.01;

  static boolean setRo = true;
  
  private static Rectangle3D bb=new Rectangle3D();
  
  private static float[][] bounds=new float[2][3];
  
  private static Matrix rootToObject=new Matrix();
  private static int framecnt;
  private String folder=".";
  private String fileName="particles";
  
  public boolean providesProxyGeometry() {
    return true;
  }

  public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig,
      boolean useDisplayLists) {
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
    write = eap.getAttribute(ShaderUtility.nameSpace(name, "write"), false);
    if (write) {
      folder = (String) eap.getAttribute(ShaderUtility.nameSpace(name, "folder"), folder);
      fileName = (String) eap.getAttribute(ShaderUtility.nameSpace(name, "fileName"), fileName);
    }
    renderCheap = eap.getAttribute(
        ShaderUtility.nameSpace(name, "renderCheap"), false);
    sprites = eap.getAttribute(
        ShaderUtility.nameSpace(name, "sprites"), sprites);
    if (sprites) {
      if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap)) {
        tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap);
      }
    }
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
    eap.getAttribute("objectToRoot", rootToObject.getArray());
    int fcnt = eap.getAttribute("frameCnt", framecnt);
    if (fcnt > framecnt) {
      framecnt = fcnt;
      newFrame=true;
    }
  }

  SmokeCalculation calc;
  
  public void updateData(JOGLRenderer jr) {
    calc = (SmokeCalculation) ((GpgpuViewer) jr.theViewer).getCalculation();
    if (calc == null) {
      calc = new SmokeCalculation();
      calc.setDisplayTexture(false);
      calc.setMeasureCPS(false);
      calc.setReadData(true);
      ((GpgpuViewer) jr.theViewer).setCalculation(calc);
      System.out.println("setting calculation.");
      return;
    }
    if (setParticles) {
      calc.setValues(particles);
      setParticles = false;
    }
    if (setVortexData) {
      calc.setData(vortexData);
      calc.triggerCalculation();
      setVortexData = false;
    }
    if (setRo) {
      calc.setRo(ro);
      setRo = false;
    }
    data = calc.getCurrentValues();
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
    if (calc == null) return;
    GL gl = jr.globalGL;
        
    gl.glPushAttrib(GL.GL_LIGHTING_BIT);
    gl.glDisable(GL.GL_LIGHTING);

    if (data == null) {
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
      calc.renderPoints(jr);
      gl.glPopAttrib();
      return;
    }

    int n = data.remaining()/4;
    int nanCnt=0;

    if (write && newFrame) {
      StringBuffer fn = new StringBuffer(folder);
      fn.append('/').append(fileName);
      if (framecnt < 1000) fn.append(0);
      if (framecnt < 100) fn.append(0);
      if (framecnt < 10) fn.append(0);
      fn.append(framecnt);
      try {
        FileWriter fw = new FileWriter(fn.toString());
        double [] tmp = new double[4];
        for (int i=0; i<n; i++) {
          tmp[0]=data.get(4*i);
          tmp[1]=data.get(4*i+1);
          tmp[2]=data.get(4*i+2);
          tmp[3]=data.get(4*i+3);
          tmp = rootToObject.multiplyVector(tmp);
          if (Double.isNaN(tmp[0]) || Double.isNaN(tmp[1]) || Double.isNaN(tmp[2])) continue;
          fw.write(tmp[0]+" "+tmp[1]+" "+tmp[2]+"\n");
        }
        fw.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
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
    if (sprites && (renderCheap || data == null)) {
      GL gl = jr.globalGL;
      gl.glDisable(GL.GL_POINT_SPRITE_ARB);
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
      gl.glDisable(GL.GL_TEXTURE_2D);
    }
  }

  public TextShader getTextShader() {
    // TODO Auto-generated method stub
    return null;
  }

}
