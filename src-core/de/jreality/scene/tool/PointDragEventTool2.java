/*
 * Created on Aug 18, 2005
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
package de.jreality.scene.tool;

import java.util.Collections;
import java.util.List;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Rectangle3D;


public class PointDragEventTool2 extends Tool {

    protected PointDragListener dragListener;
    private DirectionDisplay dirDispl;
    //transient private double startTime;
    private double force;
    private final double stepSize=0.001;
    private final double forceStart=0;   
  
    List activationSlots;
    private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
    List usedSlots = Collections.singletonList(pointerSlot);
    static InputSlot evolutionSlot = InputSlot.getDevice("TrackballTransformation");
    static InputSlot camPath = InputSlot.getDevice("WorldToCamera");
    
    public PointDragEventTool2(String dragSlotName) {
      activationSlots = Collections.singletonList(InputSlot.getDevice(dragSlotName));
      dirDispl=new DirectionDisplay();
      dragListener=new PointDragListenerSphere();      
      //usedSlots.add(evolutionSlot);
      //usedSlots.add(camPath);
    }
    
    public PointDragEventTool2() {
      this("PointDragActivation");
    }
    
    public List getActivationSlots() {
        return activationSlots;
    }

    public List getCurrentSlots() {
        return usedSlots;
    }

    public List getOutputSlots() {
        return Collections.EMPTY_LIST;
    }

    protected boolean active;
    protected PointSet pointSet;
    protected int index=-1;    
    transient EffectiveAppearance eap;
    double[] point;
    Matrix objWorldTrans;
    
    public void activate(ToolContext tc) {
      if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT) { 
    	active = true;
    	//startTime = tc.getTime();    	
    	dirDispl.activate(tc); 
    	tc.deschedule(dirDispl);
        if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
            eap = EffectiveAppearance.create(tc.getRootToToolComponent());
        }
        signature = eap.getAttribute("signature", Pn.EUCLIDEAN);
        
        pointSet = (PointSet) tc.getCurrentPick().getPickPath().getLastElement();        
        index=tc.getCurrentPick().getIndex();  
        point = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index).toDoubleArray(null);
         
        objWorldTrans=new Matrix();
        tc.getCurrentPick().getPickPath().getMatrix(objWorldTrans.getArray());
        
        force=forceStart;
        pointTrans=new Matrix();         
        firePointDragStart(point);        
      }
      else tc.reject();
    }
    
    private Matrix getCenter(SceneGraphComponent comp) {
  	  Matrix centerTranslation = new Matrix();
  	    Rectangle3D bb = GeometryUtility.calculateChildrenBoundingBox(comp);
  	    MatrixBuilder.init(null, signature).translate(bb.getCenter()).assignTo(centerTranslation);
  	    return centerTranslation;
    }
    transient private int signature;
    
    
    Matrix pointTrans;
    transient Matrix evolution = new Matrix();
    double[] oldTrans=new double[4];
    double[] newTrans=new double[4];
    
    Matrix pointerTrans=new Matrix();
        
    public void perform(ToolContext tc) {
      if (!active) return;        
      tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointTrans.getArray());
      tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());
      if(!tc.getAxisState(InputSlot.getDevice("Meta")).isPressed()){
      	evolution.assignFrom(tc.getTransformationMatrix(evolutionSlot));
      	Matrix object2avatar = objToAvatar(tc);
      	evolution.conjugateBy(object2avatar);      
      	//Matrix dirDisplCenter = getCenter(dirDispl);  
      	pointTrans.assignFrom(dirDispl.getDirTransformation());
      	//pointTrans.multiplyOnRight(dirDisplCenter);
      	pointTrans.multiplyOnRight(evolution);
      	//pointTrans.multiplyOnRight(dirDisplCenter.getInverse());
      	dirDispl.setDirTransformation(pointTrans);        	
      
      }else{
    	//Matrix localPointer = ToolUtility.worldToTool(tc, pointerTrans);  
    	double[] from=new double[3];
    	double[] dirs=new double[3];
    	from = (pointerTrans.getColumn(3).length==3) ? pointerTrans.getColumn(3) : Pn.dehomogenize(from,pointerTrans.getColumn(3));
    	dirs = (pointerTrans.getColumn(2).length==3) ? pointerTrans.getColumn(2) : Pn.dehomogenize(dirs,pointerTrans.getColumn(2));
    	Rn.normalize(dirs,dirs);  
    	
    	double[] pointWorld=new double[3];
    	double[] pointWorldTemp=objWorldTrans.multiplyVector(point);
    	pointWorld=(pointWorldTemp.length==3) ? pointWorldTemp : Pn.dehomogenize(pointWorld,pointWorldTemp);
    	double[] dir=dirDispl.getDir();
    	
    	double t=Rn.innerProduct(Rn.crossProduct(null,dir,dirs),Rn.crossProduct(null,Rn.subtract(null,from,pointWorld),dirs));
    	t=t/Rn.euclideanNormSquared(Rn.crossProduct(null,dir,dirs));
    	double[] u=Rn.add(null,pointWorld,Rn.times(null,t,dir));
    	
    	force=Rn.euclideanNorm(Rn.subtract(null,u,pointWorld));
    	if((u[0]-pointWorld[0])/dir[0]<0) force=-force;  
      }
      
  	  double[] pointWorld=new double[3];
  	  double[] pointWorldTemp=objWorldTrans.multiplyVector(point);
	  pointWorld=(pointWorldTemp.length==3) ? pointWorldTemp : Pn.dehomogenize(pointWorld,pointWorldTemp);
	  double[] newPoint=Rn.add(null,pointWorld,Rn.times(null,force,dirDispl.getDir()));
	  newPoint=objWorldTrans.getInverse().multiplyVector(newPoint);
      
      firePointDragged(newPoint);
    }
    
    private Matrix objToAvatar(ToolContext tc) {
        Matrix object2avatar = new Matrix(tc.getRootToToolComponent().getInverseMatrix(null)); 
        Matrix tmp = new Matrix(tc.getTransformationMatrix(camPath));
        Matrix avatarTrans = new Matrix();
        MatrixBuilder.init(null, signature).translate(tmp.getColumn(3)).assignTo(avatarTrans);
        object2avatar.multiplyOnLeft(avatarTrans);
        object2avatar.assignFrom(P3.extractOrientationMatrix(null, object2avatar.getArray(), P3.originP3, signature));
        return object2avatar;
      }

    public void deactivate(ToolContext tc) {
      if (!active) return;      
      dirDispl.deactivate(tc);      
      firePointDragEnd(pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index).toDoubleArray(null));
      index=-1;
      pointSet=null;
      active = false;
    }
    
    public void addPointDragListener(PointDragListener listener) {
      dragListener = PointDragEventMulticaster.add(dragListener, listener);
    }

    public void removePointDragListener(PointDragListener listener) {
      dragListener = PointDragEventMulticaster.remove(dragListener, listener);
    }
    
    protected void firePointDragStart(double[] location) {
      final PointDragListener l=dragListener;
      if (l != null) l.pointDragStart(new PointDragEvent(pointSet, index, location));
    }

    protected void firePointDragged(double[] location) {
      final PointDragListener l=dragListener;
      if (l != null) l.pointDragged(new PointDragEvent(pointSet, index, location));
    }
    
    protected void firePointDragEnd(double[] location) {
      final PointDragListener l=dragListener;
      if (l != null) l.pointDragEnd(new PointDragEvent(pointSet, index, location));
    }
    
    public void setDisplayType(String type){
    	dirDispl.setType(type);
    }
    
    public static void main(String[] args) {
		ViewerApp.display(new SceneGraphComponent());
	}
    
}
