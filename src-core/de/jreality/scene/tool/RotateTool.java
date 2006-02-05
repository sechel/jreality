/*
 * Created on Apr 10, 2005
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
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.Rectangle3D;

/**
 *
 * TODO: document this
 *
 * @author brinkman
 *
 */
public class RotateTool extends Tool {

  transient List activationSlots = new LinkedList();
  transient List usedSlots = new LinkedList();

  static InputSlot activationSlot = InputSlot.getDevice("RotateActivation");
  static InputSlot evolutionSlot = InputSlot.getDevice("TrackballTransformation");
  static InputSlot camPath = InputSlot.getDevice("WorldToCamera");

  boolean fixOrigin=false;
  
  public RotateTool() {
    activationSlots.add(activationSlot);
    usedSlots.add(evolutionSlot);
    usedSlots.add(camPath);
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

  transient SceneGraphComponent comp;

  transient Matrix center=new Matrix();
  
  public void activate(ToolContext tc) {
    startTime = tc.getTime();
    comp = (moveChildren ? tc.getRootToLocal():tc.getRootToToolComponent()).getLastComponent();
    // stop possible animation
    tc.deschedule(comp);
    if (comp.getTransformation() == null)
      comp.setTransformation(new Transformation());
    if (!fixOrigin)
    	center = getCenter(comp);
  }
  
  private Matrix getCenter(SceneGraphComponent comp) {
	  Matrix centerTranslation = new Matrix();
	    Rectangle3D bb = GeometryUtility.calculateChildrenBoundingBox(comp);

	    centerTranslation.setColumn(3, bb.getCenter());
	    centerTranslation.setEntry(3,3,1);
	    return centerTranslation;
  }

  transient Matrix result = new Matrix();
  transient Matrix evolution = new Matrix();
  
  transient private double startTime;
  
  private boolean moveChildren;

  private double animTimeMin=250;
  private double animTimeMax=750;
  private boolean updateCenter;

  public void perform(ToolContext tc) {
    Matrix object2avatar = objToAvatar(tc);
    evolution.assignFrom(tc.getTransformationMatrix(evolutionSlot));
    evolution.conjugateBy(object2avatar);
    if (!fixOrigin && updateCenter) center = getCenter(comp);
    
	result.assignFrom(comp.getTransformation());
    result.multiplyOnRight(center);
    result.multiplyOnRight(evolution);
    result.multiplyOnRight(center.getInverse());
    comp.getTransformation().setMatrix(result.getArray());
  }

  /**
   * @return
   */
  private Matrix objToAvatar(ToolContext tc) {
    Matrix object2avatar = new Matrix((moveChildren ? tc.getRootToLocal():tc.getRootToToolComponent()).getInverseMatrix(null)); 
    // TODO: see if we can't remove head dependency from Rotate device
    Matrix tmp = new Matrix(tc.getTransformationMatrix(camPath));
    Matrix avatarTrans = new Matrix();
    avatarTrans.setColumn(3, tmp.getColumn(3));
    object2avatar.multiplyOnLeft(avatarTrans);
    object2avatar = object2avatar.getRotation();
    return object2avatar;
  }

  public void enforceConstraints(Matrix matrix) {
    // do nothing, for now
  }

  public void deactivate(ToolContext tc) {
	  double t = tc.getTime()-startTime; 
    if (t > animTimeMin && t < animTimeMax) {
      AnimatorTask task = new AnimatorTask() {
        Matrix e = new Matrix(evolution);
        Matrix cen = new Matrix(center); 
        SceneGraphComponent c = comp;
        public boolean run(AnimatorContext ac) {
          if (updateCenter) cen = getCenter(c);
          Matrix m=new Matrix(c.getTransformation().getMatrix());
		  m.multiplyOnRight(cen);
		  m.multiplyOnRight(e);
		  m.multiplyOnRight(cen.getInverse());
		  m.assignTo(c.getTransformation());
          return true;
        }
      };
      tc.schedule(comp, task);
    }
  }
  
  public boolean getMoveChildren() {
    return moveChildren;
  }
  public void setMoveChildren(boolean moveChildren) {
    this.moveChildren = moveChildren;
  }

public double getAnimTimeMax() {
	return animTimeMax;
}

public void setAnimTimeMax(double animTimeMax) {
	this.animTimeMax = animTimeMax;
}

public double getAnimTimeMin() {
	return animTimeMin;
}

public void setAnimTimeMin(double animTimeMin) {
	this.animTimeMin = animTimeMin;
}

public boolean isUpdateCenter() {
	return updateCenter;
}

public void setUpdateCenter(boolean updateCenter) {
	this.updateCenter = updateCenter;
	if (!updateCenter)
		center=new Matrix();
}

public boolean isFixOrigin() {
	return fixOrigin;
}

public void setFixOrigin(boolean fixOrigin) {
	this.fixOrigin = fixOrigin;
}

}
