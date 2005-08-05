/*
 * Created on Dec 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.portal.tools;

import java.util.Iterator;
import java.util.List;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.tools.UserTool;
import de.jreality.jogl.tools.UserToolInterface;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickPoint;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JoglBoxTool extends SceneGraphVisitor implements UserToolInterface {
	public void registerHelp(HelpOverlay ho) {
		ho.registerInfoString("JoglBoxTool: ","no info available");

	}
	UserTool boxTool = null;
	InteractiveViewer viewer;
   
	int currentEvent = -1;
	private SceneGraphPath path;
	private static final int TRACK = 11;
	private static final int START = 12;
	private static final int END = 13;
	private boolean debug = true;
	private BoxContext context;
	
	/**
	 * 
	 */
	public JoglBoxTool(InteractiveViewer v) {
		super();
		viewer = v;
		addBoxTool();
		path = new SceneGraphPath();
	}
	
	public void addBoxTool()	{
		boxTool = new UserTool();
		boxTool.addListener(this);
		viewer.getToolManager().addUserTool(boxTool, "B", "find enclosing boxes");
	}
	
	double[] anchorPointNDC; //, anchorPointWorld = null;
	double[] currentPoint = new double[4];
	double[] NDCToWorld = null;
	Graphics3D gc = null;
	private double[] worldToPoint;
	public void startTracking(UserTool t) {
		currentEvent = START;
		//if (debug) System.out.println("Start tracking");
		anchorPointNDC = t.getPointNDC();
//		anchorPointWorld = t.getPickPoint().getPointWorld();
		if (t.getPickPoint() == null) {
			return;
		}
		gc = t.getPickPoint().getContext();
		double[] currentNDC = t.getPointNDC();
		try {
			NDCToWorld = gc.getNDCToWorld();
			double[] currentWorld = Rn.matrixTimesVector(null, NDCToWorld, currentNDC);
			if (debug) System.out.println("Start tracking: "+currentWorld[0]+", "+currentWorld[1]+", "+currentWorld[2]);
			worldToPoint = P3.makeTranslationMatrix(null, currentWorld, Pn.EUCLIDEAN);
			visit(viewer.getSceneRoot());
		} catch (NullPointerException ne) {}
	}
	
	public void track(UserTool t) {
		currentEvent = TRACK;
		double[] currentNDC = t.getPointNDC();
		currentNDC[2] = anchorPointNDC[2];		// we're working in a plane parallel to the screen
		try {
			double[] currentWorld = Rn.matrixTimesVector(null, NDCToWorld, currentNDC);
			if (debug) System.out.println("tracking: "+currentWorld[0]+", "+currentWorld[1]+", "+currentWorld[2]);
			worldToPoint = P3.makeTranslationMatrix(null, currentWorld, Pn.EUCLIDEAN);
			visit(viewer.getSceneRoot());
		} catch (NullPointerException ne) {}
	}

	public void endTracking(UserTool t) {
		currentEvent = END;
		visit(viewer.getSceneRoot());
		if (debug) System.out.println("End tracking");
	}
	
	BoxContext createContext() {
		return new BoxContext() {

			public Transformation getLocalTransformation() {
				return new Transformation(Rn.times(null, path.getInverseMatrix(null), worldToPoint));
			}

			public int getButton() {
				return 1;
			}

			public SceneGraphPath getRootToLocal() {
				return path;
			}
			
		};
	}
	
	public void visit(SceneGraphComponent sg) {
		path.push(sg);
		processBoxes(sg.getTools());
		sg.childrenAccept(this);
		path.pop();
	}

	/**
	 * @param tools
	 */
	private void processBoxes(List tools) {
		if (tools.isEmpty()) return;
		for (Iterator i = tools.iterator(); i.hasNext(); ) {
			context = createContext();
			EventBox box = (EventBox) i.next();
			if (debug) System.out.println("event type: "+currentEvent);
			switch(currentEvent) {	
				case TRACK:
					if (debug) System.out.println("ID_WAND_DRAGGED");
					box.processDrag(context);
				break;
								
				case START:
					if (debug) System.out.println("ID_BUTTON_PRESSED");
					box.processButtonPress(context);
				break;
				
				case END:
					if (debug) System.out.println("ID_BUTTON_RELEASED");
					box.processButtonRelease(context);
				break;
			}
		}
	}


}
