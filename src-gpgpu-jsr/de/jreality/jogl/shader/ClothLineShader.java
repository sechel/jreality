package de.jreality.jogl.shader;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.WeakHashMap;

import javax.media.opengl.GL;

import de.jreality.jogl.ClothCalculation;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.math.Matrix;
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
public class ClothLineShader extends AbstractPrimitiveShader implements LineShader {

  Texture2D spriteTex;
  
  Texture2D tex; 
  
  private double pointRadius;

  private Color diffuseColor;

  static float[] difCol = new float[4];

  static private FloatBuffer data;
  static private IntBuffer index;
  static private FloatBuffer texCoords;
  
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

  public int proxyGeometryFor(JOGLRenderingState jrs) {
//    if (original.getGeometryAttributes(GeometryUtility.BOUNDING_BOX) != bb)
//      original.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, bb);
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
        spriteTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap);
      } else {
          spriteTex = null;
      }
    }
    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, CommonAttributes.TEXTURE_2D), eap)) {
      tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, CommonAttributes.TEXTURE_2D), eap);
    } else {
        tex = null;
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

  private static boolean inited;
  
  public void updateData(JOGLRenderer jr) {
    calc = (ClothCalculation) ((GpgpuViewer) jr.theViewer).getCalculation();
    if (calc == null) {
      calc = new ClothCalculation(rows,columns);
      calc.setDisplayTexture(false);
      calc.setMeasureCPS(false);
      calc.setReadData(true);
      ((GpgpuViewer) jr.theViewer).setCalculation(calc);
      System.out.println("setting calculation.");
      inited=true;
      // prepare index buffer and tex coord buffer:
      
      int cols = columns*columns;
      index = ByteBuffer.allocateDirect(4*4*(rows-1)*(cols-1)).order(ByteOrder.nativeOrder()).asIntBuffer();
      texCoords = ByteBuffer.allocateDirect(2*4*(rows)*(cols)).order(ByteOrder.nativeOrder()).asFloatBuffer();
      
      for(int j = 0; j< (rows-1); j++)
        for(int i = 0; i< (cols-1); i++) {
          int pos = j*cols +i; 
          index.put(pos);
          index.put(pos+1);
          index.put(pos+1+cols);
          index.put(pos+cols);
        }
      for(int j = 0; j< (rows); j++)
        for(int i = 0; i< (cols); i++) { 
          texCoords.put(i/(float)(cols-1));
          texCoords.put(j/(float)(rows-1));
        }
      //GpgpuUtility.dumpData(index);
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
    
  public void render(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
    updateData(jr);

    if (data == null || inited) {
      inited=false;
      return;
    }
        
    GL gl = jr.globalGL;
        
    gl.glPushAttrib(GL.GL_LIGHTING_BIT);
    gl.glDisable(GL.GL_LIGHTING);

      gl.glColor3fv(difCol,0);
      gl.glPointSize((float) pointSize);
      if (sprites && spriteTex != null) {
        gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation, 0);
        gl.glEnable(GL.GL_POINT_SPRITE_ARB);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        Texture2DLoaderJOGL.render(jr.getCanvas(), spriteTex);
      }
      //System.out.println(data);
      //GpgpuUtility.dumpData(data);
      
      data.clear();
      index.clear();
      int cnt=index.remaining();

      if (tex != null) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        Texture2DLoaderJOGL.render(jr.getCanvas(), tex);
      }
      
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        
        gl.glVertexPointer(4, GL.GL_FLOAT, 0, data);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords);
        
        //gl.glEnableClientState(GL.GL_INDEX_ARRAY);
        //gl.glIndexPointer(GL.GL_INT, 0, index);
        
        gl.glLockArraysEXT(0, data.remaining()/4);
        
        //gl.glDrawArrays(GL.GL_QUADS, 0, cnt);
        gl.glDrawElements(GL.GL_QUADS, cnt, GL.GL_UNSIGNED_INT, index);
        
        gl.glUnlockArraysEXT();
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_INDEX_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);

    gl.glPopAttrib();
  }

  public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
    GL gl = jr.globalGL;
    if (sprites) {
      gl.glDisable(GL.GL_POINT_SPRITE_ARB);
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
      gl.glDisable(GL.GL_TEXTURE_2D);
    }
    if (tex != null) {
      gl.glDisable(GL.GL_TEXTURE_2D);
    }

  }

  public static void main(String[] args) {
    new ClothLineShader();
}

		
}
