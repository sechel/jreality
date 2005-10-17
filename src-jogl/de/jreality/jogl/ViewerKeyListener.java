/*
 * Created on Jun 17, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import javax.swing.JColorChooser;
import javax.swing.KeyStroke;

import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.tools.ToolManager;
import de.jreality.math.P3;
import de.jreality.scene.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class ViewerKeyListener extends KeyAdapter {
	InteractiveViewer viewer;
	boolean motionToggle = false;
	boolean fullScreenToggle = false;
	HelpOverlay helpOverlay;
	InfoOverlay infoOverlay;
	/**
	 * 
	 */
	public ViewerKeyListener(InteractiveViewer v) {
		super();
		viewer = v;
		//helpOverlay = new HelpOverlay(v);
		helpOverlay = v.getHelpOverlay();
		infoOverlay = v.getInfoOverlay();
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,0), "Increase alpha (1-transparency)");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.SHIFT_DOWN_MASK), "Decrease alpha");
		//helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,0), "Toggle antialiasing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B,0), "Toggle backplane display");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B,InputEvent.SHIFT_DOWN_MASK), "Toggle selection bound display");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C,0), "Set polygon diffuse color in selected appearance");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.SHIFT_DOWN_MASK), "Set background color");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D,0), "Toggle use of display lists");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E,0), "Encompass");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.SHIFT_DOWN_MASK), "Toggle edge drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F,0), "Activate fly tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.SHIFT_DOWN_MASK), "Toggle face drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_G,0), "Toggle fog");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.SHIFT_DOWN_MASK), "Toggle parallel/frenet tubes");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_H,0), "Toggle help overlay");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_I,0), "Toggle info overlay");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_J,0), "Increase sphere radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_J,InputEvent.SHIFT_DOWN_MASK), "Decrease sphere radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_K,0), "Cycle through selection list");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_L,0), "Toggle lighting enabled");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_M,0), "Reset Matrices to default");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.SHIFT_DOWN_MASK), "Set default Matrices with current state");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_N,0), "Add current selection to selection list");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.SHIFT_DOWN_MASK), "Remove current selection from selection list");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_O,0), "Increase level of detail");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.SHIFT_DOWN_MASK), "Decrease level of detail");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P,0), "Toggle perspective/orthographic view");
		//		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0), "Force render");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0), "Toggle interpolate vertex colors in line shader");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.SHIFT_DOWN_MASK), "Orthonormalize camera transform");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_R,0), "Activate rotation tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S,0), "Toggle smooth shading");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.SHIFT_DOWN_MASK), "Toggle sphere drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_T,0), "Activate translation tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.SHIFT_DOWN_MASK), "Toggle tube drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_U,0), "Increase fog factor");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_U,InputEvent.SHIFT_DOWN_MASK), "Decrease fog factor");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_V,0), "Print frame rate");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.SHIFT_DOWN_MASK), "Toggle vertex drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_W,0), "Increase line width/ tube radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.SHIFT_DOWN_MASK), "Decrease line width/tube radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_X,0), "Toggle transparency enabled");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y,0), "Activate selection tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z,0), "Toggle stereo/mono camera");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.SHIFT_DOWN_MASK), "Cycle stereo modes");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,0), "Toggle fullscreen mode");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Quit");
		if ((viewer.getViewingComponent() instanceof GLCanvas))
			((GLDrawable) v.getViewingComponent()).addGLEventListener(helpOverlay);

	}

    boolean encompassToggle = true;
	public void keyPressed(KeyEvent e)	{
			//System.err.println("handling keyboard event");
			switch(e.getKeyCode())	{

				case KeyEvent.VK_A:		// transparency
					modulateValueAdditive(CommonAttributes.TRANSPARENCY,  0.5, .05, 0.0, 1.0, e.isShiftDown());
					break;

				case KeyEvent.VK_B:		// toggle backplane
					if (e.isShiftDown()) {
						viewer.getSelectionManager().setRenderSelection( !viewer.getSelectionManager().isRenderSelection());
						viewer.getSelectionManager().setRenderPick( !viewer.getSelectionManager().isRenderPick());
					} else
						viewer.toggleBackPlane();
					viewer.render();
					break;

				case KeyEvent.VK_C:		// select a color
					java.awt.Color color = JColorChooser.showDialog(viewer.getViewingComponent(), "Select background color",  null);
					if (color == null) break;
					if (e.isShiftDown())	
						viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, color);
					else  {
						viewer.getSelectionManager().getSelectedAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, color);
						viewer.getSelectionManager().getSelectedAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, color);
						viewer.getSelectionManager().getSelectedAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, color);
					}
					viewer.render();
					viewer.render();
					break;
					
				case KeyEvent.VK_D:		// toggle use of display lists
					if (e.isShiftDown()) break;
//					boolean useD = viewer.getRenderer().isUseDisplayLists();
//					viewer.getRenderer().setUseDisplayLists(!useD);
					toggleValue(viewer, CommonAttributes.ANY_DISPLAY_LISTS, viewer.getSceneRoot().getAppearance());
					viewer.render();
