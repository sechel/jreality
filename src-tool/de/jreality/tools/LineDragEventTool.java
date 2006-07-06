package de.jreality.tools;

import java.util.Collections;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class LineDragEventTool extends AbstractTool {
	  protected LineDragListener dragListener;
	  
	    private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	    
	    public LineDragEventTool(String dragSlotName) {
	    	super(InputSlot.getDevice(dragSlotName));
			addCurrentSlot(pointerSlot, "triggers line-drag events");
	    }
	    
	    public LineDragEventTool() {
	      this("LineDragActivation");
	    }

	    protected boolean active;
	    protected IndexedLineSet lineSet;
	    protected int index=-1;
	    protected double[] pickPoint;
	    
	    private Matrix pointerToPoint = new Matrix();
	    
	    public void activate(ToolContext tc) {
	        if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE) {
	            active = true;
	            lineSet = (IndexedLineSet) tc.getCurrentPick().getPickPath().getLastElement();
	            index=tc.getCurrentPick().getIndex();
	            tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerToPoint.getArray());
	            pointerToPoint.invert();
	            pointerToPoint.multiplyOnRight(tc.getRootToLocal().getMatrix(null));
	            
	            pickPoint=tc.getCurrentPick().getObjectCoordinates();
	            if(pickPoint.length==3) Pn.homogenize(pickPoint,pickPoint);
	            MatrixBuilder.euclidean(pointerToPoint).translate(pickPoint); 
	            
	            fireLineDragStart(new double[]{0,0,0,1});        
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
	        if(translation[3]!=0) fireLineDragged(translation);
	        else fireLineDragged(new double[] {translation[0],translation[1],translation[2],1});
	    }

	    public void deactivate(ToolContext tc) {
	      if (!active) return;      
	      fireLineDragEnd(new double[]{0,0,0,1});
	      index=-1;
	      lineSet=null;
	      active = false;
	    }
	    
	    public void addLineDragListener(LineDragListener listener) {
	      dragListener = LineDragEventMulticaster.add(dragListener, listener);
	    }

	    public void removeLineDragListener(LineDragListener listener) {
	      dragListener = LineDragEventMulticaster.remove(dragListener, listener);
	    }
	    
	    protected void fireLineDragStart(double[] translation) {
	      final LineDragListener l=dragListener;
	      if (l != null) l.lineDragStart(new LineDragEvent(lineSet, index, translation));
	    }

	    protected void fireLineDragged(double[] translation) {
	      final LineDragListener l=dragListener;
	      if (l != null) l.lineDragged(new LineDragEvent(lineSet, index, translation));
	    }
	    
	    protected void fireLineDragEnd(double[] translation) {
	      final LineDragListener l=dragListener;
	      if (l != null) l.lineDragEnd(new LineDragEvent(lineSet, index, translation));
	    }
}
