/*
 * Created on 07.09.2006
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
package de.jreality.softviewer;

public class Polygon extends AbstractPolygon {
    private int length;
    private double[][] data;
    public Polygon(int size) {
        super();
        length = size;
        data = new double[length][VERTEX_LENGTH];
    }

    @Override
    public final double[] getPoint(int i) {
        if(i>=length) length = i+1;
        ensureLength();
        return data[i];
    }

    /**
     * 
     */
    private void ensureLength() {
        if(length> data.length){
        double[][] tmp = new double[length][];
        System.arraycopy(data,0,tmp,0,data.length);
        for(int j = data.length; j<length;j++)
            tmp[j] = new double[VERTEX_LENGTH];
        data = tmp;
        }
    }

    @Override
    public final int getLength() {
        return length;
    }
    public final void  setLength(int l) {
        length = l;
        ensureLength();
    }
}
