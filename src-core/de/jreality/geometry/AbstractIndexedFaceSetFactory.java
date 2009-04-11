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
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.pick.AABBTree;
import de.jreality.util.PickUtility;

/** 
 * This class implements the functionality of the API class {@link IndexedFaceSetFactory}. 
 * Should only be accessed via IndexedFaceSetFactory.  
 */
class AbstractIndexedFaceSetFactory extends AbstractIndexedLineSetFactory {
	
	/** The IndexedFaceSet to be generated. Reference will not change any more.**/
	final IndexedFaceSet ifs;
	
	/** List of geometry attributes specific to faces. 
	 **/
	GeometryAttributeListSet face = new GeometryAttributeListSet( this, Geometry.CATEGORY_FACE );
	
	/* Standard generated attributes and their ingredients*/
	AttributeGenerator vertexCoordinates = attributeGeneratorNode( vertex, double[][].class, Attribute.COORDINATES);
	AttributeGenerator vertexNormals     = attributeGeneratorNode( vertex, double[][].class, Attribute.NORMALS );
	
	AttributeGenerator edgeIndices       = attributeGeneratorNode( edge, int[][].class,      Attribute.INDICES );
	
	AttributeGenerator faceIndices       = attributeGeneratorNode( face, int[][].class,      Attribute.INDICES );
	AttributeGenerator faceLabels        = attributeGeneratorNode( face, String[].class,     Attribute.LABELS );
	AttributeGenerator faceNormals       = attributeGeneratorNode( face, double[][].class,   Attribute.NORMALS );
	
	/* Ingredients that affect the geometry attributes in a non standard way, or have non standard */
	OoNode faceCount = node( "faceCount", Integer.class, 0 );
	OoNode aabbTree  = node( "aabbTree", AABBTree.class, null );
	boolean generateAABBTree = false;
	OoNode unwrapFaceIndices = node( "unwrapFaceIndices", int[][].class, null);
	OoNode actualVertexOfUnwrapVertex = node ( "actualVertexOfUnwrapVertex", int[].class, null);
	
	/* constructors */
	AbstractIndexedFaceSetFactory( IndexedFaceSet ifs, int metric, boolean generateEdgesFromFaces, boolean generateVertexNormals, boolean generateFaceNormals ) {
		super( ifs, metric );	

		this.ifs = ifs;
		
		setGenerateEdgesFromFaces( generateEdgesFromFaces);
		setGenerateFaceNormals( generateVertexNormals );
		setGenerateFaceNormals( generateFaceNormals );
	}
	
	AbstractIndexedFaceSetFactory( int metric, boolean generateEdgesFromFaces, boolean generateVertexNormals, boolean generateFaceNormals ) {
		this( new IndexedFaceSet(0,0), metric, generateEdgesFromFaces, generateVertexNormals, generateFaceNormals );	
	}
	
	AbstractIndexedFaceSetFactory( IndexedFaceSet existing, int metric ) {
		this(existing,  metric, false, false, false );
	}
	
	AbstractIndexedFaceSetFactory( int metric ) {
		this( metric, false, false, false );
	}
	
	AbstractIndexedFaceSetFactory(IndexedFaceSet existing) {
		this(  existing, Pn.EUCLIDEAN, false, false, false );
	}
	
	public AbstractIndexedFaceSetFactory() {
		this( Pn.EUCLIDEAN );
	}
	
	/* Getters and setters. */
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
	
	protected void setFaceAttribute(Attribute attr, double [] data ) {
		if( data != null && (nof() == 0 && data.length != 0 || data.length % nof() != 0) )
			throw new IllegalArgumentException( "array has wrong length" );
		setFaceAttribute( attr, data==null ? null : new DoubleArrayArray.Inlined( data, data.length / nof() ) );
	}
	
	protected void setFaceAttribute(Attribute attr,  double [][] data ) {
		setFaceAttribute( attr,
				StorageModel.DOUBLE_ARRAY.array(data[0].length).createReadOnly(data));
	}
	
	protected void setFaceAttributes(DataListSet dls ) {
		face.setAttributes(dls);
	}

	protected void setFaceIndices(DataList data) {
		setFaceAttribute(Attribute.INDICES, data);
	}

