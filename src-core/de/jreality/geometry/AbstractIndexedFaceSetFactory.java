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

// TODO:  no support for setting edge attributes

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import de.jreality.math.Pn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;

class AbstractIndexedFaceSetFactory extends AbstractIndexedLineSetFactory {
	
	final OoNode faceNormals = new OoNode( "face.normals" );
	final OoNode faceIndices = new OoNode( "face.indices" );
	final OoNode faceLabels  = new OoNode( "face.labels" );

	final OoNode edgeIndices = new OoNode( "edge.indices" );
	
	final OoNode vertexCoordinates = new OoNode( "vertex.coordinates" );
	final OoNode vertexNormals     = new OoNode( "vertex.normals" );

	
	boolean generateVertexNormals  = false;
	boolean generateFaceNormals    = false;
	boolean generateFaceLabels	   = false;
	boolean generateEdgesFromFaces = false;
	
	DataListSet faceDLS = new DataListSet(0);

	final IndexedFaceSet ifs;
	
	HashMap faceAttributeNode = new HashMap();
	
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
		return faceDLS.getListLength();
	}
	
	public int getFaceCount() {
		return nof();
	}
	
	void setFaceCount( int count ) {
		if( count == nof() )
			return;
	
		faceDLS.reset(count);
	}
	
	OoNode faceAttributeNode( Attribute attr ) {
		return this.geometryAttributeNode( faceAttributeNode, "FACE", attr );
	}
	
	void updateFaceAttributes() {
		for( Iterator iter = faceDLS.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			
			faceAttributeNode( attr ).update();	
		}
	}
	
	protected void setFaceAttribute( Attribute attr, DataList data ) {
		setAttrImpl( faceDLS, attr, data );
		faceAttributeNode(attr).setObject( data );
	}
	
	protected void setFaceAttributes(DataListSet dls ) {
		faceDLS = dls;	
		for( Iterator iter = dls.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			faceAttributeNode(attr).setObject( dls.getList(attr));
		}
	}
	
	protected void setFaceIndices( DataList data ) {
		setFaceAttribute( Attribute.INDICES, data );
	}
	
	protected void setFaceIndices( int[][] data ) {
		setFaceAttribute( Attribute.INDICES, new IntArrayArray.Array( data ) );
	}
	
	protected void setFaceIndices( int[] data, int pointCountPerFace ) {
		if( data.length != pointCountPerFace * nof() )
			throw new IllegalArgumentException( "array has wrong length" );
		setFaceAttribute( Attribute.INDICES, new IntArrayArray.Inlined( data, pointCountPerFace ) );
	}
	
	protected void setFaceIndices( int[] data ) {
		setFaceIndices( data, 3 );
	}
	
	protected void setFaceNormals( DataList data ) {
		setFaceAttribute( Attribute.NORMALS, data );
	}
	
	protected void setFaceNormals( double [] data ) {
		if( data.length % nof() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );	
		setFaceAttribute( Attribute.NORMALS, new DoubleArrayArray.Inlined( data, data.length / nof() ) );
	}
	
	protected void setFaceNormals( double [][] data ) {
		setFaceAttribute( Attribute.NORMALS, new DoubleArrayArray.Array( data ) );
	}
	
	protected void setFaceColors( DataList data ) {
		setFaceAttribute( Attribute.COLORS, data );
	}
	
	protected void setFaceColors( double [] data ) {
		if( data.length % nof() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );	
		setFaceAttribute( Attribute.COLORS, new DoubleArrayArray.Inlined( data, data.length / nof() ) );
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
		if( data.length != nof() )
			throw new IllegalArgumentException( "array has wrong length" );
		setFaceAttribute( Attribute.LABELS, new StringArray(data));
	}
	

	String [] faceLabels() {
		return (String[])faceLabels.getObject();
	}
	
	String [] generateFaceLabels() {
		if( faceDLS.containsAttribute(Attribute.LABELS)) {
			return faceDLS.getList(Attribute.LABELS)
			.toStringArray((String[])faceLabels.getObject());
		} else {
			log( "compute", Attribute.LABELS, "face");
			return indexString(nof());
		}
	}
	

	{
		faceLabels.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateFaceLabels();		
					}					
				}
		);
	}
	{
		faceIndices.addIngr( faceAttributeNode( Attribute.INDICES ) );
		faceIndices.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {	
						return faceDLS.getList(Attribute.INDICES)
							.toIntArrayArray(null);					
					}					
				}
		);
	}
	
	int [][] faceIndices() {
		return (int[][])(faceIndices.getObject());
	}
	
	{
		
		vertexCoordinates.addIngr( vertexAttributeNode( Attribute.COORDINATES)) ;
		vertexCoordinates.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return vertexDLS.getList(Attribute.COORDINATES)
							.toDoubleArrayArray(null);			
					}					
				}
		);
	}
	
	double [][] vertexCoordinates() {
		return (double[][])vertexCoordinates.getObject();
	}
	
	{
		edgeIndices.addIngr( faceIndices );
		edgeIndices.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateEdgeIndices();		
					}					
				}
		);
				
	}
		
	IntArrayArray edgeIndices() {
		return (IntArrayArray)edgeIndices.getObject();
	}
	
	/* overwrite in subclass. */
	IntArrayArray generateEdgeIndices() {
		return IndexedFaceSetUtility.edgesFromFaces( faceIndices() );
	}
	
	{
		faceNormals.addIngr(signature);
		faceNormals.addIngr(faceIndices);
		faceNormals.addIngr(vertexCoordinates);
		faceNormals.addIngr(faceAttributeNode( Attribute.NORMALS ));
		faceNormals.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateFaceNormals();		
					}					
				}
		);
	}
	
	double [][] faceNormals() {
		return (double[][])faceNormals.getObject();
	}
	
	double [][] generateFaceNormals() {
		if( faceDLS.containsAttribute(Attribute.NORMALS)) {
			return faceDLS.getList(Attribute.NORMALS)
			.toDoubleArrayArray((double[][])faceNormals.getObject());
		} else {
			log( "compute", Attribute.NORMALS, "face");
			return GeometryUtility.calculateFaceNormals( faceIndices(), vertexCoordinates(), getSignature() );
		}
	}
	
	
	{
		vertexNormals.addIngr(faceNormals);
		
		vertexNormals.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateVertexNormals();		
					}					
				}
		);
	}
	

	double [][] vertexNormals() {
		return (double[][])vertexNormals.getObject();
	}
	
	double [][] generateVertexNormals() {
		if( vertexDLS.containsAttribute(Attribute.NORMALS)) {
			return null;
		} else {
			log( "compute", Attribute.NORMALS, "vertex" );
			return GeometryUtility.calculateVertexNormals( faceIndices(), vertexCoordinates(), faceNormals(), getSignature() );
		}
	}
	
	
	void recompute() {		
			
		super.recompute();
		
		if( isGenerateFaceLabels() )
			faceLabels.update();
		
		if( isGenerateEdgesFromFaces() ) 
			edgeIndices.update();
		
		if( isGenerateFaceNormals() )
			faceNormals.update();
		
		if( isGenerateVertexNormals() )
			vertexNormals.update();
		
	}
	
	protected void updateImpl() {
		super.updateImpl();
		
		if( ifs.getNumFaces() == nof() ) {

			for( Iterator iter = faceDLS.storedAttributes().iterator(); iter.hasNext(); ) {
				Attribute attr = (Attribute)iter.next();
				
				faceAttributeNode( attr ).update();
				
				if(  nodeWasUpdated(faceAttributeNode( attr ))  ) {
					log( "set", attr, "face" );
					ifs.setVertexAttributes( attr, vertexDLS.getWritableList(attr));			
				}				
			}
		} else {
			updateFaceAttributes();
			ifs.setFaceCountAndAttributes(faceDLS);
		}
	
		if( !edgeDLS.containsAttribute(Attribute.INDICES) ) {
			if( generateEdgesFromFaces ) { 
				if( nodeWasUpdated(edgeIndices) ) { 
					log( "set", Attribute.INDICES, "edge");
					ifs.setEdgeCountAndAttributes(Attribute.INDICES, edgeIndices() );
				} else if( nof() == 0 ) {
					ifs.setNumEdges(0);
				}
			} else if( ifs.getEdgeAttributes().containsAttribute(Attribute.INDICES) ) {
				log( "cancle", Attribute.INDICES, "edge");
				ifs.setEdgeAttributes(Attribute.INDICES, null );
			}
		}
		
		if( !faceDLS.containsAttribute(Attribute.NORMALS) ) {
			if( generateFaceNormals ) {
				if( nodeWasUpdated( faceNormals ) ) { 
					log( "set", Attribute.NORMALS, "face");
					ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(faceNormals()[0].length).createReadOnly(faceNormals()));
				}
			} else {
				if( ifs.getFaceAttributes().containsAttribute(Attribute.NORMALS) ) {
					log( "cancle", Attribute.NORMALS, "face");
					ifs.setFaceAttributes(Attribute.NORMALS, null);
				}
			}
		}
		
		if( !vertexDLS.containsAttribute(Attribute.NORMALS) ) {
			if( generateVertexNormals ) {
				if( nodeWasUpdated(vertexNormals) ) { 
					log( "set", Attribute.NORMALS, "vertex");
					ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vertexNormals()[0].length).createReadOnly(vertexNormals()));
				}
			} else {
				if( ifs.getVertexAttributes().containsAttribute(Attribute.NORMALS) ) {
					log( "cancle", Attribute.NORMALS,  "vertex" );
					ifs.setVertexAttributes(Attribute.NORMALS, null);
				}
			}
		}
		
		if( !faceDLS.containsAttribute(Attribute.LABELS) ) {
			if( generateFaceLabels ) { 
				if( nodeWasUpdated(faceLabels) ) { 
					log( "set", Attribute.LABELS, "labels");
					ifs.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(faceLabels()));
				} 
			} else if( ifs.getFaceAttributes().containsAttribute(Attribute.LABELS ) ) {
				log( "cancle", Attribute.LABELS, "labels");
				ifs.setVertexAttributes(Attribute.LABELS, null );
			}
		}
	}

	
	public IndexedFaceSet getIndexedFaceSet() {
		return ifs;
	}
	
	public boolean isGenerateEdgesFromFaces() {
		return generateEdgesFromFaces;
	}

	public void setGenerateEdgesFromFaces(boolean generateEdgesFromFaces) {
		this.generateEdgesFromFaces=generateEdgesFromFaces;
	}
	
	public boolean isGenerateVertexNormals() {
		return generateVertexNormals;
	}

	public void setGenerateVertexNormals(boolean generateVertexNormals) {
		this.generateVertexNormals=generateVertexNormals;
	}
	
	public boolean isGenerateFaceNormals() {
		return generateFaceNormals;
	}

	public void setGenerateFaceNormals(boolean generateFaceNormals) {
		this.generateFaceNormals=generateFaceNormals;
	}

	public boolean isGenerateFaceLabels() {
		return generateFaceLabels;
	}

	public void setGenerateFaceLabels(boolean generateFaceLabels) {
		this.generateFaceLabels = generateFaceLabels;
	}

}
