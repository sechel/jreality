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

import java.awt.Color;
import java.awt.Component;

import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;

/**
 * This is an experimental PS viewer for jReality.
 * It is still verry rudimentary and rather a 
 * proof of concept thatn a full featured PS writer.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public abstract class AbstractViewer implements Viewer {

    protected Camera camera;
    protected int argbBackground;
    protected SceneGraphComponent root;
    private SceneGraphPath cameraPath;
    private Transformation cameraWorld = new Transformation();

    private PolygonPipeline pipeline;
    protected PolygonRasterizer rasterizer;
    private RenderTraversal renderTraversal;

    int width, height;
    
    public AbstractViewer() {
        super();
    }

    public void setBackgroundColor(int c) {
        argbBackground=c;
        rasterizer.setBackground(c);
    }
    
    void render(int width, int height) {
        
        pipeline =new PolygonPipeline(rasterizer,true);
        
        renderTraversal = new RenderTraversal();
        renderTraversal.setPipeline(pipeline);
        
        if(root == null || camera == null)
            throw new IllegalStateException("need camera and root node");
        if(width == 0 || height == 0) return;

        //
        //make sure that the rasterizer knows is of correct size:
        //
        rasterizer.setWindow(0, width, 0, height);
        rasterizer.setSize(width, height);
        rasterizer.start();
        
        pipeline.getPerspective().setWidth(width);
        pipeline.getPerspective().setHeight(height);

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
        cameraWorld.multiplyOnLeft(cameraPath.getMatrix(null));
        
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
        // TODO: make intersections work
        //Intersector.intersectPolygons(pipeline);
        
        pipeline.sortPolygons();
        
        //
        // render
        //
        pipeline.renderRemaining(rasterizer);
        // TODO should this (start and stop)
        // be in the pipeline?
        rasterizer.stop();
        
    }    
    
//    public Camera getCamera() {
//        return camera;
//    }
//
//    public void setCamera(Camera aCamera) {
//        camera=aCamera;
//        if(root!= null && camera !=null)
//            cameraPath = new SceneGraphPath(root,camera); 
//    }

    public SceneGraphComponent getSceneRoot() {
        return root;
    }

    public void setSceneRoot(SceneGraphComponent component) {
        root=component;
//        if(root!= null && camera !=null)
//            cameraPath = new SceneGraphPath(root,camera); 
    }
    
    
    
    /* (non-Javadoc)
     * @see de.jreality.soft.Viewer#getViewingComponent()
     */
    public Component getViewingComponent() {
        return null;
    }


    /* (non-Javadoc)
     * @see de.jreality.soft.Viewer#render()
     */
    public void render() {
        render(width,height);
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#getCameraPath()
     */
    public SceneGraphPath getCameraPath() {
        return cameraPath;
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#setCameraPath(de.jreality.util.SceneGraphPath)
     */
    public void setCameraPath(SceneGraphPath p) {
        cameraPath =p;
        camera =(Camera) p.getLastElement();
        
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#hasViewingComponent()
     */
    public boolean hasViewingComponent() {
        return false;
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
     */
    public void initializeFrom(Viewer v) {
        setSceneRoot(v.getSceneRoot());
        setCameraPath(v.getCameraPath());
    }

}
