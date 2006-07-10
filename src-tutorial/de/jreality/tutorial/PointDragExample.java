package de.jreality.tutorial;

import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
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
		DragEventTool t = new DragEventTool();
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
				System.out.println("drag start of vertex no "+e.getIndex());				
			}

			public void pointDragged(PointDragEvent e) {
				System.out.println("dragging vertex no "+e.getIndex()+"; new Position: "+Rn.toString(e.getPosition()));
				PointSet pointSet = e.getPointSet();
				double[][] points=new double[pointSet.getNumPoints()][];
		        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
		        points[e.getIndex()]=e.getPosition();  
		        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));			
			}

			public void pointDragEnd(PointDragEvent e) {
				System.out.println("drag end of vertex no "+e.getIndex());
			}
			
		});
		
		cmp.addTool(t);
		cmp.setGeometry(Primitives.icosahedron());
		cmp.setAppearance(new Appearance());
		cmp.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,0.05);
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    System.setProperty("de.jreality.ui.viewerapp.ViewerApp.synchRender", "true");
    ViewerApp.display(cmp);
	}
}
