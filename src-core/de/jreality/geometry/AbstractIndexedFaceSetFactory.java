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
import java.awt.Color;

import de.jreality.geometry.OoNode.UpdateMethod;
import de.jreality.math.Pn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.AABBTree;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.PickUtility;

class AbstractIndexedFaceSetFactory extends AbstractIndexedLineSetFactory {
	
	final IndexedFaceSet ifs;
	
	GeometryAttributeListSet face = new GeometryAttributeListSet( this, Geometry.CATEGORY_FACE );
	
	OoNode faceCount = node( "faceCount", Integer.class, 0 );
	OoNode aabbTree  = node( "aabbTree", AABBTree.class, null );
	
	boolean generateAABBTree = false;
	
	
	AttributeGenerator vertexCoordinates = attributeGeneratorNode( vertex, double[][].class, Attribute.COORDINATES);
	AttributeGenerator vertexNormals     = attributeGeneratorNode( vertex, double[][].class, Attribute.NORMALS );
	
	AttributeGenerator edgeIndices       = attributeGeneratorNode( edge, int[][].class,      Attribute.INDICES );
	
	AttributeGenerator faceIndices       = attributeGeneratorNode( face, int[][].class,      Attribute.INDICES );
	AttributeGenerator faceLabels        = attributeGeneratorNode( face, String[].class,     Attribute.LABELS );
	AttributeGenerator faceNormals       = attributeGeneratorNode( face, double[][].class,   Attribute.NORMALS );

	AbstractIndexedFaceSetFactory( IndexedFaceSet ifs, int signature, boolean generateEdgesFromFaces, boolean generateVertexNormals, boolean generateFaceNormals ) {
		super( ifs, signature );	

		this.ifs = ifs;
		
		setGenerateEdgesFromFaces( generateEdgesFromFaces);
		setGenerateFaceNormals( generateVertexNormals );
		setGenerateFaceNormals( generateFaceNormals );
	}
	
	AbstractIndexedFaceSetFactory( int signature, boolean generateEdgesFromFaces, boolean generateVertexNormals, boolean generateFaceNormals ) {
		this( new IndexedFaceSet(0,0), signature, generateEdgesFromFaces, generateVertexNormals, generateFaceNormals );	
	}
	
	AbstractIndexedFaceSetFactory( IndexedFaceSet existing, int signature ) {
		this(existing,  signature, false, false, false );
	}
	
	AbstractIndexedFaceSetFactory( int signature ) {
		this( signature, false, false, false );
	}
	
	AbstractIndexedFaceSetFactory(IndexedFaceSet existing) {
		this(  existing, Pn.EUCLIDEAN, false, false, false );
	}
	
	public AbstractIndexedFaceSetFactory() {
		this( Pn.EUCLIDEAN );
	}
	
	protected int nof(){
		return (Integer)faceCount.getObject();
	}
	
	protected int getFaceCount() {
		return nof();
	}
	
	void setFaceCount( int count ) {
		face.setCount( count );
		faceCount.setObject(new Integer(count));
	}
	
	protected void setFaceAttribute( Attribute attr, DataList data ) {
		face.setAttribute(attr, data);
	}
	
	protected void setFaceAttributes(DataListSet dls ) {
		face.setAttributes(dls);
	}
	
	protected void setFaceIndices( DataList data ) {
		setFaceAttribute( Attribute.INDICES, data );
	}
	
	protected void setFaceIndices( int[][] data ) {
		setFaceAttribute( Attribute.INDICES, new IntArrayArray.Array( data ) );
	}
	
	protected void setFaceIndices( int[] data, int pointCountPerFace ) {
		if( data != null && data.length != pointCountPerFace * nof() )
			throw new IllegalArgumentException( "array has wrong length" );
		setFaceAttribute( Attribute.INDICES, data==null ? null : new IntArrayArray.Inlined( data, pointCountPerFace ) );
	}
	
	protected void setFaceIndices( int[] data ) {
		setFaceIndices( data, 3 );
	}
	
	protected void setFaceNormals( DataList data ) {
		setFaceAttribute( Attribute.NORMALS, data );
	}
	
	protected void setFaceNormals( double [] data ) {
		if( data != null && data.length % nof() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );	
		setFaceAttribute( Attribute.NORMALS, data != null ? new DoubleArrayArray.Inlined( data, data.length / nof() ) : null );
	}
	
	protected void setFaceNormals( double [][] data ) {
		setFaceAttribute( Attribute.NORMALS, new DoubleArrayArray.Array( data ) );
	}
	
	protected void setFaceColors( DataList data ) {
		setFaceAttribute( Attribute.COLORS, data );
	}
	
	protected void setFaceColors( double [] data ) {
		if( data != null && data.length % nof() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );	
		setFaceAttribute( Attribute.COLORS, data==null ? null : new DoubleArrayArray.Inlined( data, data.length / nof() ) );
	}
	
	protected void setFaceColors( Color [] colors ) {
    double[] data = new double[colors.length*4];
    float[] col = new float[4];
    for (int i = 0; i < colors.length; i++) {
      colors[i].getRGBComponents(col);
      data[4*i  ] = col[0];
      data[4*i+1] = col[1];
      data[4*i+2] = col[2];
      data[4*i+3] = col[3];
    }
    setFaceAttribute( Attribute.COLORS, new DoubleArrayArray.Inlined( data, 4 ) );
	}
	
	protected void setFaceColors( double [][] data ) {
		setFaceAttribute( Attribute.COLORS, new DoubleArrayArray.Array( data ) );
	}


	protected void setFaceLabels( DataList data ) {
		setVertexAttribute( Attribute.LABELS, data );
	}
	