	protected void setFaceIndices(int[][] data) {
		setFaceAttribute(Attribute.INDICES, new IntArrayArray.Array(data));
	}

	protected void setFaceIndices(int[] data, int pointCountPerFace) {
		if (data != null && data.length != pointCountPerFace * nof())
			throw new IllegalArgumentException("array has wrong length");
		setFaceAttribute(Attribute.INDICES, data == null ? null
				: new IntArrayArray.Inlined(data, pointCountPerFace));
	}

	protected void setFaceIndices(int[] data) {
		setFaceIndices(data, 3);
	}
	
	protected void setUnwrapFaceIndices(DataList data) {
		if (data!=null && data.size()!=nof())
			throw new IllegalArgumentException("Data list of face indices for unwrapes faces has wrong length.");
		setUnwrapFaceIndices(data==null?null:data.toIntArrayArray(null));
	}

	protected void setUnwrapFaceIndices(int[][] data) {
		if (data!=null && data.length!=nof())
			throw new IllegalArgumentException("Array of face indices for unwraped faces has wrong length.");
		unwrapFaceIndices.setObject(data);
	}

	protected void setUnwrapFaceIndices(int[] data, int pointCountPerFace) {
		if (data != null && data.length != pointCountPerFace * nof())
			throw new IllegalArgumentException("Array of indices for unwraped faces has wrong length.s");
		setUnwrapFaceIndices(data == null ? null
				: new IntArrayArray.Inlined(data, pointCountPerFace));
	}

	protected void setUnwrapFaceIndices(int[] data) {
		setUnwrapFaceIndices(data, 3);
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
	
	public IndexedFaceSet getIndexedFaceSet() {
		return ifs;
	}
	
	public boolean isGenerateEdgesFromFaces() {
		// edgeIndices.outdate(); Why should this be here? 
		return edgeIndices.isGenerate();
	}

	public void setGenerateEdgesFromFaces(boolean generateEdgesFromFaces) {
		if( generateEdgesFromFaces && edge.hasEntries() )
			throw new UnsupportedOperationException( 
					"You cannot generate edges form faces " +
					"while edge attributes are set." +
					"Set them to null before.");
			
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
    	
    /* IMPLEMENTATIONS OF GENERATED ATTRIBUTES according to the following dependency tree:
     * 
     *  faceCount      metric   vertexCoordinates    faceIndices      unwrapFaceIndices
     *     |               |\        /  ____\________/  /   \                 |
     *  faceLables         | \      /  /     \         /     \     actualVertexOfUnwrapVertex
     *                     |  faceNormals     aabbTree     edgeIndices   
     *                     |     /                            |
     *                     |    /               edgeCount (see AbstractIndexedLineSet)
     *                     |   /
     *               vertexNormals
     * 
     */
	
	/* actualVertexOfUnwrapVertex
	 * generate the translation table between actual vertices and unwrap vertices 
	 * from faceIndices and unwrapFaceIndices
	 */
	{
		actualVertexOfUnwrapVertex.addIngr(unwrapFaceIndices);
		actualVertexOfUnwrapVertex.addIngr(faceIndices);
		actualVertexOfUnwrapVertex.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {
						if (unwrapFaceIndices.getObject()==null) 
							return null;
	
						int[][] fi = (int[][]) faceIndices.getObject();
						int[][] unwrapFI = (int[][]) unwrapFaceIndices.getObject();
						int[] actualVOUV = (int[]) object;
	
						//get number of unwrap vertices
						int nv=nov();
						
						//update translation table between actual and unwrap vertex indices
						if (actualVOUV==null || actualVOUV.length != nv)
							actualVOUV = new int[nv];
						for (int f=0; f<nof(); f++) 
							for (int v=0; v<fi[f].length; v++) 
								actualVOUV[unwrapFI[f][v]]=fi[f][v];
						
						return actualVOUV;
					}					
				}
		);
	}

	
	/* faceLabels
	 * The faces are labeled by their indices.
	 */
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

	/* aabbTree
	 * generate aabbTree according to AABBTree.construct()
	 */
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

