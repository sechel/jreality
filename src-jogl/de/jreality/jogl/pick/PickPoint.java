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


package de.jreality.jogl.pick;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;


/**
 * This class is used by {@link de.jreality.jogl.pick.PickAction} to record the results
 * for a single pick hit. 
 * 
 * It consists of a variety of information about a pick hit. The constructor requires three
 * fields: a Viewer instance (to provide the necessary information about the camera transformation
 * and frustum, the scene graph path leading to the selected geometry, and the pick point in 
 * normalized device coordinates (@link #getPointNDC(double[])}. 
 * 
 * The first two items are combined into an instance of {@link charlesgunn.jreality.jogl.pick.Graphics3D} (@see #getContext()), 
 * which can be used to derive much information about the pick point, such as other coordinate systems.
 * (By using the <tt>getMatrix()</tt> methods on {@link de.jreality.scene.SceneGraphPath}, etc}.
 * 
 * There are certain data related to the pick point which can also be set by the picker.  First the type of the
 * pick hit, (whether it comes from a vertex, edge, or face) can be set using {@link #setPickType(int).
 * Additionally, vector data such as object normal vector and
 * texture coordinates can be set.  (@see #setNormalObject(double[])) and (@see #setTextureCoordinates(double[])). 
 * 
 * Additionally in the case of  a picked face, there are methods related to interpolating arbitrary vertex data.  
 * To do this effectively,
 * the picking procedure (for example, @link de.jreality.scene.pick.PickAction) needs to identify a triangle in which
 * the pick point lies and provide its integer indices {@link #setIndices(int[])} within its larger face, 
 * and also its barycentric coordinates 
 * within this triangle using {@link #setBarycentricCoords(double[])}.  Then, these values can be accessed 
 * and other sorts of vertex data can be interpolated reliably.  
 * 
 * Current limitations and notes:
 * 		The barycentric coordinates are not being used at the moment.  
 * 		Certain getters ({@link #getPointWorld(), @link #getPointObject()} are provided now as convenience
 * methods.  These results can be deduced from the other fields of the instance, so they perhaps should be
 * moved into static methods in a utility class.  I have deprecated the methods to indicate this issue.
 *    
 * Conventions:
 * 		in order to work seamlessly with non-euclidean geometries, all geometric data (positions and
 * normal vectors) are specified using homogeneous coordinates.  That means, in the euclidean case, that
 * positions should be specified as homogeneous 4-vectors ending with 1.0, and normal vectors as homogeneous
 * vectors ending with 0.0.  Users of both set and get methods should keep this convention in mind. Probably
 * the set methods should be adjusted to automatically homogenize the points if the caller has provided 3-vectors.
 * 
 * @author Charles Gunn
 *
 */
public class PickPoint implements PickResult {

	/**
	 * @return Returns the indices.
	 */
	public int[] getIndices() {
		return indices;
	}
	/**
	 * @param indices The indices to set.
	 */
	public void setIndices(int[] indices) {
		this.indices = indices;
	}
	public static final int HIT_NOTHING = 0;
	public static final int HIT_FACE	= 1;
	public static final int HIT_EDGE	= 2;
	public static final int HIT_VERTEX	= 4;
	
	public int		pickType, pickCoordinateSystem;
	public double[]	pointNDC, pointWorld;
	public double[]	textureCoordinates;
	public int	faceNum, edgeNum, vertexNum;
	public int[] edgeID = {0,0};
	public int[]	indices = new int[3];
	double[]	barycentricCoords;
	SceneGraphPath pickPath = null, cameraPath = null;
	//public Graphics3D context = null;
	
	private PickPoint()	{
		super();
	}
	/**
	 * @deprecated
	 */
	private PickPoint(Viewer v,  SceneGraphPath sgp, double[] pNDC) {
		this(v.getSceneRoot(), v.getCameraPath(), sgp);
	    setPointNDC(pNDC);
	}
	
	/**
	 * @deprecated Use factory methods
	 * @param sgc
	 * @param cp
	 * @param pp
	 */
	private PickPoint(SceneGraphComponent sgc, SceneGraphPath cp, SceneGraphPath pp)	{
		super();
		//pointNDC = pNDC;
		setPickPath(pp);
		setCameraPath(cp);
		
		pickType = HIT_NOTHING;
	}
	
	/**
	 * Use this method to create a pick point in NDC coordinates.  A camera path is required argument.
	 * @param pp
	 * @param camPath
	 * @param pointNDC
	 * @return
	 */public static PickPoint PickPointFactory(SceneGraphPath pp, SceneGraphPath camPath, double[] pointNDC)	{
	 	if (camPath == null) throw new IllegalArgumentException("Camera path can't be null");
		PickPoint value = new PickPoint();
		value.setPickPath(pp);
		value.setCameraPath(camPath);
		value.setPointNDC(pointNDC);
		value.pickCoordinateSystem = PickAction.PICK_NDC;
		return value;
	}

