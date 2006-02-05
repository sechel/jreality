package de.jreality.geometry;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedFaceSetFactory extends AbstractIndexedFaceSetFactory {

	public IndexedFaceSetFactory() {
		super();
	}

	/* vertex attributes */
	
	public void setVertexCount( int count ) {
		super.setVertexCount(count);
	}
	
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

	/* face attributes */
	
	public void setFaceCount( int count ) {
		super.setFaceCount( count );
	}
	
	public void setFaceAttribute( Attribute attr, DataList data) {
		super.setFaceAttribute( attr, data );
	}
	
	public void setFaceAttributes(DataListSet dls ) {
		super.setFaceAttributes(dls);
	}
	
	public void setFaceIndices( DataList data ) {
		super.setFaceIndices(data);
	}
	
	public void setFaceIndices( int[][] data ) {
		super.setFaceIndices(data);
	}
	
	public void setFaceIndices( int[] data, int pointCountPerFace ) {
		super.setFaceIndices(data, pointCountPerFace );
	}
	
	public void setFaceIndices( int[] data ) {
		super.setFaceIndices(data);
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

	
}
