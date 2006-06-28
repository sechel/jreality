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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class QuadMeshFactory extends AbstractQuadMeshFactory {

	public QuadMeshFactory() {
		super();
	}
	
	public IndexedFaceSet getQuadMesh() {
		return getIndexedFaceSet();
	}
	
	/* vertex attributes */
	
	public void setVertexAttribute( Attribute attr, DataList data ) {
		super.setVertexAttribute( attr, data );
	}
	
	public void setVertexAttributes(DataListSet dls ) {
		super.setVertexAttributes( dls );
	}

	public void setVertexCoordinates( DataList data ) {
		super.setVertexCoordinates(data);
	}
	
	public void setVertexCoordinates( double [] data ) {
		super.setVertexCoordinates( data );
	}
	
	public void setVertexCoordinates( double [][] data ) {
		super.setVertexCoordinates( data );
	}
	
	/**
	 * A convenience method to allow users to work with a rectangular 3D array to describe a quad mesh
	 * @param points a 3-dimension, rectangular array; the first to dimension must equal
	 * the number of v-lines (@link getVLineCount) and u-lines (@link getULineCount).
	 */
	public void setVertexCoordinates(double[][][] points) {
		double[][] npoints = convertDDDtoDD(points);
		setVertexCoordinates(npoints);
	}

	public void setVertexColors(double[][][] cs) {
		double[][] ncs = convertDDDtoDD(cs);
		setVertexColors(ncs);
	}

	/**
	 * @param points
	 * @return
	 */
	private double[][] convertDDDtoDD(double[][][] points) {
		int lengthv = points.length;
		int lengthu = points[0].length;
		setMeshSize(lengthu, lengthv);
		int lengthf = points[0][0].length;
		if (lengthv != getVLineCount() || lengthu != getULineCount() ) {
			throw new IllegalArgumentException("Bad dimension for 3D array");
		}
		double[][] npoints = new double[lengthv * lengthu][points[0][0].length];
		for (int i = 0; i<lengthv; ++i)	{
			for (int j = 0; j<lengthu; ++j)	{
				System.arraycopy(points[i][j], 0, npoints[i*lengthu+j],0,lengthf);
			}
		}
		return npoints;
	}
	
	public void setVertexNormals( DataList data ) {
		super.setVertexNormals(data);
	}
	
	public void setVertexNormals( double [] data ) {
		super.setVertexNormals( data );
	}
	
	public void setVertexNormals( double [][] data ) {
		super.setVertexNormals( data );
	}
	
	public void setVertexColors( DataList data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexColors( double [] data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexColors( double [][] data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexTextureCoordinates( DataList data ) {
		super.setVertexTextureCoordinates( data );
	}
	
	public void setVertexTextureCoordinates( double [] data ) {
		super.setVertexTextureCoordinates( data );
	}
	
	public void setVertexTextureCoordinates( double [][] data ) {
		super.setVertexTextureCoordinates( data );
	}

	public void setVertexLabels( String [] data ) {
		super.setVertexLabels( data );
	}
	/* face attributes */
	
	public void setFaceAttribute( Attribute attr, DataList data) {
		super.setFaceAttribute( attr, data );
	}
	
	public void setFaceAttributes(DataListSet dls ) {
		super.setFaceAttributes(dls);
	}

	public void setFaceNormals( DataList data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceNormals( double [] data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceNormals( double [][] data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceColors( DataList data ) {
		super.setFaceColors(data);
	}
	
	public void setFaceColors( double [] data ) {
		super.setFaceColors(data);
	}
	
	public void setFaceColors( double [][] data ) {
		super.setFaceColors(data);
	}
	
	public void setFaceLabels( String [] data ) {
		super.setFaceLabels( data );
	}
}
