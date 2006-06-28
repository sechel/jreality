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
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;

class AbstractIndexedLineSetFactory extends AbstractPointSetFactory {
	
//	final OoNode edgeNormals = new OoNode( "edge.normals" );
//	final OoNode edgeIndices = new OoNode( "edge.indices" );
	final OoNode edgeLabels  = new OoNode( "edge.labels" );

//	final OoNode vertexCoordinates = new OoNode( "vertex.coordinates" );
//	final OoNode vertexNormals     = new OoNode( "vertex.normals" );

	
//	boolean generateVertexNormals     = false;
//	boolean generateEdgeNormals       = false;
	boolean generateEdgeLabels	      = false;
//	boolean generateEdgesFromVertices = false;
	
	DataListSet edgeDLS = new DataListSet(0);

	final IndexedLineSet ils;
	
	HashMap edgeAttributeNode = new HashMap();
	
	AbstractIndexedLineSetFactory( IndexedLineSet ils, int signature ) {
		super( ils, signature );	

		this.ils = ils;
	}
	
	AbstractIndexedLineSetFactory( int signature ) {
		this( new IndexedLineSet(0,0), signature );
	}
	
	AbstractIndexedLineSetFactory(IndexedLineSet existing) {
		this(  existing, Pn.EUCLIDEAN );
	}
	
	public AbstractIndexedLineSetFactory() {
		this( new IndexedLineSet(0,0), Pn.EUCLIDEAN );
	}
	
	protected int noe(){
		return edgeDLS.getListLength();
	}
	
	public int getLineCount() {
		return noe();
	}
	
	void setLineCount( int count ) {
		if( count == noe() )
			return;
	
		edgeDLS.reset(count);
	}
	
	OoNode edgeAttributeNode( Attribute attr ) {
		return geometryAttributeNode( edgeAttributeNode, "EDGE", attr );
	}
	
	void updateEdgeAttributes() {
		for( Iterator iter = edgeDLS.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			
			edgeAttributeNode( attr ).update();	
		}
	}
	
	protected void setEdgeAttribute( Attribute attr, DataList data ) {
		setAttrImpl( edgeDLS, attr, data );
		edgeAttributeNode(attr).setObject( data );
	}
	
	protected void setEdgeAttributes(DataListSet dls ) {
		edgeDLS = dls;	
		for( Iterator iter = dls.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			edgeAttributeNode(attr).setObject( dls.getList(attr));
		}
	}
	
	protected void setEdgeIndices( DataList data ) {
		setEdgeAttribute( Attribute.INDICES, data );
	}
	
	protected void setEdgeIndices( int[][] data ) {
		setEdgeAttribute( Attribute.INDICES, new IntArrayArray.Array( data ) );
	}
	
	protected void setEdgeIndices( int[] data, int pointCountPerLine ) {
		if( data.length != pointCountPerLine * noe() )
			throw new IllegalArgumentException( "array has wrong length" );
		setEdgeAttribute( Attribute.INDICES, new IntArrayArray.Inlined( data, pointCountPerLine ) );
	}
	
	protected void setEdgeIndices( int[] data ) {
		setEdgeIndices( data, 2 );
	}
	
//	protected void setEdgeNormals( DataList data ) {
//		setEdgeAttribute( Attribute.NORMALS, data );
//	}
//	
//	protected void setEdgeNormals( double [] data ) {
//		if( data.length % noe() != 0 )
//			throw new IllegalArgumentException( "array has wrong length" );	
//		setEdgeAttribute( Attribute.NORMALS, new DoubleArrayArray.Inlined( data, data.length / noe() ) );
//	}
//	
//	protected void setEdgeNormals( double [][] data ) {
//		setEdgeAttribute( Attribute.NORMALS, new DoubleArrayArray.Array( data ) );
//	}
//	
	protected void setEdgeColors( DataList data ) {
		setEdgeAttribute( Attribute.COLORS, data );
	}
	
	protected void setEdgeColors( double [] data ) {
		if( data.length % noe() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );	
		setEdgeAttribute( Attribute.COLORS, new DoubleArrayArray.Inlined( data, data.length / noe() ) );
	}
	
