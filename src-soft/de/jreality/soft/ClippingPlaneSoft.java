/*
 * Created on Dec 5, 2003
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
package de.jreality.soft;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class ClippingPlaneSoft  {
    protected double[] normal = new double[3];
	protected double[] point = new double[3];

	public ClippingPlaneSoft( double[] dir, double[] src) {
		normal = dir;
		point = src;
	}

    /**
     * @return Returns the normal.
     */
    public double[] getNormal() {
        return normal;
    }

    /**
     * @param normal The normal to set.
     */
    public void setNormal(double[] normal) {
        this.normal = normal;
    }

    /**
     * @return Returns the point.
     */
    public double[] getPoint() {
        return point;
    }

    /**
     * @param point The point to set.
     */
    public void setPoint(double[] point) {
        this.point = point;
    }

}
