/*
 * Created on 16-Jan-2005
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
package de.jreality.scene.data;

import java.io.Serializable;

public final class DaaNormal extends Daa {

    final double[][] data;
    
    public DaaNormal(double[][] data) {
        super(data.length);
        this.data=new double[getLength()][];
        for (int i = 0; i < getLength(); i++) {
            final double[] slot = this.data[i] = new double[data[i].length];
            for (int j=0; j<data[i].length; j++) slot[j] = data[i][j];
        }
    }
    public int getLengthAt(int n)
    {
      return data[n].length;
    }
    public double getValueAt(int n, int i)
    {
      return data[n][i];
    }
    public void setValueAt(int n, int j, double d) {
        data[n][j]=d;
    }
}