	protected void setEdgeColors( Color [] data ) {
		setEdgeColors( toDoubleArray(data));
	}
	
	protected void setEdgeColors( double [][] data ) {
		setEdgeAttribute( Attribute.COLORS, new DoubleArrayArray.Array( data ) );
	}


	protected void setEdgeLabels( DataList data ) {
		setVertexAttribute( Attribute.LABELS, data );
	}
	
	
	protected void setEdgeLabels( String[] data ) {
		if( data.length != noe() )
			throw new IllegalArgumentException( "array has wrong length" );
		setEdgeAttribute( Attribute.LABELS, new StringArray(data));
	}

	String [] edgeLabels() {
		return (String[])edgeLabels.getObject();
	}
	
	String [] generateEdgeLabels() {
		if( edgeDLS.containsAttribute(Attribute.LABELS)) {
			return edgeDLS.getList(Attribute.LABELS)
			.toStringArray((String[])edgeLabels.getObject());
		} else {
			log( "compute", Attribute.LABELS, "edge");
			return indexString(noe());
		}
	}
	

	{
		edgeLabels.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateEdgeLabels();		
					}					
				}
		);
	}
	
//	{
//		edgeIndices.addIngr( edgeAttributeNode( Attribute.INDICES ) );
//		edgeIndices.setUpdateMethod(
//				new OoNode.UpdateMethod() {
//					public Object update( Object object) {	
//						return edgeDLS.getList(Attribute.INDICES)
//							.toIntArrayArray(null);					
//					}					
//				}
//		);
//	}
//	
//	int [][] edgeIndices() {
//		return (int[][])(edgeIndices.getObject());
//	}
	
