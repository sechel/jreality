/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.softviewer;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class Renderer {
  protected Camera              camera;
  protected SceneGraphComponent root;
  protected SceneGraphComponent auxiliaryRoot;
  private SceneGraphPath cameraPath;
  private Transformation cameraWorld = new Transformation();

  private TrianglePipeline pipeline;
  private TriangleRasterizer rasterizer;
  private RenderingVisitor renderTraversal;

  final BufferedImage img;
  final Dimension d;
  final int[] pixels;

  Renderer(BufferedImage bi) {
    img = bi;
    d = new Dimension(bi.getWidth(), bi.getHeight());
    pixels = new int[d.width * d.height];
    rasterizer = new DoubleTriangleRasterizer(pixels);
    pipeline = new TrianglePipeline(rasterizer);
    renderTraversal = new RenderingVisitor();
    renderTraversal.setPipeline(pipeline);
    } 
  
   
	public void setBackgroundColor(int c) {
        rasterizer.setBackground(c);
  }
    
  public int getBackgroundColor() {
    return rasterizer.getBackground();
  }

 
  void render(int width, int height) {
    //
    //make sure that the buffered image is of correct size:
    //
//    if( (width != w || height != h) ) {
      rasterizer.setWindow(0, width, 0, height);
      rasterizer.setSize(width, height);
      
      pipeline.getPerspective().setWidth(width);
      pipeline.getPerspective().setHeight(height);
//    }
    Appearance a = root == null ? null : root.getAppearance();
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
    
    if (root != null && camera != null) {
      PerspectiveProjection p =( (PerspectiveProjection)pipeline.getPerspective());
      p.setFieldOfViewDeg(camera.getFieldOfView());
      p.setNear(camera.getNear());
      p.setFar(camera.getFar());
      DefaultMatrixSupport.getSharedInstance().restoreDefault(cameraWorld, true);
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
      if(auxiliaryRoot!= null)
          renderTraversal.traverse(auxiliaryRoot);
    }    
    pipeline.finish();
    rasterizer.stop();
  }    
	
  public SceneGraphPath getCameraPath() {
    return cameraPath;
  }

  public void setCameraPath(SceneGraphPath p) {
    cameraPath = p;
    camera= p == null ? null : (Camera) p.getLastElement();
  }

  public SceneGraphComponent getSceneRoot() {
    return root;
  }

  public void setSceneRoot(SceneGraphComponent component) {
    root=component;
    if(root!= null && camera !=null) {
        //cameraPath = new SceneGraphPath(root,camera); 
        List camPaths = SceneGraphUtility.getPathsBetween(root, camera);
        if (camPaths.size() > 0) cameraPath = (SceneGraphPath) camPaths.get(0);
        else {
          camera = null;
          cameraPath = null;
        }
    }
  }
    public SceneGraphComponent getAuxiliaryRoot() {
        return auxiliaryRoot;
      }

      public void setAuxiliaryRoot(SceneGraphComponent component) {
          auxiliaryRoot=component;
        if(root!= null && camera !=null) {
            //cameraPath = new SceneGraphPath(root,camera); 
            List camPaths = SceneGraphUtility.getPathsBetween(root, camera);
            if (camPaths.size() > 0) cameraPath = (SceneGraphPath) camPaths.get(0);
            else {
              camera = null;
              cameraPath = null;
            }
        }
  }

      public void render() {
          render(d.width, d.height);
      }
      public void update() {
          img.getRaster().setDataElements(0, 0, d.width, d.height, pixels);
      }


}

