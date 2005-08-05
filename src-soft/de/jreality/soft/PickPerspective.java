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

import de.jreality.math.Rn;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class PickPerspective implements Perspective  {

	private double fieldOfView =  60. / 180.* Math.PI;
	private double focalLength = 	1. /Math.tan(15. / 360. * Math.PI);
	//TODO: customize This
	private int width= 640;
	private int height = 480;
	private double wh = width/2.;
	private double hh = height/2.;
    private double mh = Math.min(wh,hh);
	private double nearclip = 3;
	private double farclip = 50;

	ClippingBox frustum = new ClippingBox();
    private double pickX;
    private double halfPixelWidth;
    private double pickY;
	/**
	 * 
	 */
	public PickPerspective() {
		super();
		frustum.z0 = -1;
		frustum.z1 = 1;
		frustum.x0 = -1;
        frustum.x1 = 1;
        frustum.y0 = -1;
        frustum.y1 = 1;
	}

	/* (non-Javadoc)
	 * @see de.jreality.soft.Perspective#perspective(double[], int)
	 */
	public final void perspective(final double[] v, final int pos) {

		double vz = (-v[Polygon.SZ+pos]);
              
        v[pos+Polygon.SX] *= ( focalLength );
        v[pos+Polygon.SY] *= ( focalLength );
        v[pos+Polygon.SZ] = ( (nearclip + farclip)*vz -(2* nearclip*farclip)*v[pos+Polygon.SW] )/(farclip - nearclip );
        v[pos+Polygon.SW] *= vz;
        
        v[pos+Polygon.SX] = (v[pos+Polygon.SX] - pickX*v[pos+Polygon.SW])*halfPixelWidth;
        v[pos+Polygon.SY] = (v[pos+Polygon.SY] - pickY*v[pos+Polygon.SW])*halfPixelWidth;

	}

    
	public double[] getInverseMatrix(double[]dst) {
	    if(dst ==null) dst = new double[16];
	    Rn.setIdentityMatrix(dst);
	    dst[ 0] =  1/(focalLength*halfPixelWidth);
        dst[ 3] =  pickX/focalLength;
        dst[ 5] =  1/(focalLength*halfPixelWidth);
        dst[ 7] =  pickY/focalLength;
        dst[10] =  0;
        dst[11] = -1;
        dst[14] = -(farclip - nearclip)/(2 * farclip * nearclip);
        dst[15] =  (farclip + nearclip)/(2 * farclip * nearclip);
        return dst;
    }
	/**
	 * @return double
	 */
	public double getFieldOfView() {
		return fieldOfView;
	}

	/**
	 * @return double
	 */
	public double getFocalLength() {
		return focalLength;
	}

	/**
	 * Sets the fieldOfView in degrees.
	 * @param fieldOfView The fieldOfView to set
	 */
	public void setFieldOfViewDeg(double fieldOfView) {
		this.fieldOfView = fieldOfView / 180.* Math.PI;
        this.focalLength = /*(width/(double)height)*/ 1/Math.tan(this.fieldOfView/2);
		//TODO fix this: fieldOfView <-> focalLength
	}

	/**
	 * Sets the focalLength.
	 * @param focalLength The focalLength to set
	 */
	public void setFocalLength(double focalLength) {
		this.focalLength = focalLength;
		//TODO fix this: fieldOfView <-> focalLength
	}

	/**
	 * @return int
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return int
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the height.
	 * @param height The height to set
	 */
	public void setHeight(int height) {
		this.height = height;
		this.hh = height/2.;
		//frustum.y1 = height;
        mh =Math.min(wh,hh);
        halfPixelWidth = (4.*mh);
	}

	/**
	 * Sets the width.
	 * @param width The width to set
	 */
	public void setWidth(int width) {
		this.width = width;
		this.wh = width/2.;
		//frustum.x1 = width;
        mh =Math.min(wh,hh);
        halfPixelWidth = (4.*mh);
        
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.Perspective#getFrustum()
     */
    public ClippingBox getFrustum() {
        // TODO Auto-generated method stub
        return frustum;
    }

    public void setNear(double d) {
        nearclip =d; 
        
    }

    
    public void setFar(double d) {
        farclip =d;
        
    }
    public void setPickPoint(int x, int y) {
        pickX = (x-wh)/(double)mh;
        pickY = ((height-y)-hh)/(double)mh;
    }

}
