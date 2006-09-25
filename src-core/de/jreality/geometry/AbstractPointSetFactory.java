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
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.OoNode.IsUpdateCounter;
import de.jreality.math.Pn;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.WritableDataList;

class AbstractPointSetFactory extends AbstractGeometryFactory {
	
	boolean generateVertexLabels = false;
	
	final OoNode vertexLabels = new OoNode( "vertex.labels", update );
	
	final PointSet ps;

	final GeometryAttributeListSet vertex = new GeometryAttributeListSet( this, Geometry.CATEGORY_VERTEX );

	AbstractPointSetFactory( PointSet ps, int signature ) {
		
		super( ps, signature );
		
		this.ps = ps;
	}

	public AbstractPointSetFactory() {
		this( new PointSet(), Pn.EUCLIDEAN);
	}
	
	int nov(){
		return vertex.getCount();
	}
	
	public int getVertexCount() {
		return nov();
	}
	
	public void setVertexCount( int count ) {
		vertex.setCount( count );
	}
	
	protected void setVertexAttribute( Attribute attr, DataList data ) {
		vertex.setAttribute( attr, data );
	}
	
	protected void setVertexAttributes( DataListSet dls ) {
		vertex.setAttributes( dls );
	}
	
	protected void setVertexCoordinates( DataList data ) {
		setVertexAttribute( Attribute.COORDINATES, data );
	}
	
	protected void setVertexCoordinates( double [] data ) {
		if( nov() == 0 && data.length != 0 || data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.COORDINATES, new DoubleArrayArray.Inlined( data, data.length / nov() ) );
	}
	
	protected void setVertexCoordinates( double [][] data ) {
		//setVertexAttribute( Attribute.COORDINATES, new DoubleArrayArray.Array( data, data[0].length ) );
		setVertexAttribute( Attribute.COORDINATES,
				StorageModel.DOUBLE_ARRAY.array(data[0].length).createReadOnly(data));
	}
	
	protected void setVertexNormals( DataList data ) {
		setVertexAttribute( Attribute.NORMALS, data );
	}
	
	protected void setVertexNormals( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.NORMALS, new DoubleArrayArray.Inlined( data,  data.length / nov() ) );
	}
	
	protected void setVertexNormals( double [][] data ) {
		setVertexAttribute( Attribute.NORMALS, new DoubleArrayArray.Array( data, data[0].length ) );
	}
	
	protected void setVertexColors( DataList data ) {
		setVertexAttribute( Attribute.COLORS, data );
	}
	
	protected void setVertexColors( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.COLORS, new DoubleArrayArray.Inlined( data, data.length / nov() )  );
	}
	
	protected void setVertexColors( Color [] data ) {
		setVertexColors( toDoubleArray( data ) );
	}
	
	protected void setVertexColors( double [][] data ) {
		setVertexAttribute( Attribute.COLORS, new DoubleArrayArray.Array( data, data[0].length ) );
	}
	
	protected void setVertexTextureCoordinates( DataList data ) {
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, data );
	}
	
	protected void setVertexTextureCoordinates( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Inlined( data, data.length / nov() ) );
	}
	
	protected void setVertexTextureCoordinates( double [][] data ) {
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array( data, data[0].length ) );
	}

	protected void setVertexLabels( DataList data ) {
		setVertexAttribute( Attribute.LABELS, data );
	}
	
	protected void setVertexLabels( String[] data ) {
		if( data != null && data.length != nov() )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.LABELS, data == null ? null : new StringArray(data));
	}
	

	String [] vertexLabels() {
		return (String[])vertexLabels.getObject();
	}
	
	String [] generateVertexLabels() {
		if( vertex.DLS.containsAttribute(Attribute.LABELS)) {
			return vertex.DLS.getList(Attribute.LABELS)
			.toStringArray((String[])vertexLabels.getObject());
		} else {
			log( "compute", Attribute.LABELS, "vertex");
			return indexString(nov());
		}
	}
	
	String [] indexString(int nov) {
		String [] labels = new String[nov];
		for( int i=0; i<nov; i++ ) {
			labels[i]=Integer.toString(i);
		}
		return labels;
	}

	{
		vertexLabels.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {					
						return generateVertexLabels();		
					}					
				}
		);
	}
	
	void recompute() {
		
		super.recompute();
		
		if( isGenerateVertexLabels() ) 
			vertexLabels.update();
		
	}

	
	void updateImpl() {
		super.updateImpl();
		
		updatePointSet();
	}
	
	void updatePointSet() {
		
		updateGeometryAttributeCathegory( vertex );
		
		updateStringArray( vertex, Attribute.LABELS, generateVertexLabels, vertexLabels );
		
	}
	
	public PointSet getPointSet() {
		return ps;
	}
	
	public boolean isGenerateVertexLabels() {
		return generateVertexLabels;
	}

	public void setGenerateVertexLabels(boolean generateVertexLabels) {
		this.generateVertexLabels = generateVertexLabels;
		
		if( generateVertexLabels ) {
			if( vertex.DLS.containsAttribute( Attribute.LABELS))
				throw new UnsupportedOperationException( "you cannot not generate the attribute " + Attribute.LABELS +
						"because it is explicitly set. Unset it first." );
			vertex.blockAttribute.add( Attribute.LABELS );
		} else {
			vertex.blockAttribute.remove( Attribute.LABELS );
		}
	}
	
	static double [] toDoubleArray( Color [] color ) {
		float [] c = new float[5];
		double [] array = new double[color.length * 4 ];
		for( int i=0, j=0; i<array.length; i+=4, j++ ) {
			color[j].getComponents(c);
			array[i+0] = c[0];
			array[i+1] = c[1];
			array[i+2] = c[2];
			array[i+3] = c[3];
		}		
		return array;
	}
}
