/*
 * Created on May 1, 2005
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
package de.jreality.scene.tool.config;

import java.beans.DefaultPersistenceDelegate;
import java.beans.PersistenceDelegate;

import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualConstant {
  
  public static final PersistenceDelegate DELEGATE = new DefaultPersistenceDelegate(new String[]{
    "slot", "value"
  });

  private InputSlot slot;
  private double[] trafo;
  private Double axis;
  private boolean isTrafo;

  public VirtualConstant(InputSlot slot, Object value) {
    this.slot=slot;
    if (value instanceof Double) {
      axis = (Double)value;
    } else {
      trafo = (double[]) value;
      if (trafo.length != 16) throw new IllegalArgumentException("no 4x4 matrix");
      isTrafo=true;
    }
  }
  
  public InputSlot getSlot() {
    return slot;
  }
  public boolean isTrafo() {
    return isTrafo;
  }
  public AxisState getAxisState() {
    if (isTrafo) return null;
    return new AxisState(axis.doubleValue());
  }
  public DoubleArray getTransformationMatrix() {
    if (!isTrafo) return null;
    return new DoubleArray(trafo);
  }
  public String toString() {
    return "VirtualConstant: "+slot+"->"+(isTrafo ? new DoubleArray(trafo).toString() : new AxisState(axis.doubleValue()).toString());
  }

}
