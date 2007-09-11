package de.jreality.tools;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
/** Tool to fly to a picked Point
 *   
 * 
 * @author gonska
 *
 */
public class FlyToPickTool  extends AbstractTool {

	private final transient InputSlot timerSlot = InputSlot.getDevice("SystemTime");
	private final transient InputSlot zoomActivateSlot = InputSlot.getDevice("JumpActivation");// F
	private final transient InputSlot mouseAimer = InputSlot.getDevice("PointerNDC");// F
	private final transient InputSlot reverseSlot = InputSlot.getDevice("Secondary");// F
	
			
	private double flightTime=1000;// time to fly (mil.sec) 
	private double goFactor=0.75;// goes % of the way, then stop 
	private boolean aiming=false;
	private double startTime=0;
	private boolean started=false;
	private SceneGraphComponent arrowNode;
	private boolean attached=false;
	private boolean reverse=false;

	
	// a "P" means "Point" 
	// a "V" means "Vector" 
	// a "W" means "in WorldCoords" 
	// a "A" means "in AvatarCoords"
	// a "S" means "in SceneCoords"
	// a number means the dimension
	private double[] mousePosFirst; 
	private double[] fromW4P;
	private double[] toW4P;
	private double[] axisW3V;
	private double angleW;
	private double[] startMatrixAvatar;
	private SceneGraphPath avaPath;
	private SceneGraphComponent avaNode;
	private AnimatorTask task = null;
    
	public FlyToPickTool() {
		addCurrentSlot(timerSlot);
		addCurrentSlot(zoomActivateSlot);
		arrowNode= new SceneGraphComponent();
		arrowNode.setGeometry(Primitives.arrow(0, 0, 1, 0, 0.2));
		setDescription("type jump: fly to picked Point ");
		setDescription(timerSlot, "");
		setDescription(zoomActivateSlot, 
				"type jump: fly to picked Point \n\r"+
				"hold jump: move mouse to set destinated to fly to \n\r");
		setDescription(reverseSlot, 
				"reverses the flight to zoom away");
		
	}
	  private void assureAttached(ToolContext tc) {
		    if (!attached) tc.getViewer().getSceneRoot().addChild(arrowNode);
		    attached = true;
		    arrowNode.setVisible(true);
		    arrowNode.setAppearance(new Appearance());
		    arrowNode.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
			}

	public void perform(ToolContext tc) {
		if (task==null) init(tc); // only ones
		if (tc.getSource()==zoomActivateSlot
				&& tc.getCurrentPick()!=null
				&& !started
				&& tc.getAxisState(zoomActivateSlot).isPressed()){
			fromW4P=Rn.matrixTimesVector(null, avaPath.getMatrix(null), new double[]{0,0,0,1});
			toW4P=tc.getCurrentPick().getWorldCoordinates();
			// remember mousePos:
			double[] temp=tc.getTransformationMatrix(mouseAimer).toDoubleArray(null);
			
			mousePosFirst=new double[]{temp[3],temp[7]};
			// starting trafo
			Transformation cmpTrafo= avaNode.getTransformation();
			if (cmpTrafo==null) cmpTrafo=new Transformation();
			startMatrixAvatar=cmpTrafo.getMatrix();
			aiming=true;
			arrowNode.setVisible(true);
		}
		if (aiming){
			assureAttached(tc);
			// reverse
			reverse=tc.getAxisState(reverseSlot).isPressed();
			///calculate mouseAiming
			double[] temp=tc.getTransformationMatrix(mouseAimer).toDoubleArray(null);
			double[] relMousePos=new double[]{mousePosFirst[0]-temp[3],mousePosFirst[1]-temp[7]};
			///rotation axis:
			axisW3V=new double[]{relMousePos[1],-relMousePos[0],0,0};
			axisW3V=Rn.matrixTimesVector(null, avaPath.getMatrix(null), axisW3V);// "W" 
			axisW3V= new double[]{axisW3V[0],axisW3V[1],axisW3V[2]};// "3"
			Rn.normalize(axisW3V,axisW3V);
			///angle:
			angleW=Rn.euclideanNorm(relMousePos)*4;
			while(angleW>Math.PI) angleW-=Math.PI*2;
			while(angleW<-Math.PI) angleW+=Math.PI*2;
			/// show DestPoint with arrow:
			double[]a=new double[]{toW4P[0],toW4P[1],toW4P[2]};
			double[]b=new double[]{fromW4P[0],fromW4P[1],fromW4P[2]};
			double dist=Rn.euclideanDistance(a, b);
			MatrixBuilder.euclidean()
			.translate(toW4P).rotate(angleW, axisW3V)
			.rotateFromTo(new double[]{1,0,0}, Rn.subtract(null, fromW4P,toW4P ))
			.scale((1-goFactor)*dist).assignTo(arrowNode);
		}	
		/// start flight
		if (tc.getSource()==zoomActivateSlot
				&& aiming
				&& tc.getAxisState(zoomActivateSlot).isReleased()){
			// compare Mouses
			AnimatorTool.getInstance(tc).schedule(avaNode, task);
			aiming=false;
			arrowNode.setVisible(false);
		}
		
	}
	private void init(ToolContext tc){
		avaPath=tc.getAvatarPath();
		avaNode = avaPath.getLastComponent();
		task= new AnimatorTask(){
			public boolean run(double time, double dt) {
				if(!started) {
					startTime=time;
					started=true;
				}
				double currTime=time-startTime;
				if(currTime>flightTime){
					avaNode.setTransformation(getNextTrafoOfN(1));
					started=false;
					return false;
				}
				avaNode.setTransformation(getNextTrafoOfN(currTime/flightTime));
				return true;
			}
		};
	}
	private Transformation getNextTrafoOfN(double s){
		/// make smoth:
		s=smothFlight(s);
		/// cam to unrotated Dest translation
		double[] moveNearW4V;
		if (!reverse){
			 moveNearW4V=Rn.times(null, goFactor*s,Rn.subtract(null, toW4P, fromW4P));
		}
		else {
			 moveNearW4V=Rn.times(null, (1-(1.0/(1-goFactor)))*s,Rn.subtract(null, toW4P, fromW4P));
		}
		moveNearW4V[3]=1;// to Point
		double[] translMatrixW=MatrixBuilder.euclidean().translate(moveNearW4V).getArray();
		/// rotate on target 
		double[] toTargetMatrixW =MatrixBuilder.euclidean().translate(toW4P).getArray();
		double[]rotateMatrixW=MatrixBuilder.euclidean().rotate(angleW*s, axisW3V).getArray();
		rotateMatrixW= Rn.conjugateByMatrix(null, rotateMatrixW, toTargetMatrixW);

		double[] result=Rn.times(null, rotateMatrixW,translMatrixW);
		result=Rn.times(null,result, startMatrixAvatar);
		return new Transformation(result);
	}
	private double smothFlight(double in){
		return -(Math.cos(in*Math.PI))/2+1.0/2;
	}
	// --------- getter & setter & description---------
	
	@Override
	public String getDescription() {
		return "type jump: fly to picked Point ";
	}
	@Override
	protected void setDescription(InputSlot slot, String description) {}
	public double getFlightTime() {
		return flightTime/1000;
	}
	public void setFlightTime(double flightTime) {
		this.flightTime = flightTime*1000;
	}
	public double getGoFactor() {
		return goFactor;
	}
	public void setGoFactor(double goFactor) {
		this.goFactor = goFactor;
	}
}
