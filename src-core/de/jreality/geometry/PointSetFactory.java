package de.jreality.geometry;

import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class PointSetFactory extends AbstractPointSetFactory {

	public PointSetFactory() {
		super();
	}
	
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

	public void setVertexLabels( String [] data ) {
		super.setVertexLabels( data );
	}

	public PointSet getPointSet() {
		return ps;
	}
	
}
