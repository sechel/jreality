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
public class SpotLightSoft extends DirectionalLightSoft {

	protected double[] source = new double[3];

	protected double a0 = 1;
	protected double a1 = 0;
	protected double a2 = 0;
	protected double cosConeAngle = Math.cos(Math.PI/6.);
	protected double softEdgeFraction = 1/3.;

	/**
	 * 
	 */
	public SpotLightSoft(double r, double g, double b, double i, double[] dir, double[] src, double coneAngle, double coneDeltaAngle,double a0,double a1, double a2) {
		super(r,g,b,i,dir);
		source = src;
		this.a0 = a0;
		this.a1 = a1;
		this.a2 = a2;
        cosConeAngle = Math.cos(coneAngle);
        softEdgeFraction = coneDeltaAngle/coneAngle;
	}

}
