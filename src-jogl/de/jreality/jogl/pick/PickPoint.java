/*
 * Created on Mar 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.pick;

import de.jreality.scene.Graphics3D;
import de.jreality.scene.SceneGraphPath;


/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PickPoint {

	public static final int HIT_NOTHING = 0;
	public static final int HIT_FACE	= 1;
	public static final int HIT_EDGE	= 2;
	public static final int HIT_VERTEX	= 4;
	
	SceneGraphPath 	pickPath;
	public int		pickType;
	public double[]	pointNDC, pointWorld, pointObject;
	public double[]	normal, normalObject;
	public double[]	textureCoordinates;
	public int	faceNum, edgeNum, vertexNum;
	public int[]	indices = new int[3];
	double[]	barycentricCoords;
	public Graphics3D context = null;
	
	/**
	 * 
	 */
	public PickPoint() {
		super();
		pickType = HIT_NOTHING;
	}

	/**
	 * @return
	 */
	public int getEdgeNum() {
		return edgeNum;
	}

	/**
	 * @return
	 */
	public int getFaceNum() {
		return faceNum;
	}

	/**
	 * @return
	 */
	public double[] getNormal() {
		return normal;
	}

	/**
	 * @return
	 */
	public double[] getNormalObject() {
		return normalObject;
	}

	/**
	 * @return
	 */
	public SceneGraphPath getPickPath() {
		return pickPath;
	}

	/**
	 * @return
	 */
	public int getPickType() {
		return pickType;
	}

	/**
	 * @return
	 */
	public double[] getPointNDC() {
		return pointNDC;
	}

	/**
	 * @return
	 */
	public double[] getPointObject() {
		return pointObject;
	}

	/**
	 * @return
	 */
	public double[] getPointWorld() {
		return pointWorld;
	}

	/**
	 * @return
	 */
	public double[] getTextureCoordinates() {
		return textureCoordinates;
	}

	/**
	 * @return
	 */
	public int getVertexNum() {
		return vertexNum;
	}

	/**
	 * @param i
	 */
	public void setEdgeNum(int i) {
		edgeNum = i;
	}

	/**
	 * @param i
	 */
	public void setFaceNum(int i) {
		faceNum = i;
	}

	/**
	 * @param ds
	 */
	public void setNormal(double[] ds) {
		normal = ds;
	}

	/**
	 * @param ds
	 */
	public void setNormalObject(double[] ds) {
		normalObject = ds;
	}

	/**
	 * @param path
	 */
	public void setPickPath(SceneGraphPath path) {
		pickPath = path;
	}

	/**
	 * @param i
	 */
	public void setPickType(int i) {
		pickType = i;
	}

	/**
	 * @param ds
	 */
	public void setPointNDC(double[] ds) {
		pointNDC = ds;
	}

	/**
	 * @param ds
	 */
	public void setPointObject(double[] ds) {
		pointObject = ds;
	}

	/**
	 * @param ds
	 */
	public void setPointWorld(double[] ds) {
		pointWorld = ds;
	}

	/**
	 * @param ds
	 */
	public void setTextureCoordinates(double[] ds) {
		textureCoordinates = ds;
	}

	/**
	 * @param i
	 */
	public void setVertexNum(int i) {
		vertexNum = i;
	}

	public Graphics3D getContext() {
		return context;
	}
	public void setContext(Graphics3D context) {
		this.context = context;
	}
}


 
