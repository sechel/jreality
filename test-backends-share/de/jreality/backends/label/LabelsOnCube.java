package de.jreality.backends.label;

import java.util.Iterator;

import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.geometry.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;

public class LabelsOnCube {

    
    public static void label(IndexedFaceSet ps) {
      int n=ps.getNumPoints();
      String[] labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = ""+i;
      ps.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
      for (Iterator iter = ps.getVertexAttributes(Attribute.LABELS).toStringArray().iterator(); iter.hasNext();) {
        Object o = iter.next();
        System.out.println(o.getClass().getName()+": "+o);
      }

      n=ps.getNumEdges();
      labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = ""+i;
      ps.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
      for (Iterator iter = ps.getVertexAttributes(Attribute.LABELS).toStringArray().iterator(); iter.hasNext();) {
        Object o = iter.next();
        System.out.println(o.getClass().getName()+": "+o);
      }

      n=ps.getNumFaces();
      labels=new String[n];
      for (int i = 0; i<n; i++) labels[i] = ""+i;
      ps.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
      for (Iterator iter = ps.getVertexAttributes(Attribute.LABELS).toStringArray().iterator(); iter.hasNext();) {
        Object o = iter.next();
        System.out.println(o.getClass().getName()+": "+o);
      }
}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

    IndexedFaceSet ifs = new IndexedFaceSet();
    GeometryUtility.calculateAndSetFaceNormals(ifs);
    IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
		label(ifs);
		String viewer=System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer"); // de.jreality.portal.DesktopPortalViewer");

		ViewerApp.display(ifs);

	}

}