	protected void setFaceLabels( String[] data ) {
		if( data != null && data.length != nof() )
			throw new IllegalArgumentException( "array has wrong length" );
		setFaceAttribute( Attribute.LABELS, data==null ? null : new StringArray(data));
	}
	
	{
		faceLabels.addIngr(faceCount);
		faceLabels.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return indexString(nof());
					}					
				}
		);
	}

	{
		aabbTree.addIngr(vertexCoordinates);
		aabbTree.addIngr(faceIndices);
		aabbTree.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return isGenerateAABBTree() ? AABBTree.construct((double[][])vertexCoordinates.getObject(), (int[][]) faceIndices.getObject()): null;
					}					
				});
	}

	{
		edgeIndices.addIngr( faceIndices );
		if( isGenerateEdgesFromFaces() )
			edgeIndices.addDeps( edgeCount );
		edgeIndices.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateEdgeIndices();		
					}					
				}
		);
				
	}
	{
		edgeCount.setUpdateMethod( new UpdateMethod() {

			public Object update(Object object) {
				int count;
				if(edgeIndices.isGenerate())
					count = ((int[][])edgeIndices.getObject()).length;
				else 
					count = edge.DLS.containsAttribute(Attribute.INDICES) ? edge.DLS.getListLength() : 0;
				
				return new Integer(count);	
			}
			
		});
	}

	/* overwrite in subclass. */
	int [][] generateEdgeIndices() {
		int [][] fi = (int[][])faceIndices.getObject();
		if( fi==null)
			return null;
		return IndexedFaceSetUtility.edgesFromFaces( fi ).toIntArrayArray(null);
	}
	
	{
		faceNormals.addIngr(signature);
		faceNormals.addIngr(faceIndices);
		faceNormals.addIngr(vertexCoordinates);
		faceNormals.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateFaceNormals( (double[][])object);		
					}					
				}
		);
	}
	

	
	/* overwrite in subclass. */
	double [][] generateFaceNormals( double [][] faceNormals ) {
		int    [][] fi = (int   [][])faceIndices.      getObject();
		double [][] vc = (double[][])vertexCoordinates.getObject();

		if( fi==null || vc==null )
			return null;
		
		log( "compute", Attribute.NORMALS, "face");
	
		return IndexedFaceSetUtility.calculateFaceNormals( fi, vc, getSignature() );
		
	}
	
	{
		vertexNormals.addIngr(signature);
		vertexNormals.addIngr(faceNormals);
		
		vertexNormals.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateVertexNormals( (double[][])object);		
					}					
				}
		);
	}
	
	/* overwrite in subclass. */
	double [][] generateVertexNormals( double [][] vertexNormals ) {
		int    [][] fi = (int   [][])faceIndices.      getObject();
		double [][] vc = (double[][])vertexCoordinates.getObject();
		double [][] fn = (double[][])faceNormals.      getObject();
		
		if( fi==null || vc==null  )
			return null;
		
		if( fn==null ) { 
			fn = IndexedFaceSetUtility.calculateFaceNormals( fi, vc, getSignature() );
		}
		
		return IndexedFaceSetUtility.calculateVertexNormals( fi, vc, fn, getSignature() );
		
	}
	
	void recompute() {		
			
		super.recompute();
			
		aabbTree.update();
		
		faceLabels.update();
		edgeIndices.update();
		faceNormals.update();
		vertexNormals.update();
		
	}

	protected void updateImpl() {
	 		
		super.updateImpl();
		
		if( ifs.getNumFaces() != getFaceCount() )
			ifs.setNumFaces( getFaceCount() );
		
		updateGeometryAttributeCathegory( face );
		
		if( nodeWasUpdated(aabbTree))
			ifs.setGeometryAttributes(PickUtility.AABB_TREE, aabbTree.getObject());
		
		edgeIndices.updateArray();
		faceLabels.updateArray();
		faceNormals.updateArray();
		vertexNormals.updateArray();
	}

	public IndexedFaceSet getIndexedFaceSet() {
		return ifs;
	}
	
	public boolean isGenerateEdgesFromFaces() {
		edgeIndices.outdate();
		return edgeIndices.isGenerate();
	}

	public void setGenerateEdgesFromFaces(boolean generateEdgesFromFaces) {
		if( generateEdgesFromFaces && edge.hasEntries() )
			throw new UnsupportedOperationException( 
					"you cannot generate edges form faces " +
					"while edge attributes are set." +
					"use clearEdgeAttributes() before");
			
		edgeIndices.setGenerate(generateEdgesFromFaces);
		
		if( isGenerateEdgesFromFaces() ) {
			edgeIndices.addDeps( edgeCount );
		} else {
			edgeIndices.removeDeps( edgeCount );
		}
		edge.blockAttributeCount = generateEdgesFromFaces;
	}
	
	public boolean isGenerateVertexNormals() {
		return vertexNormals.isGenerate();
	}

	public void setGenerateVertexNormals(boolean generateVertexNormals) {
		vertexNormals.setGenerate(generateVertexNormals);
	}
	
	public boolean isGenerateFaceNormals() {
		return faceNormals.isGenerate();
	}

	public void setGenerateFaceNormals(boolean generateFaceNormals) {
		faceNormals.setGenerate(generateFaceNormals);
	}

	public boolean isGenerateFaceLabels() {
		return faceLabels.isGenerate();
	}

	public void setGenerateFaceLabels(boolean generateFaceLabels) {
		faceLabels.setGenerate(generateFaceLabels);
	}

	public boolean isGenerateAABBTree() {
		return generateAABBTree;
	}
	
	public void setGenerateAABBTree( boolean generate ) {
		if( generateAABBTree==generate)
			return;
		
		aabbTree.outdate();
		
		generateAABBTree = generate;
		
		//TODO:
	}
}