//					JOGLConfiguration.theLog.log(Level.INFO,"Using display lists: "+viewer.getRenderer().isUseDisplayLists());
					break;

				case KeyEvent.VK_E:		
					if (!e.isShiftDown()) 	{
//						if (encompassToggle)	
//							CameraUtility.encompass2(viewer);
//						else					
							CameraUtility.encompass(viewer);
						encompassToggle = !encompassToggle;
					}
					else				toggleValue(CommonAttributes.EDGE_DRAW);
					viewer.render();
					break;

				case KeyEvent.VK_F:		// toggle face drawing
					if (e.isShiftDown())		toggleValue(CommonAttributes.FACE_DRAW);
					else viewer.getToolManager().activateTool(ToolManager.CAMERA_FLY_TOOL);
					viewer.render();
					break;

				case KeyEvent.VK_G:		// toggle face drawing
					if (e.isShiftDown())		toggleValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_STYLE);
					else toggleValue(viewer,"fogEnabled", viewer.getSceneRoot().getAppearance());
					viewer.render();
					break;

				case KeyEvent.VK_H:		// toggle help
					if (e.isShiftDown()) helpOverlay.printOut();
					helpOverlay.setVisible(!helpOverlay.isVisible());
					viewer.render();
					break;

				case KeyEvent.VK_I:		// toggle help
					if (e.isShiftDown()) break;
					infoOverlay.setVisible(!infoOverlay.isVisible());
					viewer.render();
					break;

				case KeyEvent.VK_J:		// line width
					modulateValue(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.05,!e.isShiftDown());
					modulateValue(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 2.0,!e.isShiftDown());
				    viewer.render();
					break;

				case KeyEvent.VK_K:		// line width
					if (e.isShiftDown()) break;
					viewer.getSelectionManager().cycleSelectionPaths();
					viewer.render();
					break;

				case KeyEvent.VK_L:		// toggle lighting
					if (e.isShiftDown()) break;
					toggleValue(CommonAttributes.LIGHTING_ENABLED);
					viewer.render();
					break;

				case KeyEvent.VK_M:		// reset matrices
					if (e.isShiftDown()) SceneGraphUtility.setDefaultMatrix(viewer.getSceneRoot());
					else  SceneGraphUtility.resetMatrix(viewer.getSceneRoot());
					viewer.render();
					break;

				case KeyEvent.VK_N:		// line width
					if (e.isShiftDown()) viewer.getSelectionManager().removeSelection(viewer.getSelectionManager().getSelection());
					else viewer.getSelectionManager().addSelection(viewer.getSelectionManager().getSelection());
					viewer.render();
					break;

				case KeyEvent.VK_O:		// line width
					modulateValueAdditive(CommonAttributes.LEVEL_OF_DETAIL, 1.0, .05, 0.0, 1.0, !e.isShiftDown()); 
					viewer.render();
					break;

				case KeyEvent.VK_P:		// toggle perspective
					if (e.isShiftDown()) break;
					boolean val = CameraUtility.getCamera(viewer).isPerspective();
					CameraUtility.getCamera(viewer).setPerspective(!val);
					viewer.render();
					break;

				case KeyEvent.VK_Q:		
					//((GLCanvas) viewer.getViewingComponent()).setNoAutoRedrawMode(false);
					if (e.isShiftDown()){
						Transformation tt =  CameraUtility.getCameraNode(viewer).getTransformation();
						double[] clean = P3.orthonormalizeMatrix(null, tt.getMatrix(), 10E-10, tt.getSignature());
						if (clean != null)	tt.setMatrix(clean);
					}
					else toggleValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.INTERPOLATE_VERTEX_COLORS);
					viewer.render();
					break;

				case KeyEvent.VK_R:		// activate translation tool
					viewer.getToolManager().activateTool(ToolManager.ROTATION_TOOL);
					break;
				
				case KeyEvent.VK_S:		//smooth shading
					if (e.isShiftDown()) toggleValue(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW);
					else {
						toggleValue(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING);
					}
				    viewer.render();
					break;

				case KeyEvent.VK_T:		// activate translation tool
					if (e.isShiftDown()) toggleValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW);
					else viewer.getToolManager().activateTool(ToolManager.TRANSLATION_TOOL);
				    viewer.render();
					break;
				
				case KeyEvent.VK_U:		// line width
					modulateValue(viewer, "fogDensity", .2, !e.isShiftDown(), 1.2, viewer.getSceneRoot().getAppearance());
				    viewer.render();
					break;

				case KeyEvent.VK_V:		// draw vertices
					if (e.isShiftDown()) 					toggleValue(CommonAttributes.VERTEX_DRAW);
					else JOGLConfiguration.theLog.log(Level.INFO,"Frame rate: "+viewer.getRenderer().getFramerate()+" fps");
					viewer.render();
					break;

				case KeyEvent.VK_W:		// line width
					modulateValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 1.0, !e.isShiftDown());
					modulateValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .02, !e.isShiftDown());
				    viewer.render();
					break;

				case KeyEvent.VK_X:		// toggle fast and dirty
					toggleValue(CommonAttributes.TRANSPARENCY_ENABLED);
					// the following is unfortunately unsymmetric: the get and the set don't match
					// this means that if the fastAndDirty attribute has been set somewhere below the root, 
					// (which shouldn't happen!), then this will have unintended results.
