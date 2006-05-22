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

import de.jreality.math.Rn;


/**
 *
 * TODO: comment this
 *
 * @author brinkman
 *
 */
public class VirtualMousePointerTrafo extends VirtualRawMousePointerTrafo {

   public ToolEvent process(VirtualDeviceContext context) {
	   ToolEvent te = super.process(context);
	   // this is the normalization code that VirtualRawMousePointerTrafo lacks
     for(int i=0; i<4; i++)
    	if (Math.abs(pointerTrafo[12+i])>Rn.TOLERANCE)
    		scaleColumn(pointerTrafo, i, 1/pointerTrafo[i+12]);
    for(int i = 0; i<3; i++)
    	columnTrafo(pointerTrafo, i, 3, -1);
    
    double nrm = columnNorm(pointerTrafo, 2);
	if (nrm>Rn.TOLERANCE)
	    scaleColumn(pointerTrafo, 2, -1/nrm);
    for(int i = 1; i>=0; i--) {
    	columnTrafo(pointerTrafo, i, i+1, -scalarColumnProduct(pointerTrafo, i, i+1));
 
    }
    columnTrafo(pointerTrafo, 1, 2, -scalarColumnProduct(pointerTrafo, 1, 2));
   	nrm = columnNorm(pointerTrafo, 1);
	if (nrm>Rn.TOLERANCE)
	    scaleColumn(pointerTrafo, 1, 1/nrm);
	columnTrafo(pointerTrafo, 0, 2, -scalarColumnProduct(pointerTrafo, 0, 2));
	columnTrafo(pointerTrafo, 0, 1, -scalarColumnProduct(pointerTrafo, 0, 1));
	nrm = columnNorm(pointerTrafo, 0);
	if (nrm>Rn.TOLERANCE)
	    scaleColumn(pointerTrafo, 0, 1/nrm);
	
  pointerTrafo[12]=pointerTrafo[13]=pointerTrafo[14]=0;
  pointerTrafo[15]=1;

	return te;
  }

  public String getName() {
    return "MousePointerTrafo";
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
