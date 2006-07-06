package de.jreality.tools;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class FaceDragEventTool extends AbstractTool {
	  protected FaceDragListener dragListener;
	  
	    private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	    
	    public FaceDragEventTool(String dragSlotName) {
	      super(InputSlot.getDevice(dragSlotName));
	      addCurrentSlot(pointerSlot, "triggers face-drag events");
	    }
	    
	    public FaceDragEventTool() {
	      this("FaceDragActivation");
	    }

	    protected boolean active;
	    protected IndexedFaceSet faceSet;
	    protected int index=-1;
	    protected double[] pickPoint;
	    
	    private Matrix pointerToPoint = new Matrix();
	    
	    public void activate(ToolContext tc) {
	        if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE) {
	            active = true;
	            faceSet = (IndexedFaceSet) tc.getCurrentPick().getPickPath().getLastElement();
	            index=tc.getCurrentPick().getIndex();
	            tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerToPoint.getArray());
	            pointerToPoint.invert();
	            pointerToPoint.multiplyOnRight(tc.getRootToLocal().getMatrix(null));
	            
	            pickPoint=tc.getCurrentPick().getObjectCoordinates();
	            if(pickPoint.length==3) Pn.homogenize(pickPoint,pickPoint);
	            MatrixBuilder.euclidean(pointerToPoint).translate(pickPoint); 
	            
	            fireFaceDragStart(new double[]{0,0,0,1});        
	        }
	        else tc.reject();
	    }
	    
	    Matrix result= new Matrix();;
	    
	    public void perform(ToolContext tc) {
	        if (!active) return;
	        tc.getTransformationMatrix(pointerSlot).toDoubleArray(result.getArray());
	        result.multiplyOnRight(pointerToPoint);
	        result.multiplyOnLeft(tc.getRootToLocal().getInverseMatrix(null));
	        
	        double[] translation=Rn.subtract(null,result.getColumn(3),pickPoint);
	        if(translation[3]!=0) fireFaceDragged(translation);
	        else fireFaceDragged(new double[] {translation[0],translation[1],translation[2],1});
	    }

	    public void deactivate(ToolContext tc) {
	      if (!active) return;      
	      fireFaceDragEnd(new double[]{0,0,0,1});
	      index=-1;
	      faceSet=null;
	      active = false;
	    }
	    
	    public void addFaceDragListener(FaceDragListener listener) {
	      dragListener = FaceDragEventMulticaster.add(dragListener, listener);
	    }

	    public void removeFaceDragListener(FaceDragListener listener) {
	      dragListener = FaceDragEventMulticaster.remove(dragListener, listener);
	    }
	    
	    protected void fireFaceDragStart(double[] translation) {
	      final FaceDragListener l=dragListener;
	      if (l != null) l.faceDragStart(new FaceDragEvent(faceSet, index, translation));
	    }

	    protected void fireFaceDragged(double[] translation) {
	      final FaceDragListener l=dragListener;
	      if (l != null) l.faceDragged(new FaceDragEvent(faceSet, index, translation));
	    }
	    
	    protected void fireFaceDragEnd(double[] translation) {
	      final FaceDragListener l=dragListener;
	      if (l != null) l.faceDragEnd(new FaceDragEvent(faceSet, index, translation));
	    }
}
