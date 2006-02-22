package de.jreality.jogl.shader;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.WeakHashMap;

import net.java.games.jogl.GL;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.*;
import de.jreality.jogl.ClothCalculation;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.SmokeCalculation;
import de.jreality.math.*;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.Rectangle3D;

/**
 * @author weissman
 * 
 */
public class ClothLineShader implements LineShader {

  Texture2D tex;
    
  private double pointRadius;

  private Color diffuseColor;

  static float[] difCol = new float[4];

  static private FloatBuffer data;

  static float[] mat = new float[16];
  static {
    mat[15] = 1;
  }

  protected static double pointSize;
  
  boolean debug;
  private boolean newFrame;
  private boolean write;

  private static boolean array=true;

  protected static boolean sprites=true;

  protected static float[] pointAttenuation = {1f, 0f, 0f};

  static float[] particles = new float[0];

  static boolean setParticles;

//  static float[] vortexData = new float[0];

//  static boolean setVortexData;

//  static double ro = 0.01;

//  static boolean setRo = true;
  
  private static Rectangle3D bb=new Rectangle3D();
  
  private static float[][] bounds=new float[2][3];
  
  private static Matrix rootToObject=new Matrix();
  private static int framecnt;
  private String folder=".";
  private String fileName="particles";
  
  
  ////
  static boolean setGravity = true;
  private static double[] gravity;
  static boolean setInitialPositions = true;
  private static double[] initialPositions;
  static boolean setDamping = true;
  private static double damping;
  static boolean setFactor = true;
  private static double factor;
  private static double[] DEFAULT_GRAVITY = new double[] {0.-0.001,0};
  
  
  private int rows;
  private int columns;
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
//    double curRo = eap.getAttribute(ShaderUtility.nameSpace(name, "ro"), ro);
    pointSize = eap.getAttribute(ShaderUtility.nameSpace(name, "size"), 2.);
    debug = eap.getAttribute(ShaderUtility.nameSpace(name, "debug"), false);
    write = eap.getAttribute(ShaderUtility.nameSpace(name, "write"), false);
    if (write) {
      folder = (String) eap.getAttribute(ShaderUtility.nameSpace(name, "folder"), folder);
      fileName = (String) eap.getAttribute(ShaderUtility.nameSpace(name, "fileName"), fileName);
    }
    sprites = eap.getAttribute(
        ShaderUtility.nameSpace(name, "sprites"), sprites);
    if (sprites) {
      if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap)) {
        tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap);
      } else {
          tex = null;
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
//    float[] rkData = (float[]) eap.getAttribute(ShaderUtility.nameSpace(name,
//        "rungeKuttaData"), vortexData);
//    if (rkData != vortexData) {
//      vortexData = rkData;
//      setVortexData = true;
//    }
//    if (curRo != ro) {
//      ro = curRo;
//      setRo = true;
//    }
    eap.getAttribute("objectToRoot", rootToObject.getArray());
    int fcnt = eap.getAttribute("frameCnt", framecnt);
    if (fcnt > framecnt) {
      framecnt = fcnt;
      newFrame=true;
    }

  ////
    rows = (int) eap.getAttribute(ShaderUtility.nameSpace(name, "rows"), 64);
    
    columns = (int) eap.getAttribute(ShaderUtility.nameSpace(name, "columns"), 64);
    
    double currFactor = (double) eap.getAttribute(ShaderUtility.nameSpace(name, "factor"), 1);
  if(factor != currFactor) {
      factor = currFactor;
      setFactor = true;
  }
  double currDamping = (double) eap.getAttribute(ShaderUtility.nameSpace(name, "damping"), 0.9);
  if(damping != currDamping) {
    damping = currDamping;
    setDamping = true;
  }
  double[] currGravity = (double[]) eap.getAttribute(ShaderUtility.nameSpace(name, "gravity"), DEFAULT_GRAVITY);
  if(gravity != currGravity) {
      gravity = currGravity;
      setGravity = true;
    }
  double[] currInitialPositions = (double[]) eap.getAttribute(ShaderUtility.nameSpace(name, "initialPositions"), DEFAULT_GRAVITY);
  if(initialPositions != currInitialPositions) {
      initialPositions = currInitialPositions;
      setInitialPositions = true;
    }
  }
//  if (curRo != ro) {
//      ro = curRo;
//      setRo = true;
//    }
//  if (curRo != ro) {
//      ro = curRo;
//      setRo = true;
//    }
//  if (curRo != ro) {
//      ro = curRo;
//      setRo = true;
//    }
//  
  ClothCalculation calc;
  
  public void updateData(JOGLRenderer jr) {
    calc = (ClothCalculation) ((GpgpuViewer) jr.theViewer).getCalculation();
    if (calc == null) {
      calc = new ClothCalculation(rows,columns);
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
//    if (setVortexData) {
//      calc.setData(vortexData);
//      calc.triggerCalculation();
//      setVortexData = false;
//    }
//    if (setRo) {
//      calc.setRo(ro);
//      setRo = false;
//    }
    
    if(setDamping) {
        calc.setDamping(damping);
        setDamping = false;
    }
    if(setFactor) {
        calc.setFactor(factor);
        setFactor = false;
    }
    if(setGravity) {
        calc.setGravity(gravity);
        calc.triggerCalculation();
        setGravity = false;
    }
    if(setInitialPositions) {
        calc.setPositions(initialPositions);
        calc.triggerCalculation();
        //setInitialPositions = false;
    }
    
    data = calc.getCurrentValues();
  }

  static WeakHashMap displayLists=new WeakHashMap();
    
  public void render(JOGLRenderer jr) {
    updateData(jr);

    if (data == null) return;
        
    GL gl = jr.globalGL;
        
    gl.glPushAttrib(GL.GL_LIGHTING_BIT);
    gl.glDisable(GL.GL_LIGHTING);

      gl.glColor3fv(difCol);
      gl.glPointSize((float) pointSize);
      if (sprites && tex != null) {
        gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation);
        gl.glEnable(GL.GL_POINT_SPRITE_ARB);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        Texture2DLoaderJOGL.render(jr.getCanvas(), tex);
      }
      //System.out.println(data);
      //GpgpuUtility.dumpData(data);
      data.clear();
      int cnt=data.remaining();

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(4, GL.GL_FLOAT, 0, data);
        gl.glDrawArrays(GL.GL_POINTS, 0, cnt/4);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);

    gl.glPopAttrib();
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

  public static void main(String[] args) {
    new ClothLineShader();
}
}
