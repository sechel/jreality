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


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public final class DaaInlined extends Daa{

    final double[] data;
    final int[] offsets;

    private static Object[] decompose(final double[][] initialData) {
        Object[] ret = new Object[2];
        final int length = initialData.length;
        final int[] offsets = new int[length+1]; 
        int dataLength=0;
        for (int i = 0; i < length-1; i++) {
            dataLength+=initialData[i].length;
            offsets[i+1]=dataLength;
        }
        dataLength+=initialData[length-1].length;
        offsets[length]=dataLength;
        final double[] dataVals=new double[dataLength];
        for (int i=0; i < length; i++)
            for (int j = 0; j < initialData[i].length; j++)
                dataVals[offsets[i]+j]=initialData[i][j];
        ret[1] = offsets; ret[0]=dataVals;
        return ret;
    }

    public DaaInlined(double[][] data) {
        this(decompose(data));
    }
    private DaaInlined(Object[] d) {
        this((double[])d[0], (int[])d[1]);
    }
    public DaaInlined(double[] data, int[] offsets) {
        super(offsets.length-1);
        this.data=data;
        this.offsets=offsets;
    }
    public int getLengthAt(int n)
    {
      return offsets[n+1]-offsets[n];
    }
    public double getValueAt(int n, int i)
    {
      return data[getIndex(n, i)];
    }

    private int getIndex(int n, int i) {
        return offsets[n]+i;
    }

    public void setValueAt(int n, int j, double d) {
        data[getIndex(n, j)] = d;
    }
}
