/*
 * Created on Apr 3, 2005
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

import java.util.List;
import java.util.Map;

import de.jreality.math.Rn;
import de.jreality.scene.data.DoubleArray;


/**
 *
 * TODO: comment this
 *
 * @author brinkman
 *
 */
public class VirtualRawMousePointerTrafo implements VirtualDevice {

  InputSlot outSlot;
  InputSlot ndcToWorldSlot;
  InputSlot pointerNdcSlot;
  
  double[] ndcToWorld = new double[16];
  double[] pointerNdc = new double[16];
  double[] pointerTrafo = new double[16];
  private DoubleArray outArray = new DoubleArray(pointerTrafo);
  
  
  public ToolEvent process(VirtualDeviceContext context) {
  	try {
    ndcToWorld = context.getTransformationMatrix(ndcToWorldSlot).toDoubleArray(ndcToWorld);
    pointerNdc = context.getTransformationMatrix(pointerNdcSlot).toDoubleArray(pointerNdc);
  	} catch (Exception e) {
		return null;
	}
  
  	double x = pointerNdc[3];
  	double y = pointerNdc[7];
  	
  	pointerNdc[0] = x+1;
  	pointerNdc[4] = y;
  	pointerNdc[8] = -1;
  	pointerNdc[12] = 1;
  	
  	pointerNdc[1] = x;
  	pointerNdc[5] = y+1;
  	pointerNdc[9] = -1;
  	pointerNdc[13] = 1;
  	
  	pointerNdc[2] = x;
  	pointerNdc[6] = y;
  	pointerNdc[10] = 1;
  	pointerNdc[14] = 1;
  	
  	pointerTrafo = Rn.times(pointerTrafo, ndcToWorld, pointerNdc);
	return new ToolEvent(context.getEvent().getSource(), outSlot, outArray);
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    outSlot = result;
    ndcToWorldSlot = (InputSlot) inputSlots.get(0);
    pointerNdcSlot = (InputSlot) inputSlots.get(1);
  }

  public void dispose() {
  }

  public String getName() {
    return "MousePointerTrafo";
  }

  public String toString() {
    return "VirtualDevice: "+getName();
  }
  
  private void scaleColumn(double[] matrix, int col, double factor) {
  	matrix[col]*=factor;
  	matrix[col+4]*=factor;
  	matrix[col+8]*=factor;
  	matrix[col+12]*=factor;
  }
  
  private void columnTrafo(double[] matrix, int i, int j, double factor) {
  	matrix[i]+=matrix[j]*factor;
  	matrix[i+4]+=matrix[j+4]*factor;
  	matrix[i+8]+=matrix[j+8]*factor;
  	matrix[i+12]+=matrix[j+12]*factor;
  }
  
  private double scalarColumnProduct(double[] matrix, int i, int j) {
  	return matrix[i]*matrix[j]+
		   matrix[i+4]*matrix[j+4]+
		   matrix[i+8]*matrix[j+8]+
		   matrix[i+12]*matrix[j+12]; 
  }
  
  private double columnNorm(double[] matrix, int i) {
  	return Math.sqrt(scalarColumnProduct(matrix, i, i));
  }
}
