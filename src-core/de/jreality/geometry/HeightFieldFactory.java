/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.geometry;

import java.awt.geom.Rectangle2D;

/**
 * A factory for generating instances of {@link IndexedFaceSet} which
 * are defined by a height field.  Use the {@link de.jreality.geometry.QuadMeshFactory#setVertexCoordinates(double[][])} 
 * or some variation, to set the height field (with one entry per vector).
 * Then use {@link #setRegularDomain(Rectangle2D)} to specify the domain of
 * definition of the height field.  The resulting height field will
 * have <i>(x,y)</i> values given by appropriately interpolated position in the
 * domain, and z-value the appropriate element of the z-array.
 * 
 * <b>Warning</b>: Not all jReality backends can handle such height fields.  JOGL and PORTAL can.
 * @author Charles Gunn
 *
 */public class HeightFieldFactory extends QuadMeshFactory {
	Rectangle2D theDomain = new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0);
	boolean domainHasChanged = true;
	public HeightFieldFactory() {
		super();
	}

	public HeightFieldFactory(int signature, int mMaxU, int mMaxV,
			boolean closeU, boolean closeV) {
		super();
		setSignature(signature);
		setULineCount(mMaxU);
		setVLineCount(mMaxV);
		setClosedInUDirection(closeU);
		setClosedInVDirection(closeV);
	}

	public void setRegularDomain(Rectangle2D r)	{
		theDomain=r;
	}
	
	public Rectangle2D getRegularDomain()	{
		return theDomain;
	}
	
	protected void updateImpl() {
		super.updateImpl();

		if( domainHasChanged)
			ifs.setGeometryAttributes(GeometryUtility.REGULAR_DOMAIN_QUAD_MESH_SHAPE, theDomain);
		
		domainHasChanged= false;
	}

	public static double[] getCoordinatesForUV(double[] store, Rectangle2D d, int u, int v, int uc, int vc)	{
		if (store == null) store = new double[2];
		store[0] = d.getMinX()+d.getWidth()*(u/(uc-1.0));
		store[1] = d.getMinY()+d.getHeight()*(v/(vc-1.0));
		return store;
	}
}
