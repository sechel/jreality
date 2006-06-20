package de.jreality.scene.tool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import de.jreality.geometry.CoordinateSystemFactory;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphPathObserver;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;

public class CoordinateSystemBeautifier extends Tool {

	transient List activationSlots = new LinkedList();
	transient List usedSlots = new LinkedList();

	//from RotateTool:
	static InputSlot evolutionSlot = InputSlot.getDevice("TrackballTransformation");
	static InputSlot camPath = InputSlot.getDevice("WorldToCamera");
	
	private CoordinateSystemFactory factory = null;;
	private boolean initialized = false;
	
	
	public CoordinateSystemBeautifier(CoordinateSystemFactory factory) {
		this.factory = factory;
		
		usedSlots.add(evolutionSlot);
		usedSlots.add(camPath);  //not necessary
	}

	public void perform(ToolContext tc) {
		//add SceneGraphPathObserver to RootToToolPath
		//and only perform something if TransformationEvent is thrown
		if (!initialized) {
			//initialize paths
			final SceneGraphPath rootToToolPath = tc.getRootToToolComponent();
			final SceneGraphPath cameraToRootPath = tc.getViewer().getCameraPath();
			//add path observer to path from root to tool
		    SceneGraphPathObserver opObserver = new SceneGraphPathObserver(rootToToolPath);
		    opObserver.addTransformationListener(new TransformationListener(){
		    	public void transformationMatrixChanged(TransformationEvent ev){
					//get transformation from camera to tool
		    		double[] rootToTool = rootToToolPath.getInverseMatrix(null);
					double[] cameraToRoot = cameraToRootPath.getMatrix(null);
					double[] cameraToTool = Rn.times(null, rootToTool, cameraToRoot);
					//update box
					factory.updateBox(cameraToTool);
		    	}
		    });
		    //update box initially
		    opObserver.transformationMatrixChanged(null);

			initialized = true;
		}
		//else do nothing
	}
	

	public List getActivationSlots() {
		return activationSlots;
	}

	public List getCurrentSlots() {
		return usedSlots;
	}

	public List getOutputSlots() {
		return Collections.EMPTY_LIST;
	}

	public void activate(ToolContext tc) {
		//is never executed since activationSlots.isEmpty()
	}

	public void deactivate(ToolContext tc) {
		
	}

}
