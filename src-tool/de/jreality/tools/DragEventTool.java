package de.jreality.tools;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;


public class DragEventTool extends AbstractTool {
	

	protected PointDragListener pointDragListener;
	protected LineDragListener lineDragListener;
	protected FaceDragListener faceDragListener;
	
    private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	
	public DragEventTool(String dragSlotName){
		super(InputSlot.getDevice(dragSlotName));
		addCurrentSlot(pointerSlot, "triggers drag events");
	}
	
	public DragEventTool(){
		  this("AllDragActivation");
	}
	
    protected boolean active;
    protected PointSet pointSet;
    protected IndexedLineSet lineSet;
    protected IndexedFaceSet faceSet;
    protected int index=-1;
    protected double[] pickPoint;
    private int pickType=PickResult.PICK_TYPE_OBJECT;
    
    private Matrix pointerToPoint = new Matrix();
    
	public void activate(ToolContext tc) {
		  active = true;
          tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerToPoint.getArray());
          pointerToPoint.invert();
          pointerToPoint.multiplyOnRight(tc.getRootToLocal().getMatrix(null));
          
	      if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT) {
	    	  pickType=PickResult.PICK_TYPE_POINT;
	          pointSet = (PointSet) tc.getCurrentPick().getPickPath().getLastElement();
	          index=tc.getCurrentPick().getIndex();
	          double[] point = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index).toDoubleArray(null);
	          MatrixBuilder.euclidean(pointerToPoint).translate(point);      
	          firePointDragStart(point);        
	      }else if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE) {	            
	    	  pickType=PickResult.PICK_TYPE_LINE;
	    	  lineSet = (IndexedLineSet) tc.getCurrentPick().getPickPath().getLastElement();
	    	  index=tc.getCurrentPick().getIndex();	            
	    	  pickPoint=tc.getCurrentPick().getObjectCoordinates();
	    	  if(pickPoint.length==3) Pn.homogenize(pickPoint,pickPoint);
	    	  MatrixBuilder.euclidean(pointerToPoint).translate(pickPoint);	            
	    	  fireLineDragStart(new double[]{0,0,0,1});        
	      }else if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE) {
	    	  pickType=PickResult.PICK_TYPE_FACE;
	    	  faceSet = (IndexedFaceSet) tc.getCurrentPick().getPickPath().getLastElement();
	    	  index=tc.getCurrentPick().getIndex();
	    	  pickPoint=tc.getCurrentPick().getObjectCoordinates();
	    	  if(pickPoint.length==3) Pn.homogenize(pickPoint,pickPoint);
	    	  MatrixBuilder.euclidean(pointerToPoint).translate(pickPoint); 	            
	    	  fireFaceDragStart(new double[]{0,0,0,1});        
	      }else tc.reject();
	}

	Matrix result=new Matrix();
	
	public void perform(ToolContext tc) {		
		if (!active) return;
        tc.getTransformationMatrix(pointerSlot).toDoubleArray(result.getArray());
        result.multiplyOnRight(pointerToPoint);
        result.multiplyOnLeft(tc.getRootToLocal().getInverseMatrix(null));
	    if (pickType == PickResult.PICK_TYPE_POINT) {
	        firePointDragged(result.getColumn(3));
	    }else if (pickType == PickResult.PICK_TYPE_LINE) {	
	        double[] translation=Rn.subtract(null,result.getColumn(3),pickPoint);
	        if(translation[3]!=0) fireLineDragged(translation);
	        else fireLineDragged(new double[] {translation[0],translation[1],translation[2],1});    	
	    }else if (pickType == PickResult.PICK_TYPE_FACE) {
	    	double[] translation=Rn.subtract(null,result.getColumn(3),pickPoint);
	        if(translation[3]!=0) fireFaceDragged(translation);
	        else fireFaceDragged(new double[] {translation[0],translation[1],translation[2],1});	    	
	    }
	}

	public void deactivate(ToolContext tc) {
		  if (!active) return;   
	      if (pickType == PickResult.PICK_TYPE_POINT) firePointDragEnd(new double[]{0,0,0,1});
	      else if (pickType == PickResult.PICK_TYPE_LINE) fireLineDragEnd(new double[]{0,0,0,1});
	      else if (pickType == PickResult.PICK_TYPE_FACE) fireFaceDragEnd(new double[]{0,0,0,1});
	      index=-1;
	      pointSet=null;
	      lineSet=null;
	      faceSet=null;
	      active = false;	
	      result=new Matrix();
	      pickType=PickResult.PICK_TYPE_OBJECT;
	}	
	
    public void addPointDragListener(PointDragListener listener) {
        pointDragListener = PointDragEventMulticaster.add(pointDragListener, listener);
    }
    public void removePointDragListener(PointDragListener listener) {
    	pointDragListener = PointDragEventMulticaster.remove(pointDragListener, listener);
    }    
    public void addLineDragListener(LineDragListener listener) {
        lineDragListener = LineDragEventMulticaster.add(lineDragListener, listener);
    }
    public void removeLineDragListener(LineDragListener listener) {
    	lineDragListener = LineDragEventMulticaster.remove(lineDragListener, listener);
    }    
    public void addFaceDragListener(FaceDragListener listener) {
    	faceDragListener = FaceDragEventMulticaster.add(faceDragListener, listener);
    }
    public void removeFaceDragListener(FaceDragListener listener) {
    	faceDragListener = FaceDragEventMulticaster.remove(faceDragListener, listener);
    }
	
    protected void firePointDragStart(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragStart(new PointDragEvent(pointSet, index, location));
    }
    protected void firePointDragged(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragged(new PointDragEvent(pointSet, index, location));
    }      
    protected void firePointDragEnd(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragEnd(new PointDragEvent(pointSet, index, location));
    }
    
	protected void fireLineDragStart(double[] translation) {
	    final LineDragListener l=lineDragListener;
		if (l != null) l.lineDragStart(new LineDragEvent(lineSet, index, translation));
	}
    protected void fireLineDragged(double[] translation) {
		final LineDragListener l=lineDragListener;
		if (l != null) l.lineDragged(new LineDragEvent(lineSet, index, translation));
	}
	protected void fireLineDragEnd(double[] translation) {
		final LineDragListener l=lineDragListener;
		if (l != null) l.lineDragEnd(new LineDragEvent(lineSet, index, translation));
	}
		   
	protected void fireFaceDragStart(double[] translation) {
		final FaceDragListener l=faceDragListener;
		if (l != null) l.faceDragStart(new FaceDragEvent(faceSet, index, translation));
	}
    protected void fireFaceDragged(double[] translation) {
		final FaceDragListener l=faceDragListener;
		if (l != null) l.faceDragged(new FaceDragEvent(faceSet, index, translation));
	}
    protected void fireFaceDragEnd(double[] translation) {
		final FaceDragListener l=faceDragListener;
		if (l != null) l.faceDragEnd(new FaceDragEvent(faceSet, index, translation));
	}
}
