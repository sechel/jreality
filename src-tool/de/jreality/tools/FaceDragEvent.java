package de.jreality.tools;

import java.util.EventObject;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;

public class FaceDragEvent extends EventObject {
    
	private static final long serialVersionUID = 19823L;

    private final int index;
    private final double[] translation;
	private final IndexedFaceSet faceSet;
  
	public FaceDragEvent(IndexedFaceSet faceSet, int index, double[] translation) {
		super(faceSet);
    this.faceSet=faceSet;
    this.index=index;
    this.translation = (double[])translation.clone();
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
  
  /**
   * BE CAREFUL: this method uses the face index when the drag started. So it makes only sense to use it
   * when the combinatorics of the indexed face set was not changed while dragging.
   * @return an array containing the indices of the face vertices
   * @throws ArrayIndexOutOfBoundsException
   */
  public int[] getFaceIndices() {
	  return faceSet.getFaceAttributes(Attribute.INDICES).toIntArrayArray().getValueAt(index).toIntArray(null);
  }  

  /**
   * BE CAREFUL: this method uses the face index when the drag started. So it makes only sense to use it
   * when the combinatorics of the indexed face set was not changed while dragging.
   * @return an array containing the face vertices
   * @throws ArrayIndexOutOfBoundsException
   */
  public double[][] getFaceVertices(){
	  int []faceIndices = getFaceIndices();
	  double[][] faceVertices=new double[faceIndices.length][];
			for(int i=0;i<faceIndices.length;i++)
				faceVertices[i]=faceSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(faceIndices[i]).toDoubleArray(null);
			return faceVertices;
  }  
  public IndexedFaceSet getIndexedFaceSet() {
    return faceSet;
  }
}