//	{
//		
//		vertexCoordinates.addIngr( vertexAttributeNode( Attribute.COORDINATES)) ;
//		vertexCoordinates.setUpdateMethod(
//				new OoNode.UpdateMethod() {
//					public Object update( Object object) {					
//						return vertexDLS.getList(Attribute.COORDINATES)
//							.toDoubleArrayArray(null);			
//					}					
//				}
//		);
//	}
//	
//	double [][] vertexCoordinates() {
//		return (double[][])vertexCoordinates.getObject();
//	}
//	
//	double [][] edgeNormals() {
//		return (double[][])edgeNormals.getObject();
//	}
//	
//	double [][] generateLineNormals() {
//		if( edgeDLS.containsAttribute(Attribute.NORMALS)) {
//			return edgeDLS.getList(Attribute.NORMALS)
//			.toDoubleArrayArray((double[][])edgeNormals.getObject());
//		} else {
//			log( "compute", Attribute.NORMALS, "edge");
//			return GeometryUtility.calculateLineNormals( edgeIndices(), vertexCoordinates(), getSignature() );
//		}
//	}
//	
//	
//	{
//		vertexNormals.addIngr(edgeNormals);
//		
//		vertexNormals.setUpdateMethod(
//				new OoNode.UpdateMethod() {
//					public Object update( Object object) {					
//						return generateVertexNormals();		
//					}					
//				}
//		);
//	}
//	
//
//	double [][] vertexNormals() {
//		return (double[][])vertexNormals.getObject();
//	}
//	
//	double [][] generateVertexNormals() {
//		if( vertexDLS.containsAttribute(Attribute.NORMALS)) {
//			return null;
//		} else {
//			log( "compute", Attribute.NORMALS, "vertex" );
//			return GeometryUtility.calculateVertexNormals( edgeIndices(), vertexCoordinates(), edgeNormals(), getSignature() );
//		}
//	}
//	
	
	void recompute() {		
			
		super.recompute();
		
		if( isGenerateEdgeLabels() )
			edgeLabels.update();
		
//		if( isGenerateEdgesFromVertices() ) 
//			edgeIndices.update();
//		
//		if( isGenerateLineNormals() )
//			edgeNormals.update();
//		
//		if( isGenerateVertexNormals() )
//			vertexNormals.update();
//		
	}
	
	protected void updateImpl() {
		super.updateImpl();
		
		if( ils.getNumEdges() == noe() ) {

			for( Iterator iter = edgeDLS.storedAttributes().iterator(); iter.hasNext(); ) {
				Attribute attr = (Attribute)iter.next();
				
				edgeAttributeNode( attr ).update();
				
				if(  nodeWasUpdated(edgeAttributeNode( attr ))  ) {
					log( "set", attr, "edge" );
					ils.setVertexAttributes( attr, vertexDLS.getWritableList(attr));			
				}				
			}
		} else {
			updateEdgeAttributes();
			ils.setEdgeCountAndAttributes(edgeDLS);
		}
		
//		if( generateEdgesFromVertices ) { 
//			if( nodeWasUpdated(edgeIndices) ) { 
//				log( "set", Attribute.INDICES, "edge");
//				ils.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array( edgeIndices() ) );
//			} else if( noe() == 0 ) {
//				ils.setNumEdges(0);
//			}
//		} else if( ils.getEdgeAttributes().containsAttribute(Attribute.INDICES) ) {
//			log( "cancle", Attribute.INDICES, "edge");
//			ils.setEdgeAttributes(Attribute.INDICES, null );
//		}
		
//		if( !edgeDLS.containsAttribute(Attribute.NORMALS) ) {
//			if( generateEdgeNormals ) {
//				if( nodeWasUpdated( edgeNormals ) ) { 
//					log( "set", Attribute.NORMALS, "edge");
//					ils.setEdgeAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(edgeNormals()[0].length).createReadOnly(edgeNormals()));
//				}
//			} else {
//				if( ils.getEdgeAttributes().containsAttribute(Attribute.NORMALS) ) {
//					log( "cancle", Attribute.NORMALS, "edge");
//					ils.setEdgeAttributes(Attribute.NORMALS, null);
//				}
//			}
//		}
//		
//		if( !vertexDLS.containsAttribute(Attribute.NORMALS) ) {
//			if( generateVertexNormals ) {
//				if( nodeWasUpdated(vertexNormals) ) { 
//					log( "set", Attribute.NORMALS, "vertex");
//					ils.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vertexNormals()[0].length).createReadOnly(vertexNormals()));
//				}
//			} else {
//				if( ils.getVertexAttributes().containsAttribute(Attribute.NORMALS) ) {
//					log( "cancle", Attribute.NORMALS,  "vertex" );
//					ils.setVertexAttributes(Attribute.NORMALS, null);
//				}
//			}
//		}
//		
		if( generateEdgeLabels ) { 
			if( nodeWasUpdated(edgeLabels) ) { 
				log( "set", Attribute.LABELS, "labels");
				ils.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(edgeLabels()));
			} 
		} else if( ils.getEdgeAttributes().containsAttribute(Attribute.LABELS ) ) {
			log( "cancle", Attribute.LABELS, "labels");
			ils.setVertexAttributes(Attribute.LABELS, null );
		}
		
	}

	
	public IndexedLineSet getIndexedLineSet() {
		return ils;
	}
	
//	public boolean isGenerateEdgesFromVertices() {
//		return generateEdgesFromVertices;
//	}
//
//	public void setGenerateEdgesFromVertices(boolean generateEdgesFromLines) {
//		this.generateEdgesFromVertices=generateEdgesFromLines;
//	}
	
//	public boolean isGenerateVertexNormals() {
//		return generateVertexNormals;
//	}
//
//	public void setGenerateVertexNormals(boolean generateVertexNormals) {
//		this.generateVertexNormals=generateVertexNormals;
//	}
//	
//	public boolean isGenerateEdgeNormals() {
//		return generateEdgeNormals;
//	}
//
//	public void setGenerateEdgeNormals(boolean generateEdgeNormals) {
//		this.generateEdgeNormals=generateEdgeNormals;
//	}

	public boolean isGenerateEdgeLabels() {
		return generateEdgeLabels;
	}

	public void setGenerateEdgeLabels(boolean generateEdgeLabels) {
		this.generateEdgeLabels = generateEdgeLabels;
	}

}
