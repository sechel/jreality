package de.jreality.tutorial;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.viewerapp.ViewerApp;

public class PointDragExample {

	public static void main(String[] args) {
		SceneGraphComponent cmp = new SceneGraphComponent();

		cmp.setGeometry(Primitives.icosahedron());
		
		cmp.setAppearance(new Appearance());
		cmp.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,0.05);
		cmp.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING,false);
		
		/**tool:*/
		DragEventTool t = new DragEventTool();
		double[] highlightColor = {1,0,0,1};		// highlight point in red
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
				System.out.println("drag start of vertex no "+e.getIndex());				
			}

			public void pointDragged(PointDragEvent e) {
				PointSet pointSet = e.getPointSet();
				double[][] points=new double[pointSet.getNumPoints()][];
		        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
		        points[e.getIndex()]=e.getPosition();  
		        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));			
			}

			public void pointDragEnd(PointDragEvent e) {
			}
			
		});
		
		cmp.addTool(t);

		ViewerApp.display(cmp);
	}
}
