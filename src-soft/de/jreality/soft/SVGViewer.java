/*
 * Created on 01.05.2004
 *
 * This file is part of the de.jreality.soft package.
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

import java.io.*;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;

/**
 * This is an experimental SVG viewer for jReality.
 * It is still verry rudimentary and rather a 
 * proof of concept thatn a full featured SVG writer.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class SVGViewer extends AbstractViewer implements Viewer {


    
    //private SVGRasterizer rasterizer;
    
    private String fileName;
    
    /**
     * 
     */
    public SVGViewer(String file) {
        super();
        fileName =file;
    }

    public void setBackgroundColor(int c) {
        argbBackground=c;
        rasterizer.setBackground(c);
    }
    
    void render(int width, int height) {
        File f=new File(fileName);
        PrintWriter w;
        try {
            w = new PrintWriter(new FileWriter(f));
            rasterizer =new SVGRasterizer(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        pipeline =new PolygonPipeline(rasterizer,true);
//        
//        renderTraversal = new RenderTraversal();
//        renderTraversal.setPipeline(pipeline);
//        
//        if(root == null || camera == null)
//            throw new IllegalStateException("need camera and root node");
//        if(width == 0 || height == 0) return;
//
//        //
//        //make sure that the buffered image is of correct size:
//        //
////    if( (width != w || height != h) ) {
//        rasterizer.setWindow(0, width, 0, height);
//        rasterizer.setSize(width, height);
//        rasterizer.start();
//        
//        pipeline.getPerspective().setWidth(width);
//        pipeline.getPerspective().setHeight(height);
////    }
//        rasterizer.clear();
//        //
//        // set camera settings:
//        //
//        
//        DefaultPerspective p =( (DefaultPerspective)pipeline.getPerspective());
//        p.setFieldOfViewDeg(camera.getFieldOfView());
//        p.setNear(camera.getNear());
//        p.setFar(camera.getFar());
//        cameraWorld.resetMatrix();
//        cameraPath.applyEffectiveTransformation(cameraWorld);
//        //SceneGraphUtilities.applyEffectiveTransformation(cameraWorld,(SceneGraphComponent) camera.getParentNode(),root);
//        
//        //
//        // traverse   
//        //
//        pipeline.clearPipeline();
//        double[] im = new double[16];
//        Rn.inverse(im,cameraWorld.getMatrix());
//        //cameraWorld.getInverseMatrix(im);
//        cameraWorld.setMatrix(im);
//        renderTraversal.setInitialTransformation(cameraWorld);
//        renderTraversal.traverse(root);
//        
//        //
//        // sort
//        // TODO: make sorting customizable
//        pipeline.sortPolygons();
//        
//        //
//        // render
//        //
//        pipeline.renderRemaining(rasterizer);
//        // TODO should this (start and stop)
//        // be in the pipeline?
//        rasterizer.stop();
//        
        super.render(width,height);
        w.close();
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#getSignature()
     */
    public int getSignature() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#setSignature(int)
     */
    public void setSignature(int sig) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#setAuxiliaryRoot(de.jreality.scene.SceneGraphComponent)
     */
    public void setAuxiliaryRoot(SceneGraphComponent ar) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#getAuxiliaryRoot()
     */
    public SceneGraphComponent getAuxiliaryRoot() {
        throw new UnsupportedOperationException("not implemented");
    }    
    
}
