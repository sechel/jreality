/*
 * Created on 09.02.2004
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
package de.jreality.renderman;

import java.awt.Component;

import de.jreality.scene.*;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class RIBViewer implements Viewer {
    private SceneGraphPath cameraPath;
    private SceneGraphComponent sceneRoot;
    private int width=100;
    private int height=100;
    private String fileName="test.rib";
    private String proj = "perspective";
    private int maximumEyeSplits = 10;
    /**
     * 
     */
    public RIBViewer() {
        super();
    }

    /* (non-Javadoc)
     * At the moment there is no viewing component. It is planned to have a java displaydriver
     * for aqsis in the future.
     * @see de.jreality.soft.Viewer#getViewingComponent()
     */
    public Component getViewingComponent() {
        return null;
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.Viewer#setSceneRoot(de.jreality.scene.SceneGraphComponent)
     */
    public void setSceneRoot(SceneGraphComponent c) {
        sceneRoot =c;
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.Viewer#getSceneRoot()
     */
    public SceneGraphComponent getSceneRoot() {
        return sceneRoot;
    }


    /* (non-Javadoc)
     * @see de.jreality.soft.Viewer#render()
     */
    public void render() {
        RIBVisitor rv =new RIBVisitor();
        rv.setWidth(width);
        rv.setHeight(height);
        rv.projection(proj);
        rv.setMaximumEyeSplits(maximumEyeSplits);
        System.out.print(" Rendering renderman RIB into "+fileName+"..");
        rv.visit(sceneRoot,cameraPath,fileName);
        System.out.println(".done."); 
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
        cameraPath = p;        
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
        cameraPath = v.getCameraPath();
        sceneRoot = v.getSceneRoot();
        if (v.hasViewingComponent()){
			setHeight(v.getViewingComponent().getHeight());
			setWidth(v.getViewingComponent().getWidth());
        }
        if(v.getCameraPath().getLastComponent().getCamera().isPerspective()){
        	projection(new String("perspective"));
        }else{
        	projection(new String("orthographic"));
        }
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

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * @return Returns the height.
     */
    public int getHeight() {
        return height;
    }
    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }
    /**
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }
    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @param proj The style of Projection.
     */
    public void projection(String proj) {
        this.proj=proj;
    }

    /**
     * @param maximumEyeSplits.
     */
    public void setMaximumEyeSplits(int maximumEyeSplits){
    	this.maximumEyeSplits=maximumEyeSplits;
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
