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

import java.awt.Dimension;
import java.util.logging.Level;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.util.LoggingSystem;

/**
 * 
 * @author schmies
 *
 */
class AbstractQuadMeshFactory extends AbstractIndexedFaceSetFactory {
	
	final OoNode textureCoordinates = node( "vertex.texture" );
	
	final OoNode closedInVDirection = node( new Boolean(false),"closed in v" );
	final OoNode closedInUDirection = node( new Boolean(false),"closed in u" );

	final OoNode vLineCount = node( new Integer(-1), "v-line count" );  //unfortunately we need the -1
	final OoNode uLineCount = node( new Integer(-1), "u-line count" );
	
	private int uLineCount_;
	
	boolean generateTextureCoordinates = false;
	
	AbstractQuadMeshFactory() {
		this( Pn.EUCLIDEAN, 10, 10, false, false );
	}
	
	AbstractQuadMeshFactory(IndexedFaceSet existing) {
		this( existing, Pn.EUCLIDEAN, 10, 10, false, false );
	}
	
	// don't scare away the customers with the metric thing...
	AbstractQuadMeshFactory( int mMaxU, int mMaxV, boolean closeU, boolean closeV ) {
		this(Pn.EUCLIDEAN, mMaxU, mMaxV, closeU, closeV);
	}
	
	AbstractQuadMeshFactory( int signature, int mMaxU, int mMaxV, boolean closeU, boolean closeV ) {
		this(new IndexedFaceSet(), signature, mMaxU, mMaxV, closeU, closeV);
	}
	
	AbstractQuadMeshFactory( IndexedFaceSet existing, int signature, int mMaxU, int mMaxV, boolean closeU, boolean closeV ) {
		super(existing == null ? new IndexedFaceSet() : existing, signature );
		
		setMeshSize( mMaxU, mMaxV );

		setClosedInUDirection(closeU);
		setClosedInVDirection(closeV);
		
		generateTextureCoordinates = true;
	}
	
	void setMeshSize(int maxU2, int maxV2) {
		if( maxU2 < 2 || maxV2 < 2 )
			throw new IllegalArgumentException( "line count must be bigger then 1" );
		
		if( maxU2 == getULineCount() && maxV2 == getVLineCount() )
			return;
		
		uLineCount.setObject(new Integer(maxU2));  uLineCount_ = maxU2; // ugly, but we need this for fast access
		vLineCount.setObject(new Integer(maxV2));
			
		super.setVertexCount( getULineCount()*getVLineCount());
		super.setFaceCount( (getULineCount()-1)*(getVLineCount()-1) );

	}

	public void setVertexCount( int count ) {
		throw new UnsupportedOperationException();
	}
	
	public void setFaceCount( int count ) {
		throw new UnsupportedOperationException();
	}
	
	public void setFaceAttribute( Attribute attr, DataList data ) {

		if( attr == Attribute.INDICES ) {			
			throw new UnsupportedOperationException( "cannot set indices of a quad mesh");
		}
		
		super.setFaceAttribute( attr, data );
	}
	
	IntArrayArray generateEdgeIndices() {
		int uLineCount = getULineCount();
		int vLineCount = getVLineCount();
		
		int sizeUCurve = vLineCount; //isClosedInVDirection() ? vLineCount+1 : vLineCount;		// a "u-curve" has a fixed u-value
		int sizeVCurve = uLineCount; //isClosedInUDirection() ? uLineCount+1 : uLineCount;
		int numVCurves = sizeUCurve;
		int numUCurves = sizeVCurve;
		int numVerts = getULineCount()*getVLineCount();
		int[][] indices = new int[numUCurves +numVCurves][];
		for (int i = 0; i<numUCurves; ++i)	{
			indices[i] = new int[sizeUCurve];
			for (int j = 0; j< sizeUCurve; ++j)	  indices[i][j] = ((j)*uLineCount + (i%uLineCount))%numVerts;
		}
		for (int i = 0; i<numVCurves; ++i)	{
			indices[i+numUCurves] = new int[sizeVCurve];
			for (int j = 0; j< sizeVCurve; ++j)	  indices[i+numUCurves][j] = (i*uLineCount + (j%uLineCount))%numVerts;
		}	
		return new IntArrayArray.Array(indices);
	}
	