	/**
	 * Use this method to create a pick point in world coordinates (no camera information assumed)
	 * @param pp
	 * @param pointWorld
	 * @return
	 */public static PickPoint PickPointFactory(SceneGraphPath pp, double[] pointWorld)	{
		PickPoint value = new PickPoint();
		value.setPickPath(pp);
		value.setPointWorld(pointWorld);
		value.pickCoordinateSystem = PickAction.PICK_WORLD;
		return value;
	}

	/**
	 * @param ds
	 */
	protected void setPointNDC(double[] ds) {
		pointNDC = new double[4];
		pointNDC[3] = 1.0;
		System.arraycopy(ds, 0, pointNDC, 0 ,ds.length);
		Pn.dehomogenize(pointNDC, pointNDC);
	}

	/**
	 * @param ds
	 */
	protected void setPointWorld(double[] ds) {
		pointWorld = new double[4];
		pointWorld[3] = 1.0;
		System.arraycopy(ds, 0, pointWorld, 0 ,ds.length);
	}

	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}
	
	protected void setCameraPath(SceneGraphPath cameraPath) {
		this.cameraPath = cameraPath;
	}
	
	/**
	 * @return
	 */
	public SceneGraphPath getPickPath() {
		return pickPath;
	}

	protected void setPickPath(SceneGraphPath pickPath) {
		this.pickPath = (SceneGraphPath) pickPath.clone();
	}
	public String toString()	{
		return "pickPoint:\n\tPath:\t"+getPickPath().toString()+"\n\tFace number: "+faceNum+"\n\tworld:\t"+Rn.toString(getPointWorld())+"\n\tobject:\t"+Rn.toString(getPointObject());		
	}

	/**
	 * @return
	 */
	public int getPickType() {
		return pickType;
	}

	/**
	 * either this was provided by the user, or it's null and we can't help that
	 * @return
	 */
	public double[] getPointNDC() {
		return pointNDC;
	}

	/**
	 * A convenience method
	 * @return
	 */
	public double[] getPointObject() {
		double[] pointObject = null;
		Graphics3D context = new Graphics3D(cameraPath, pickPath, 1.0);
		if (pickCoordinateSystem == PickAction.PICK_NDC)  pointObject = Rn.matrixTimesVector(null, context.getNDCToObject(), pointNDC );
		else pointObject = Rn.matrixTimesVector(null, context.getWorldToObject(), pointWorld);
		if (pointObject.length == 4) Pn.dehomogenize(pointObject, pointObject);
		return (double[]) pointObject.clone();
	}

	/**
	 * A convenience method
	 * @return
	 */
	public double[] getPointWorld() {
		if (pickCoordinateSystem == PickAction.PICK_WORLD) return pointWorld;
		if (pointNDC == null)	throw new IllegalStateException("PickPoint should have non-null NDC point");
		Graphics3D context = new Graphics3D(cameraPath, pickPath, 1.0);
		double[] pointWorld = Rn.matrixTimesVector(null, context.getNDCToWorld(), pointNDC );
		if (pointWorld.length == 4) Pn.dehomogenize(pointWorld, pointWorld);
		return (double[]) pointWorld.clone();
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
  
  public int getIndex() {
    switch (pickType) {
      case PICK_TYPE_LINE:
        return edgeNum;
      case PICK_TYPE_POINT:
        return vertexNum;
      case PICK_TYPE_FACE:
        return faceNum;
      default:
        return -1;
    }
  }

	/**
	 * @param i
	 */
	public void setVertexNum(int i) {
		vertexNum = i;
	}

	/**
	 * @param i
	 * @deprecated Use {@link PickPoint#setEdgeNum(int[])}.
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
	 * @param i
	 */
	public void setPickType(int i) {
		pickType = i;
	}

	/**
	 * @param ds
	 */
	public void setTextureCoordinates(double[] ds) {
		textureCoordinates = ds;
	}

	/**
	 * @return Returns the barycentricCoords.
	 */
	public double[] getBarycentricCoords() {
		return barycentricCoords;
	}
	/**
	 * @param barycentricCoords The barycentricCoords to set.
	 */
	public void setBarycentricCoords(double[] barycentricCoords) {
		this.barycentricCoords = barycentricCoords;
	}
	
	/**
	 * To identify an edge between two vertices we need two integers, since a single "edge" in an IndexedLineSet 
	 * is actually a curve. The second entry identifies which segment of the curve is given.
	 * @param geomID
	 */
	public void setEdgeNum(int[] ia) {
		edgeID[0] = ia[0];
		edgeID[1] = ia[1];
		
	}
  /* (non-Javadoc)
   * @see de.jreality.scene.pick.PickResult#getWorldCoordinates()
   */
  public double[] getWorldCoordinates() {
    return getPointWorld();
  }
  /* (non-Javadoc)
   * @see de.jreality.scene.pick.PickResult#getObjectCoordinates()
   */
  public double[] getObjectCoordinates() {
    return getPointObject();
  }
public double getAffineCoordinate() {
	return 0;
}
	
}


 
