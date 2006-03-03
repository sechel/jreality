package de.jreality.backends.label;

import javax.media.StopAtTimeEvent;

import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;

public class LabelsOnPointSet {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numPoints = 100;
		double[] points = new double[3*numPoints];
		String[] labels = new String[numPoints];
		for (int i = 0; i < numPoints; i++) {
			points[3*i] = Math.random()*10;
			points[3*i+1] = Math.random()*10;
			points[3*i+2] = Math.random()*10;
			labels[i] = "This is point "+i; 
		}
		PointSet pSet = new PointSet();
		pSet.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_INLINED.createReadOnly(points));
		pSet.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
		
		Appearance a = new Appearance();
		a.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		a.setAttribute(CommonAttributes.EDGE_DRAW,false);
		a.setAttribute("pointShader.scale", .01);
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setAppearance(a);
		sgc.setGeometry(pSet);
		
		 String viewer=System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer de.jreality.jogl.Viewer"); // de.jreality.portal.DesktopPortalViewer");

		ViewerApp.display(sgc);

	}

}
