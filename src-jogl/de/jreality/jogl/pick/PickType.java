/*
 * Created on Jan 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.pick;

/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PickType {
	protected boolean isPickVertex,
					isPickEdge,
					isPickFace;
	/**
	 * 
	 */
		
	public PickType()	{
		this(true, false, false);
	}
		
	public PickType(boolean v,boolean e,boolean f) {
		super();
		// TODO Auto-generated constructor stub
		isPickVertex = v;
		isPickEdge = e;
		isPickFace = f;
	}

	/**
	 * @return
	 */
	public boolean isPickEdge() {
		return isPickEdge;
	}

	/**
	 * @return
	 */
	public boolean isPickFace() {
		return isPickFace;
	}

	/**
	 * @return
	 */
	public boolean isPickVertex() {
		return isPickVertex;
	}

	/**
	 * @param b
	 */
	public void setPickEdge(boolean b) {
		isPickEdge = b;
	}

	/**
	 * @param b
	 */
	public void setPickFace(boolean b) {
		isPickFace = b;
	}

	/**
	 * @param b
	 */
	public void setPickVertex(boolean b) {
		isPickVertex = b;
	}

}
