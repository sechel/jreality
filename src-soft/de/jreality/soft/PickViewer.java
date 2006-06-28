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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.soft;

import java.awt.Component;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public  class PickViewer implements Viewer {

    protected Camera camera;
    protected int argbBackground;
    protected SceneGraphComponent root;
    private SceneGraphPath cameraPath;
    private Transformation cameraWorld = new Transformation();

    private PolygonPipeline pipeline;
    private PickPerspective perspective = new PickPerspective();
    private NewDoublePolygonRasterizer rasterizer;
    private PickVisitor pickVisitor = new PickVisitor();

    int width, height;
    private int pickX;
    private int pickY;
    
    public PickViewer() {
        super();
        int pixels[] = new int[1];
        
        rasterizer = new NewDoublePolygonRasterizer(pixels,true,true,pickVisitor.getHitDetector());
        pipeline = new PolygonPipeline(rasterizer);
        pipeline.setPerspective(perspective);
        pickVisitor.setPipeline(pipeline);
    }

    
    void render(int width, int height) {
        
        //pipeline =new PolygonPipeline(rasterizer,true);
        
        
        if(root == null || camera == null)
            throw new IllegalStateException("need camera and root node");
        if(width == 0 || height == 0) return;

        //
        //make sure that the rasterizer is of correct size:
        //
//        rasterizer.setWindow(0, width, 0, height);
//        rasterizer.setSize(width, height);
        rasterizer.setWindow(0, 1, 0, 1);
        rasterizer.setSize(1, 1);
        rasterizer.start();
        
        perspective.setWidth(width);
        perspective.setHeight(height);
        
        perspective.setPickPoint(pickX, pickY);
        
        rasterizer.clear();
        //
        // set camera settings:
        //
        

        perspective.setFieldOfViewDeg(camera.getFieldOfView());
        perspective.setNear(camera.getNear());
        perspective.setFar(camera.getFar());
        pickVisitor.getHitDetector().setNdcToCamera(perspective.getInverseMatrix(null));
        
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
        pickVisitor.setInitialTransformation(cameraWorld);
        pickVisitor.traverse(root);
        
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
        pipeline.clearPipeline();
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
    public void setPickPoint(int x, int y) {
        this.pickX = x;
        this.pickY = y;
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
    public List getHitList(){
        return pickVisitor.getHitDetector().getHitList();
    }

	public SceneGraphComponent getAuxiliaryRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAuxiliaryRoot(SceneGraphComponent ar) {
		// TODO Auto-generated method stub
		
	}

}
