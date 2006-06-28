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
