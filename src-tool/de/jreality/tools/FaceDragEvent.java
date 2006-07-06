package de.jreality.tools;

import java.util.EventObject;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;

public class FaceDragEvent extends EventObject {
    
	private static final long serialVersionUID = 19823L;

    private final int index;
    private final double[] translation;
	private final IndexedFaceSet faceSet;
	private int[] faceIndices;
	private double[][] faceVertices;
  
	public FaceDragEvent(IndexedFaceSet faceSet, int index, double[] translation) {
		super(faceSet);
    this.faceSet=faceSet;
    this.index=index;
    this.translation = (double[])translation.clone();
    this.faceIndices = faceSet.getFaceAttributes(Attribute.INDICES).toIntArrayArray().getValueAt(index).toIntArray(null);
	this.faceVertices=new double[faceIndices.length][];
	for(int i=0;i<faceIndices.length;i++)
		faceVertices[i]=faceSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(faceIndices[i]).toDoubleArray(null);
	}
	
	/** The x-coordinate of this event's translation. */
	public double getX() {
		return translation[0];
	}
	
	/** The y-coordinate of this event's translation. */
	public double getY() {
		return translation[1];
	}
	
	/** The z-coordinate of this event's translation. */
	public double getZ() {
		return translation[2];
	}

  public double[] getTranslation() {
    return (double[]) translation.clone();
  }
  
  public int getIndex() {
	  return index;
  }
  public int[] getFaceIndices() {
	  return faceIndices;
  }  
  public double[][] getFaceVertices(){
	  return faceVertices;
  }  
  public IndexedFaceSet getIndexedFaceSet() {
    return faceSet;
  }
}