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
		setVertexCoordinates(npoints);
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
	
}
