/*
 * Created on Dec 6, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.soft;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.shader.CommonAttributes;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public abstract class Renderer {
  protected Camera              camera;
  protected int                 argbBackground;
  protected SceneGraphComponent root;
  private SceneGraphPath cameraPath;
  private Transformation cameraWorld = new Transformation();

  private PolygonPipeline pipeline;
  private PolygonRasterizer rasterizer;
  private RenderTraversal renderTraversal;

  Renderer(PolygonPipeline pipe, PolygonRasterizer ras) {
    pipeline=pipe;
    rasterizer=ras;
    renderTraversal = new RenderTraversal();
    renderTraversal.setPipeline(pipeline);
  }

	public void setBackgroundColor(int c) {
    argbBackground=c;
    rasterizer.setBackground(c);
  }
  public int getBackgroundColor() {
    return argbBackground;
  }

  public abstract void render();
  public abstract void update();
  
  void render(int width, int height) {
    if(root == null || camera == null)
      throw new IllegalStateException("need camera and root node");
    if(width == 0 || height == 0) return;

    //
    //make sure that the buffered image is of correct size:
    //
//    if( (width != w || height != h) ) {
      rasterizer.setWindow(0, width, 0, height);
      rasterizer.setSize(width, height);
      
      pipeline.getPerspective().setWidth(width);
      pipeline.getPerspective().setHeight(height);
//    }
      Appearance a = root.getAppearance();
      Color background;
      if(a != null) {
          Object o = a.getAttribute(CommonAttributes.BACKGROUND_COLOR);

          if( o instanceof Color) background = (Color) o;
          else background = Color.WHITE;
      } else
          background = Color.WHITE;
    rasterizer.setBackground(background.getRGB());
    rasterizer.clear();
    //
    // set camera settings:
    //
    
    DefaultPerspective p =( (DefaultPerspective)pipeline.getPerspective());
    p.setFieldOfViewDeg(camera.getFieldOfView());
    p.setNear(camera.getNear());
    p.setFar(camera.getFar());
    cameraWorld.resetMatrix();
    //cameraPath.applyEffectiveTransformation(cameraWorld);
    cameraWorld.setMatrix(cameraPath.getMatrix(null));
    //SceneGraphUtilities.applyEffectiveTransformation(cameraWorld,(SceneGraphComponent) camera.getParentNode(),root);
    
    //
    // traverse   
    //
    pipeline.clearPipeline();
	double[] im = new double[16];
	Rn.inverse(im,cameraWorld.getMatrix());
	//cameraWorld.getInverseMatrix(im);
	cameraWorld.setMatrix(im);
    renderTraversal.setInitialTransformation(cameraWorld);
    renderTraversal.traverse(root);
    
    //
    // sort
    // TODO: make sorting customizable
    pipeline.sortPolygons();
    
    //
    // render
    //
    pipeline.renderRemaining(rasterizer);
    rasterizer.stop();
  }    
	
  public SceneGraphPath getCameraPath() {
    return cameraPath;
  }

  public void setCameraPath(SceneGraphPath p) {
      cameraPath = p;
    camera=(Camera) p.getLastElement();
//    if(root!= null && camera !=null)
//        //cameraPath = new SceneGraphPath(root,camera); 
//        cameraPath = SceneGraphPath.getFirstPathBetween(root,camera); 
  }

  public SceneGraphComponent getSceneRoot() {
    return root;
  }

  public void setSceneRoot(SceneGraphComponent component) {
    root=component;
    if(root!= null && camera !=null)
        //cameraPath = new SceneGraphPath(root,camera); 
        cameraPath = SceneGraphPath.getFirstPathBetween(root,camera); 
  }

//  public static class ByteArray extends Renderer
//  {
//    final BufferedImage img;
//    final Dimension d;
//    final byte[] pixels;
//
//    ByteArray(BufferedImage bi) {
//      this(bi, new byte[3 * bi.getWidth() * bi.getHeight()]);
//    }
//    private ByteArray(BufferedImage bi, byte[] bBuffer) {
//      this(bi, createRasterizer(bBuffer), bBuffer);
//    }
//    private ByteArray(BufferedImage bi, PolygonRasterizer r, byte[] bBuffer) {
//      super(createPipeline(bi, r), r);
////      new BufferedImage(d.width, d.height, BufferedImage.TYPE_3BYTE_BGR)
//      d=new Dimension(bi.getWidth(), bi.getHeight());
//      pixels=bBuffer;
//      img=bi;
//    }
//    private static PolygonRasterizer createRasterizer(byte[] pixels) {
//      return new ByteRasterizer(pixels);
//    }
//    static PolygonPipeline createPipeline(BufferedImage bi, PolygonRasterizer r) {
//      return new PolygonPipeline(r);
//    }
//    public void render() {
//        render(d.width, d.height);
//    }
//    public void update() {
//      img.getRaster().setDataElements(0, 0, d.width, d.height, pixels);
//    }
//
//  }
  public static class IntArray extends Renderer
  {
    final BufferedImage img;
    final Dimension d;
    final int[] pixels;

    IntArray(BufferedImage bi) {
      this(bi, new int[bi.getWidth() * bi.getHeight()]);
    }
    private IntArray(BufferedImage bi, int[] bBuffer) {
      this(bi, createRasterizer(bBuffer), bBuffer);
    }
    private IntArray(BufferedImage bi, PolygonRasterizer r, int[] bBuffer) {
      super(createPipeline(bi, r), r);
      d=new Dimension(bi.getWidth(), bi.getHeight());
      pixels=bBuffer;
      img=bi;
    }
    private static PolygonRasterizer createRasterizer(int[] pixels) {
        //return new IntRasterizer(pixels);
        return new NewPolygonRasterizer(pixels);
    }
    static PolygonPipeline createPipeline(BufferedImage bi, PolygonRasterizer r) {
      return new PolygonPipeline(r);
    }
    public void render() {
        render(d.width, d.height);
    }
    public void update() {
      img.getRaster().setDataElements(0, 0, d.width, d.height, pixels);
    }

  }
  public static class IntArrayDouble extends Renderer
  {
      final BufferedImage img;
      final Dimension d;
      final int[] pixels;

      IntArrayDouble(BufferedImage bi) {
          this(bi, new int[bi.getWidth() * bi.getHeight()]);
      }
      private IntArrayDouble(BufferedImage bi, int[] bBuffer) {
          this(bi, createRasterizer(bBuffer), bBuffer);
      }
      private IntArrayDouble(BufferedImage bi, PolygonRasterizer r, int[] bBuffer) {
          super(createPipeline(bi, r), r);
          d=new Dimension(bi.getWidth(), bi.getHeight());
          pixels=bBuffer;
          img=bi;
      }
      private static PolygonRasterizer createRasterizer(int[] pixels) {
          //return new DoubleRasterizerInt(pixels);
          return new NewDoublePolygonRasterizer(pixels);
      }
      static PolygonPipeline createPipeline(BufferedImage bi, PolygonRasterizer r) {
          return new PolygonPipeline(r);
      }
      public void render() {
          render(d.width, d.height);
      }
      public void update() {
          img.getRaster().setDataElements(0, 0, d.width, d.height, pixels);
      }

  }
  public static class ByteArrayDouble extends Renderer
  {
      final BufferedImage img;
      final Dimension d;
      final byte[] pixels;

      ByteArrayDouble(BufferedImage bi) {
          this(bi, new byte[3 * bi.getWidth() * bi.getHeight()]);
      }
      private ByteArrayDouble(BufferedImage bi, byte[] bBuffer) {
          this(bi, createRasterizer(bBuffer), bBuffer);
      }
      private ByteArrayDouble(BufferedImage bi, PolygonRasterizer r, byte[] bBuffer) {
          super(createPipeline(bi, r), r);
//      new BufferedImage(d.width, d.height, BufferedImage.TYPE_3BYTE_BGR)
          d=new Dimension(bi.getWidth(), bi.getHeight());
          pixels=bBuffer;
          img=bi;
      }
      private static PolygonRasterizer createRasterizer(byte[] pixels) {
          return new DoubleRasterizer(pixels);
      }
      static PolygonPipeline createPipeline(BufferedImage bi, PolygonRasterizer r) {
          return new PolygonPipeline(r);
      }
      public void render() {
          render(d.width, d.height);
      }
      public void update() {
          img.getRaster().setDataElements(0, 0, d.width, d.height, pixels);
      }

  }
  
}

