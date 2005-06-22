/*
 * Created on 09.06.2005
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
package de.jreality.soft;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class HatchTexture implements Texture {
    double scaleU = .02;
    double scaleV = .02;
    public final int transparency = 255;
    /**
     * 
     */
    public HatchTexture() {
        super();
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.Texture#getColor(double, double, int[])
     */
    public void getColor(double u, double v,int x, int y, int[] color) {
        int value  = (color[0] + color[1] + color[2])/3;

        if(value>50) {
            color[0] = color[1] = color[2] = 255;
            color[3] = transparency;
            return;
        }
        
        int m = value >20? 4:(value>5? 2:1);
        int iu = ((int) ((x+y)/4))%m;
        //int iu = ((int) ((v/scaleV)+(u/scaleU)))%m;
        if(iu ==0 ) {
            color[0] = color[1] = color[2] = 0;
            color[3] = 255;
        } else {
            color[0] = color[1] = color[2] = 255;
            color[3] = transparency;
        }

    }

}
