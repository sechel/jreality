/*
 * Created on Jun 4, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package de.jreality.portal.tools;

import javax.swing.text.NavigationFilter;

import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.WandMotionListener;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.FactoredTransformation;
import de.jreality.util.BoundingBoxTraversal;
import de.jreality.util.P3;
import de.jreality.util.Rectangle3D;
import de.jreality.util.Rn;

/**
 * @author gollwas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class WandTool implements WandListener, WandMotionListener {

	
	/*************** portal event listener implementation ***************/
	
	FactoredTransformation actOnStartTransformation = new FactoredTransformation();
	FactoredTransformation currentWandMatrix = new FactoredTransformation();
	FactoredTransformation headTransform = new FactoredTransformation();
	FactoredTransformation wandDeltaFractionMatrix = new FactoredTransformation();
	FactoredTransformation wandDeltaMatrix = new FactoredTransformation();
	FactoredTransformation wandStartMatrix = new FactoredTransformation();
	FactoredTransformation wandTransformation;
	FactoredTransformation wandRealTransformation = new FactoredTransformation();
	FactoredTransformation worldTransform = new FactoredTransformation();
	//Transformation wandOffset = new Transformation();
	private boolean move;
	private boolean rotate;
	private boolean showSkybox;
	double[] wandStartPoint = new double[4];
	private static final boolean DEBUG = true;
	
	SceneGraphComponent navComponent, wandComponent;
	
	public WandTool(SceneGraphComponent navigationNode, SceneGraphComponent wandNode) {
		this.navComponent = navigationNode;
		this.wandComponent = wandNode;
		if (navComponent.getTransformation() == null) navComponent.setTransformation(new FactoredTransformation());
		if (wandComponent.getTransformation() == null) wandComponent.setTransformation(new FactoredTransformation());
		actOnStartTransformation.setMatrix(navComponent.getTransformation().getMatrix());
		wandTransformation = wandComponent.getTransformation();
		worldTransform.setMatrix(navComponent.getTransformation().getMatrix());
	}
	
	FactoredTransformation wand = new FactoredTransformation();
	
	public void axisMoved(WandEvent event) {
		if (!navigationEnabled) return;
//		if (event.buttonPressed(1)) { // zoom
//			Transformation wand = new Transformation(P3.transposeF2D(
//					new double[16], event.getMatrix()));
//			Transformation scale = new Transformation();
//			double val = event.getMainAxisValue();
//			val *= 1.1;
//			scale.setStretch(1. + (val * val * val * 0.1) );
//			worldTransform.multiplyOnLeft(wand.getInverse());
//			worldTransform.multiplyOnLeft(scale);
//			worldTransform.multiplyOnLeft(wand);
//			navComponent.getTransformation().setMatrix(worldTransform.getMatrix());
//		} else { // move
			
			//double[] wandDeltaM = new double[16];
			//Rn.times(wandDeltaM, wandTransformation.getMatrix(), oldWandMatrix
			//		.getInverse().getMatrix());
			
			setWandMatrix(event, wand);
			FactoredTransformation move = new FactoredTransformation();
			//move.setCenter(wand.getInverse().getCenter());
			//move.setRotation(wand.getInverse().getRotationAngle()*0.01, wand.getInverse().getRotationAxis());
			//move.setMatrix(wandTransformation.getMatrix());
			//move.setRotationAngle(move.getRotationAngle()*0.01);
			double x = event.getAxisValue(0);
			double y = event.getAxisValue(1);
			x *= -1.1;
			y *= 1.1;
			double dx = x * x * x * 0.1;
			double dy = y * y * y * 0.1;
			move.setTranslation(dx, 0,  dy);
			worldTransform.multiplyOnLeft(wand.getInverse());
			worldTransform.multiplyOnLeft(move);
			worldTransform.multiplyOnLeft(wand);
			navComponent.getTransformation().setMatrix(worldTransform.getMatrix());
//		}
	}

	public void buttonPressed(WandEvent arg0) {
			setWandMatrix(arg0, wandStartMatrix);
			wandStartPoint = wandStartMatrix.getTranslation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see szg.framework.event.WandListener#buttonReleased(szg.framework.event.WandEvent)
	 */
	public void buttonReleased(WandEvent arg0) {
	}

	public void buttonTipped(WandEvent arg0) {
		if (arg0.getButton() == 2) {
				worldTransform.setMatrix(actOnStartTransformation.getMatrix());
		}
	}
    
  /**
   * this method simply trnaslates the center of the boundingbox to (0,2,-2);
   * 
   * @param root
   * @return
   */
    public void center() {
        BoundingBoxTraversal bbv = new BoundingBoxTraversal();
        bbv.traverse(navComponent);
        Rectangle3D worldBox = bbv.getBoundingBox();
        FactoredTransformation t = new FactoredTransformation();
        double[] transl = worldBox.getCenter();
        transl[1] -= 2; transl[2] += 2;
        t.setTranslation(transl);
        worldTransform.multiplyOnRight(t.getInverse());
        navComponent.getTransformation().setMatrix(worldTransform.getMatrix());
    }

    
    public void wandDragged(WandEvent arg0) {
		setWandMatrix(arg0, wandTransformation);
		// rotate & translate
		if (navigationEnabled && arg0.getButton() == 0) {
				setWandMatrix(arg0, currentWandMatrix);
				double[] wandDeltaM = new double[16];
				Rn.times(wandDeltaM, currentWandMatrix.getMatrix(), wandStartMatrix
						.getInverse().getMatrix());
				wandDeltaMatrix.setMatrix(wandDeltaM);
				wandStartMatrix.setMatrix(currentWandMatrix.getMatrix());
				worldTransform.multiplyOnLeft(wandDeltaM);
				navComponent.getTransformation().setMatrix(worldTransform.getMatrix());
		}
	}

	FactoredTransformation oldWandMatrix = new FactoredTransformation();
	private boolean navigationEnabled;

	public void wandMoved(WandEvent arg0) {
		oldWandMatrix.setMatrix(wandTransformation.getMatrix());
		setWandMatrix(arg0, wandTransformation);
	}

	private void setWandMatrix(WandEvent arg0, FactoredTransformation t) {
		t.setMatrix(Rn.transposeF2D(new double[16], arg0
				.getMatrix()));
		//t.multiplyOnRight(wandOffset.getInverse());
	}

	/**
	 * @param navigationEnabled
	 */
	public void setNavigationEnabled(boolean navigationEnabled) {
		this.navigationEnabled = navigationEnabled;
	}


}