	{
		faceIndices.addIngr( (OoNode)face.attributeNode.get( Attribute.INDICES )); //allready in superclass
		faceIndices.addIngr( uLineCount );
		faceIndices.addIngr( vLineCount );
		faceIndices.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {		
						return generateFaceIndices( (int[][])object);					
					}					
				}
		);
	}
	
	int [][] generateFaceIndices( int [][] faceIndices ) {
			
		final int uLineCount = getULineCount();
		final int vLineCount = getVLineCount();
	
		log("compute", Attribute.INDICES, "face");
		if( faceIndices == null || nof() != faceIndices.length )
			faceIndices = new int[nof()][4];
		
		final int numUFaces = uLineCount-1;
		final int numVFaces = vLineCount-1;
		
		final int numPoints = nov();
		
		for (int i = 0, k=0; i<numVFaces; ++i) {
			for (int j = 0; j< numUFaces; ++j, k++)	{
				final int [] face = faceIndices[k];
				face[0] = (i * uLineCount + j);
				face[1] = (((i+1) * uLineCount) + j) % numPoints;
				face[2] = (((i+1) * uLineCount) + (j+1)%uLineCount) % numPoints;
				face[3] = ((i* uLineCount) + (j+1)%uLineCount) % numPoints;				
			}
		}

		return faceIndices;
	}
	
	public int getULineCount() {
		return ((Integer)uLineCount.getObject()).intValue();
	}
	
	public int getVLineCount() {
		return ((Integer)vLineCount.getObject()).intValue();
	}
	
	public void setULineCount(int newU) {
		setMeshSize( newU, getVLineCount() );
	}
	
	public void setVLineCount(int newV) {
		setMeshSize( getULineCount(), newV );
	}
	
	public boolean isClosedInUDirection() {
		return ((Boolean)closedInUDirection.getObject()).booleanValue();
	}
	
	public boolean isClosedInVDirection() {
		return ((Boolean)closedInVDirection.getObject()).booleanValue();
	}
	
	public void setClosedInUDirection(boolean close) {
		closedInUDirection.setObject( new Boolean(close));
	}
	
	public void setClosedInVDirection(boolean close) {
		closedInVDirection.setObject( new Boolean(close));
	}

	{
		textureCoordinates.addIngr( vertex.attributeNode( Attribute.TEXTURE_COORDINATES ));
		textureCoordinates.addIngr( uLineCount );
		textureCoordinates.addIngr( vLineCount );
		textureCoordinates.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateTextureCoordinates( (double[])object);					
					}					
				}
		);
	}
	
	double [] textureCoordinates() {
		return (double[])(textureCoordinates.getObject());
	}
	
	double [] generateTextureCoordinates( double [] textureCoordinates ) {
		
		if( vertex.DLS.containsAttribute(Attribute.TEXTURE_COORDINATES))
			return null;
		
		if( textureCoordinates == null || textureCoordinates.length != 2*nov() )
			textureCoordinates = new double[2*nov()];
		
		final int vLineCount = getVLineCount();
		final int uLineCount = getULineCount();
		
		final double dv= 1.0 / (vLineCount - 1);
		final double du= 1.0 / (uLineCount - 1);
		
		double v=0;
		for(int iv=0, firstIndexInULine=0;
		iv < vLineCount;
		iv++, v+=dv, firstIndexInULine+=uLineCount) {
			double u=0;
			for(int iu=0; iu < uLineCount; iu++, u+=du) {
				final int indexOfUV=firstIndexInULine + iu;
				textureCoordinates[2*indexOfUV+0] = u;
				textureCoordinates[2*indexOfUV+1] = v;
			}
		}
				
		return textureCoordinates;
	}

	void recompute() {
		
		super.recompute();
		
		if( generateTextureCoordinates ) {
			textureCoordinates.update();
		}
	}
	
	protected void updateImpl() {
	
		super.updateImpl();

		if( nodeWasUpdated( faceIndices) ) { 
			log( "set", Attribute.INDICES, "face");
			ifs.setFaceAttributes( Attribute.INDICES, 
					new IntArrayArray.Array( faceIndices() ) );
		}
		
		if( !vertex.DLS.containsAttribute(Attribute.TEXTURE_COORDINATES) ) {
			if( generateTextureCoordinates ) {
				if( nodeWasUpdated(textureCoordinates) ) { 
					log( "set", Attribute.TEXTURE_COORDINATES, "vertex");
					ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, 
							new DoubleArrayArray.Inlined(textureCoordinates(),2));
				}
			} else {
				if( ifs.getVertexAttributes().containsAttribute(Attribute.TEXTURE_COORDINATES) ) {
					log( "cancle", Attribute.TEXTURE_COORDINATES, "vertex");
					ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, null);
				}
			}
		}
		
		if( nodeWasUpdated(uLineCount)|| nodeWasUpdated(vLineCount) ) {
			log( "set", GeometryUtility.QUAD_MESH_SHAPE, "vertex");
			ifs.setGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE, new Dimension( getULineCount(), getVLineCount() ));
		}
	}

	public boolean isGenerateTextureCoordinates() {
		return generateTextureCoordinates;
	}

	public void setGenerateTextureCoordinates(boolean generateTextureCoordinates) {
		this.generateTextureCoordinates = generateTextureCoordinates;
	}

	final private int index( int u, int v ) {
		return u + uLineCount_ * v;
	}
	
	void average( double [] x, double [] y ) {
		Rn.add(x,x,y);
		Rn.times(x,2,x); //unnecessary because we normalize later
		Rn.copy(y,x);
	}
	
	void normalize( double [] x ) {
		Rn.normalize(x,x);
	}
	
	{
		vertexNormals.addIngr(closedInUDirection);
		vertexNormals.addIngr(closedInVDirection);
	}
	
	double [][] generateVertexNormals( double [][] vertexNormals ) {
		vertexNormals = super.generateVertexNormals( vertexNormals );
		
		if( !isClosedInUDirection() && !isClosedInVDirection() )
			return vertexNormals;
		
		if( getSignature() != Pn.EUCLIDEAN )
			LoggingSystem.getLogger(this).log(Level.WARNING, 
					"currently only eucledian normals used for smoothing");
		
		if( isClosedInUDirection() ) {		
			final int last = getULineCount()-1;		
			for( int i=0; i<getVLineCount(); i++ ) {
				average( vertexNormals[index(0,i)], vertexNormals[index(last,i)]);
			}
		}
		
		if( isClosedInVDirection() ) {		
			final int last = getVLineCount()-1;		
			for( int i=0; i<getULineCount(); i++ ) {
				average( vertexNormals[index(i,0)], vertexNormals[index(i,last)]);
				normalize(vertexNormals[index(i,0)]);
				normalize(vertexNormals[index(i,last)]);
			}
		}
		
		if( isClosedInUDirection() ) {		
			final int last = getULineCount()-1;		
			for( int i=0; i<getVLineCount(); i++ ) {
				normalize( vertexNormals[index(0,i)]);
				normalize( vertexNormals[index(last,i)]);
			}
		}
		
		return vertexNormals;
	}

}