//					// In general, however, it would be good to have "rendering hints" 
//					boolean fad;
//					Object foo;
//					foo = viewer.getSceneRoot().getAppearance().getAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED);
//					if (foo instanceof Boolean)		{
//						fad = ((Boolean) foo).booleanValue();
//						System.err.println("Read value of "+fad);
//					}
//					else fad = false;
//					viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED,!fad);
//					foo = viewer.getSceneRoot().getAppearance().getAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED);
//					if (foo instanceof Boolean)		{
//						fad = ((Boolean) foo).booleanValue();
//						System.err.println("After flipping: Read value of "+fad);
//					}
//					viewer.render();
					break;
					
				case KeyEvent.VK_Y:		// activate translation tool
					viewer.getToolManager().activateTool(ToolManager.SELECTION_TOOL);
					break;
				
				
				case KeyEvent.VK_Z:		
					if (e.isShiftDown()) {		// cycle stereo types
						int which = viewer.getStereoType()+1;
						which = (which + 1) % 4;
						viewer.setStereoType(which+1);						
					} else {						// toggle stereo/mono
						Camera cam = CameraUtility.getCamera(viewer);
						cam.setStereo(!cam.isStereo());
						//cam.update();						
					}
					viewer.render();
					break;
				
				case KeyEvent.VK_ESCAPE:		// toggle lighting
					if (e.isShiftDown()) break;
					System.exit(0);
					break;

				//case KeyEvent.VK_BACK_QUOTE:
				case KeyEvent.VK_COMMA:
					if (e.isShiftDown()) break;
					Frame frame = Frame.getFrames()[0];
					fullScreenToggle = !fullScreenToggle;
					frame.dispose();
					frame.setUndecorated(fullScreenToggle);
					frame.show();
					frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullScreenToggle ? frame : null);
			        break;
			}
		}

	private void toggleValue(String  name)	{
		toggleValue(viewer, name,viewer.getSelectionManager().getSelectedAppearance());
	}
	
	public static void toggleValue(InteractiveViewer viewer, String  name)	{
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		toggleValue(viewer, name, ap);
	}
		
	public static void toggleValue(InteractiveViewer viewer, String  name, Appearance ap)	{

		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		if (obj != null && obj instanceof Boolean)	{
			boolean newVal = true;
			newVal = !((Boolean) obj).booleanValue();
			JOGLConfiguration.getLogger().log(Level.INFO,"Toggling property "+name);
			ap.setAttribute(name, newVal);
			viewer.render();
			return;
		} else if (name.indexOf(CommonAttributes.TUBE_STYLE) != -1)	{
			int newV = TubeUtility.PARALLEL;
			int val = TubeUtility.PARALLEL;
			if (obj != null && obj instanceof Integer)		{
				val = ((Integer) obj).intValue();
				if (val == TubeUtility.PARALLEL)	newV = TubeUtility.FRENET;
				else newV = TubeUtility.PARALLEL;
			}
			JOGLConfiguration.theLog.log(Level.INFO,"Tube style is now: "+(newV == TubeUtility.FRENET ? "frenet" : "parallel"));
			ap.setAttribute(name, newV);
			viewer.render();
			return;
		}
		JOGLConfiguration.getLogger().log(Level.INFO,"Turning on property "+name);
		ap.setAttribute(name, true);
		viewer.render();
	}

	private void modulateValue(String name, double val, boolean increase)	{
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		modulateValue(viewer, name, val, increase, 1.2, ap);
	}
	
	public static void modulateValue(InteractiveViewer viewer, String name, double val, boolean increase, double factor, Appearance ap)	{
		//Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		double newVal = val;
		if (obj != null && obj instanceof Double)	{
			newVal = ((Double) obj).doubleValue();
			if (increase) newVal *= factor;
			else newVal /= factor;
		}
		//System.err.println("Setting value "+name+"Object is "+obj+"New value is "+newVal);
			
		ap.setAttribute(name, newVal);
		
		viewer.render();		
	}

	/**
	 * @param string
	 * @param d
	 * @param e
	 * @param f
	 * @param g
	 * @param b
	 */
	private void modulateValueAdditive(String name, double def, double inc, double min, double max, boolean increase) {
		modulateValueAdditive(viewer, name, def, inc, min, max, increase);
	}
		
	public static void modulateValueAdditive(InteractiveViewer viewer, String name, double defawlt, double inc, double min, double max, boolean increase) {
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		double newVal = defawlt;
		if (obj != null && obj instanceof Double)	{
			newVal = ((Double) obj).doubleValue();
			if (increase) newVal +=  inc;
			else newVal -= inc;
		}
		//System.err.println("Setting value "+name+"Object is "+obj+"New value is "+newVal);
		if (newVal < min) newVal = min;
		if (newVal > max) newVal = max;
		ap.setAttribute(name, newVal);
		
		viewer.render();		
	}

}