	/* EdgeIndices
	 * generate EdgeIndices according to IndexedFaceSetUtility.edgesFromFaces()
	 */
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
	/* overwrite in subclass. */
	int [][] generateEdgeIndices() {
		int [][] fi = (int[][])faceIndices.getObject();
		if( fi==null)
			return null;
		return IndexedFaceSetUtility.edgesFromFaces( fi ).toIntArrayArray(null);
	}
	
	/* edgeCount
	 * update edgeCount (see AbstractIndexedLineSet). 
	 */
	{
		edgeCount.setUpdateMethod( new UpdateMethod() {
			public Object update(Object object) {
				int count;
				if(edgeIndices.isGenerate())
					count = ((int[][])edgeIndices.getObject()).length;
				else // Never happens? (see setGenerateEdgesFromFaces() )
					count = edge.DLS.containsAttribute(Attribute.INDICES) ? edge.DLS.getListLength() : 0;
				return new Integer(count);	
			}			
		});
	}

    /* faceNormals
     * generate faceNormals according to IndexedFaceSetUtility.calculateFaceNormals()
     */
	{
		faceNormals.addIngr(metric);
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
	
		return IndexedFaceSetUtility.calculateFaceNormals( fi, vc, getMetric() );
		
	}
	
    /* vertexNormals
     * generate vertexNormals according to IndexedFaceSetUtility.calculateVertexNormals()
     */
	{
		vertexNormals.addIngr(metric);
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
			fn = IndexedFaceSetUtility.calculateFaceNormals( fi, vc, getMetric() );
		}
		
		return IndexedFaceSetUtility.calculateVertexNormals( fi, vc, fn, getMetric() );
	}

	
	void recompute() {		
			
		super.recompute();
			
		aabbTree.update();
		
		actualVertexOfUnwrapVertex.update();
		
		faceLabels.update();
		edgeIndices.update();
		faceNormals.update();
		if (unwrapFaceIndices.getObject()==null  && nodeWasUpdated(unwrapFaceIndices)) 
				//unwrapFaceIndices where just set to null, 
				//so the usual vertexNormals have to be restored
				vertexNormals.outdate();		
		vertexNormals.update();
	}

	protected void updateImpl() {
	 		
		super.updateImpl();
		
		if( ifs.getNumFaces() != getFaceCount() )
			ifs.setNumFaces( getFaceCount() );
		
		updateGeometryAttributeCathegory( face );
		
		if( nodeWasUpdated(aabbTree))
			ifs.setGeometryAttributes(PickUtility.AABB_TREE, aabbTree.getObject());
		
		/*face indices */
		if (unwrapFaceIndices.getObject()!=null)			
			ifs.setFaceAttributes(
					Attribute.INDICES, 
					StorageModel.INT_ARRAY_ARRAY.createReadOnly( (int[][]) unwrapFaceIndices.getObject())
					);
		else if (nodeWasUpdated(unwrapFaceIndices)) // unwrapFaceIndices was set to null since last update, so 
			//faceIndices have to be restored, even if they have not been updated. If faceIndices where updated
			//they where already set/restored by "updateGeometryAttributeCathegory( face )" above. 
			ifs.setFaceAttributes(
					Attribute.INDICES, 
					StorageModel.INT_ARRAY_ARRAY.createReadOnly( (int[][]) faceIndices.getObject())
					);
			
		
		edgeIndices.updateArray();
		faceLabels.updateArray();
		faceNormals.updateArray();
		
		/* vertex normals */
		if (unwrapFaceIndices.getObject()==null  || !vertexNormals.isGenerate()) {
			//in both cases the usual updateArray is able to decide what has to be done
			vertexNormals.updateArray();
		}
		else //unwrapFaceIndices present AND vertexNormals are generated  
			if (nodeWasUpdated(actualVertexOfUnwrapVertex) || nodeWasUpdated(vertexNormals)) {
				//
			double[][] vn=(double[][]) vertexNormals.getObject();
			int[] translation=(int[]) actualVertexOfUnwrapVertex.getObject();
			double[][] unwrappedVN = new double[nov()][vn[0].length]; 
			
			for (int i=0; i<unwrappedVN.length; i++)
				System.arraycopy(vn[translation[i]], 0, unwrappedVN[i], 0, vn[translation[i]].length);
			
			ifs.setVertexAttributes(
					Attribute.NORMALS, 
					StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly( unwrappedVN ) 
					);
		}
			
				
	}

}
