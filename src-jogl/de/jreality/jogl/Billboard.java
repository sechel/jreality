/*
 * Author	gunn
 * Created on Mar 3, 2006
 *
 */
package de.jreality.jogl;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class Billboard extends IndexedFaceSet {

	static double[][] points = {{0,0,0},{1,0,0},{1,1,0},{0,1,0}};
    static int[][] indices = {{0,1,2,3}};
    static double[][] texs = {{0,1},{1,1},{1,0},{0,0}};
	public Billboard() {
		super();
		setNumPoints(4);
		vertexAttributes.addReadOnly(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3),points);
		setNumFaces(1);
		vertexAttributes.addReadOnly(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(2),texs);
		faceAttributes.addReadOnly(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, indices);
		GeometryUtility.calculateAndSetNormals(this);
	}

	double xscale=1.0, yscale=1.0;
	double[] offset = {0,0,0};
	double[] position = {0,0,0};
	public double[] getPosition() {
		return position;
	}
	public void setPosition(double[] position) {
		this.position = position;
	}
	public double[] getOffset() {
		return offset;
	}
	public void setOffset(double[] offset) {
		this.offset = offset;
	}
	public double getXscale() {
		return xscale;
	}
	public void setXscale(double xscale) {
		this.xscale = xscale;
	}
	public double getYscale() {
		return yscale;
	}
	public void setYscale(double yscale) {
		this.yscale = yscale;
	}
	
	
}
