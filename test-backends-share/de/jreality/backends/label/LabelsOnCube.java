package de.jreality.backends.label;

import java.awt.Color;
import java.util.Iterator;

import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.geometry.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;

public class LabelsOnCube {

    
    public static void label(IndexedFaceSet ps) {
      int n=ps.getNumPoints();
      String[] labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = "Point "+i;
      ps.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));

      n=ps.getNumEdges();
      labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = "Edge "+i;
      ps.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));

      n=ps.getNumFaces();
      labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = "Face "+i;
      ps.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

    IndexedFaceSet ifs = new IndexedFaceSet();
    GeometryUtility.calculateAndSetFaceNormals(ifs);
    IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
		label(ifs);
    
    SceneGraphComponent cmp = new SceneGraphComponent();
    Appearance a = new Appearance();
    cmp.setAppearance(a);
    cmp.setGeometry(ifs);
    
    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(a, false);
    DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
    DefaultTextShader ets = (DefaultTextShader) ((DefaultLineShader)dgs.getLineShader()).getTextShader();
    DefaultTextShader fts = (DefaultTextShader) ((DefaultPolygonShader)dgs.getPolygonShader()).getTextShader();
    
    pts.setDiffuseColor(Color.blue);
    ets.setDiffuseColor(Color.orange);
    fts.setDiffuseColor(Color.green);
    
    Double scale = new Double(0.01);
    pts.setScale(scale);
    ets.setScale(scale);
    fts.setScale(scale);
    
    double[] offset = new double[]{-.1,0,0.3};
    pts.setOffset(offset);
    ets.setOffset(offset);
    fts.setOffset(offset);
    
    dgs.setShowPoints(Boolean.TRUE);
    
		String viewer=System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer"); // de.jreality.portal.DesktopPortalViewer");

		ViewerApp.display(cmp);

	}

}
