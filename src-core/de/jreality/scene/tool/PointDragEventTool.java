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

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;


/**
 *
 * TODO: document this
 *
 * @author weissman
 *
 */
public class PointDragEventTool extends Tool {

  protected PointDragListener dragListener;
  
    List activationSlots;
    private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
    List usedSlots = Collections.singletonList(pointerSlot);
    
    public PointDragEventTool(String dragSlotName) {
      activationSlots = Collections.singletonList(InputSlot.getDevice(dragSlotName));
    }
    
    public PointDragEventTool() {
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
    
    private Matrix pointerToPoint = new Matrix();
    
    public void activate(ToolContext tc) {
      if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT) {
        active = true;
        pointSet = (PointSet) tc.getCurrentPick().getPickPath().getLastElement();
        index=tc.getCurrentPick().getIndex();
        tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerToPoint.getArray());
        pointerToPoint.invert();
        pointerToPoint.multiplyOnRight(tc.getRootToLocal().getMatrix(null));
        double[] point = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index).toDoubleArray(null);
        MatrixBuilder.euclidean(pointerToPoint).translate(point);
        firePointDragStart(point);
      }
      else tc.reject();
    }
    
    Matrix result = new Matrix();
        
    public void perform(ToolContext tc) {
      if (!active) return;
      tc.getTransformationMatrix(pointerSlot).toDoubleArray(result.getArray());
      result.multiplyOnRight(pointerToPoint);
      result.multiplyOnLeft(tc.getRootToLocal().getInverseMatrix(null));
      firePointDragged(result.getColumn(3));
    }

    public void deactivate(ToolContext tc) {
      if (!active) return;
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

}
