/*
 * Created on 02.06.2005
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

import java.util.List;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickSystem;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.Rn;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class SoftPickSystem implements PickSystem {
    private SceneGraphComponent root;
    private PolygonPipeline pipeline;
    private PickPerspective perspective = new PickPerspective();
    private NewDoublePolygonRasterizer rasterizer;
    private PickVisitor pickVisitor = new PickVisitor();

    private Transformation cameraWorld = new Transformation();
    
    public SoftPickSystem() {
        super();
        int pixels[] = new int[1];
        
        rasterizer = new NewDoublePolygonRasterizer(pixels,true,true,pickVisitor.getHitDetector());
        pipeline = new PolygonPipeline(rasterizer);
        pipeline.setPerspective(perspective);
        pickVisitor.setPipeline(pipeline);
    }
    
    /* (non-Javadoc)
     * @see de.jreality.scene.tool.PickSystem#setSceneRoot(de.jreality.scene.SceneGraphComponent)
     */
    public void setSceneRoot(SceneGraphComponent root) {
        this.root = root;

    }

    /* (non-Javadoc)
     * @see de.jreality.scene.tool.PickSystem#computePick(double[], double[])
     */
    public List computePick(double[] foot, double[] direction) {
        rasterizer.setWindow(0, 1, 0, 1);
        rasterizer.setSize(1, 1);
        rasterizer.start();
        int wh = 200;
        int hh = 200;
        perspective.setWidth(2*wh);
        perspective.setHeight(2*hh);
        
        perspective.setPickPoint(wh, hh);
        
        rasterizer.clear();
        //
        // set camera settings:
        //
        

        perspective.setFieldOfViewDeg(0.1);
        perspective.setNear(0);
        perspective.setFar(1000);
        pickVisitor.getHitDetector().setNdcToCamera(perspective.getInverseMatrix(null));
        
//        double dd[] = (double[]) direction.clone(); 
//        normalToEuler(dd);
//        VecMat.assignRotationZ(tmp  = new double[16],disk[5]);
//        t.multiplyOnRight(tmp);
//        
//        VecMat.assignRotationY(tmp,disk[4]);
//        t.multiplyOnRight(tmp);
//        
//        VecMat.assignRotationX(tmp,disk[3]+Math.PI/2.);
//        t.multiplyOnRight(tmp);
        
        MatrixBuilder mb = MatrixBuilder.euclidian();
        mb.translate(foot[0],foot[1],foot[2]);
        mb.rotateFromTo(new double[] {0,0,-1},direction);
        //cameraWorld.resetMatrix();
        //cameraWorld.multiplyOnLeft(cameraPath.getMatrix(null));
        
        //
        // traverse   
        //
        pipeline.clearPipeline();
        double[] im = new double[16];
        Rn.inverse(im,mb.getMatrix().getArray());
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
        
        return pickVisitor.getHitDetector().getHitList();
    }
    
    
    /**
     * changes the normal (i.e. components 3,4,5) of the i-th result of
     * centerNormalRadius into euler rotation angles.
     */
    private static void normalToEuler(double r[]) {
        double x = r[0];
        double y = r[1];
        double z = r[2];
        double xrot = 0;
        double zrot = 0;
        double yrot = 0;
        
//  if(x*x+y*y -0.0001> 0.) {
//      xrot =  -Math.acos(z);
//      zrot =  Math.atan2(y,x);
//  }
        if(z*z +x*x -0.000001> 0.) {
            xrot =  Math.acos(y);
            yrot =  Math.atan2(x,z);
        }
        //e.set(xrot,yrot,zrot);
        r[0] = xrot;
        r[0] = yrot;
        r[0] = zrot;
        //e.set(Math.PI/2,0,Math.PI/2.);
        //System.err.println("rot "+e+ "   "+ x+ " "+y+" "+z);
    }


}
