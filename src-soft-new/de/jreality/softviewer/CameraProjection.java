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

public abstract class CameraProjection {

    private double frustumXmin,frustumXmax,frustumYmin,frustumYmax,frustumZmin,frustumZmax;
    
    public CameraProjection() {
        super();
        // TODO Auto-generated constructor stub
    }
    public abstract void perspective(double[] data);

    /**
     * @param w
     */
    public abstract void setWidth(int w);

    /**
     * @param h
     */
    public abstract void setHeight(int h);
    public double getFrustumXmax() {
        return frustumXmax;
    }
    public void setFrustumXmax(double frustumXmax) {
        this.frustumXmax = frustumXmax;
    }
    public double getFrustumXmin() {
        return frustumXmin;
    }
    public void setFrustumXmin(double frustumXmin) {
        this.frustumXmin = frustumXmin;
    }
    public double getFrustumYmax() {
        return frustumYmax;
    }
    public void setFrustumYmax(double frustumYmax) {
        this.frustumYmax = frustumYmax;
    }
    public double getFrustumYmin() {
        return frustumYmin;
    }
    public void setFrustumYmin(double frustumYmin) {
        this.frustumYmin = frustumYmin;
    }
    public double getFrustumZmax() {
        return frustumZmax;
    }
    public void setFrustumZmax(double frustumZmax) {
        this.frustumZmax = frustumZmax;
    }
    public double getFrustumZmin() {
        return frustumZmin;
    }
    public void setFrustumZmin(double frustumZmin) {
        this.frustumZmin = frustumZmin;
    }
    
}
