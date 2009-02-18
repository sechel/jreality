package de.jreality.tutorial.viewer;

import static de.jreality.shader.CommonAttributes.*;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.jreality.tutorial.util.SelectionRenderer;
import de.jreality.ui.viewerapp.Selection;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerInterface;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;

public class SelectionExample {

	 public static void main(String[] args) throws IOException {
		 SelectionExample selectionExample = new SelectionExample();
		 selectionExample.doIt();
	 }
	 
	SceneGraphPath selection = null, lastSelection;
	SelectionManagerInterface sm = null;
	SelectionRenderer sr;
	ViewerApp viewerApp;
	SceneGraphComponent world;
	DraggingTool dragtool = new DraggingTool();
	RotateTool rotateTool = new RotateTool();
	Color selectionColor = Color.white;
	public void doIt() {
			Color[] faceColors = {new Color(100, 200, 100), new Color(100, 100, 200), new Color(100,200,200), new Color(200,100,100)};
		    IndexedFaceSet ico = Primitives.sharedIcosahedron;
		    world = SceneGraphUtility.createFullSceneGraphComponent("world");
		    // set up a hierarchy of scene graph components
			SceneGraphComponent sgcGeom = SceneGraphUtility.createFullSceneGraphComponent("sgcGeom");
			sgcGeom.setGeometry(ico);
		    for (int i = 0; i<2; ++i)	{
				SceneGraphComponent sgc0 = SceneGraphUtility.createFullSceneGraphComponent("sgc"+i);
				MatrixBuilder.euclidean().translate(-2+4*i, 0, 0).assignTo(sgc0);
				world.addChild(sgc0);
		    	for (int j = 0; j<2; ++j)	{
		    		SceneGraphComponent sgc1 = SceneGraphUtility.createFullSceneGraphComponent("sgc"+i+j);
	    			MatrixBuilder.euclidean().translate(0,-2+4*j, 0).assignTo(sgc1);
	    			sgc0.addChild(sgc1);
		    		for (int k = 0; k<2; ++k)	{
		    			SceneGraphComponent sgc2 = SceneGraphUtility.createFullSceneGraphComponent("sgc"+i+j+k);
		    			sgc1.addChild(sgc2);
		    			// set translation onto corner of a cube
		    			MatrixBuilder.euclidean().translate(0, 0, -2+4*k).scale(1.5).assignTo(sgc2);
		    			// set same geometry 
		    			sgc2.addChild(sgcGeom);
		    			// set appearance individually
		    			sgc2.getAppearance().setAttribute(DIFFUSE_COLOR, faceColors[2*j+k]);
		    		}
		    	}
		    }
		    DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(world);
		    Tool selectionTool = new AbstractTool(
		    		InputSlot.getDevice("PrimarySelection"),		// right mouse
		    		InputSlot.getDevice("SecondarySelection")) 		// shift-right mouse
		    {
		    	
				private SceneGraphComponent selectedComponent;

				public void deactivate(ToolContext tc) {
					if (tc.getCurrentPick() == null) return;
					selection = tc.getRootToLocal();
					if (tc.getSource().equals(InputSlot.getDevice("SecondarySelection"))) {
						// on shift down, restore matrices to original state
						DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(world, true);
						return;
					}
					if (lastSelection != null && selection.isEqual(lastSelection)) {
						sm.cycleSelectionPath();
						while (sm.getSelectionPath().getLastComponent().getName().indexOf("layer") != -1)
							sm.cycleSelectionPath();
					} else {
						sm.setSelection(new Selection(selection));
						lastSelection = new SceneGraphPath(selection);
					}
					if (selectedComponent != null) {
						selectedComponent.removeTool(dragtool);
						selectedComponent.removeTool(rotateTool);
						if (selectedComponent.getAppearance() != null)
							selectedComponent.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR,Appearance.INHERITED);
					}
					selectedComponent = sm.getSelectionPath().getLastComponent();
					selectedComponent.addTool(dragtool);
					selectedComponent.addTool(rotateTool);
					if (selectedComponent.getAppearance() != null)
						selectedComponent.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, selectionColor);
				}

				public String getDescription(InputSlot slot) {
					return null;
				}

				public String getDescription() {
					return "A tool which paints on a 3D surface";
				}
		    	
		    };
			world.addTool(selectionTool);
			
		    viewerApp = new ViewerApp(world);
		    sm = SelectionManager.selectionManagerForViewer(viewerApp.getViewerSwitch());
		    SceneGraphPath toWorld = SceneGraphUtility.getPathsBetween(viewerApp.getViewerSwitch().getSceneRoot(), world).get(0);
		    sm.setDefaultSelection(new Selection(toWorld));
			sr = new SelectionRenderer(viewerApp.getViewerSwitch() );
			sr.setSelectionPath(sm.getSelectionPath());
			sr.setVisible(true);
			viewerApp.update();
			viewerApp.display();

		}

}
