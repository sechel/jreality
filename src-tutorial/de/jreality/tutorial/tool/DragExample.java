package de.jreality.tutorial.tool;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tools.LineDragEvent;
import de.jreality.tools.LineDragListener;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.viewerapp.ViewerApp;

public class DragExample {

	public static void main(String[] args) {
		SceneGraphComponent cmp = new SceneGraphComponent();		
		cmp.setGeometry(Primitives.icosahedron());	
		
		cmp.setAppearance(new Appearance());
		cmp.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,0.025);
		cmp.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,true);
		cmp.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,0.05);
		// set differing diffuse colors for tubes and spheres
		cmp.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+
				CommonAttributes.DIFFUSE_COLOR,new Color(250, 250, 0));
		cmp.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+
				CommonAttributes.DIFFUSE_COLOR,new Color(250, 0, 0));
		
		/**tool:*/
		DragEventTool t = new DragEventTool();
		
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
				System.out.println("start dragging vertex "+e.getIndex());				
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
		t.addLineDragListener(new LineDragListener() {
			
			private IndexedLineSet lineSet;
			private double[][] points;
			
			public void lineDragStart(LineDragEvent e) {
				System.out.println("start dragging line "+e.getIndex());
				
				lineSet = e.getIndexedLineSet();
				points=new double[lineSet.getNumPoints()][];
				lineSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
			}

			public void lineDragged(LineDragEvent e) {
				double[][] newPoints=(double[][])points.clone();
				Matrix trafo=new Matrix();
				MatrixBuilder.euclidean().translate(e.getTranslation()).assignTo(trafo);
				int[] lineIndices=e.getLineIndices();
				for(int i=0;i<lineIndices.length;i++){
					newPoints[lineIndices[i]]=trafo.multiplyVector(points[lineIndices[i]]);
				}
				lineSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(newPoints));	
			}

			public void lineDragEnd(LineDragEvent e) {
			}			
		});
		t.addFaceDragListener(new FaceDragListener() {
			
			private IndexedFaceSet faceSet;
			private double[][] points;
			
			public void faceDragStart(FaceDragEvent e) {
				System.out.println("start dragging face "+e.getIndex());
				
				faceSet = e.getIndexedFaceSet();
				points=new double[faceSet.getNumPoints()][];
				points = faceSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			}

			public void faceDragged(FaceDragEvent e) {
				double[][] newPoints=(double[][])points.clone();
				Matrix trafo=new Matrix();
				MatrixBuilder.euclidean().translate(e.getTranslation()).assignTo(trafo);
				System.err.println("trans = "+Rn.toString(e.getTranslation()));
				System.err.println("Trafo = "+trafo.toString());
				int[] faceIndices=e.getFaceIndices();
				for(int i=0;i<faceIndices.length;i++){
					newPoints[faceIndices[i]]=trafo.multiplyVector(points[faceIndices[i]]);
				}
				System.err.println("Face verts = "+Rn.toString(newPoints));
				faceSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(newPoints));	
			}

			public void faceDragEnd(FaceDragEvent e) {
			}			
		});
		
		cmp.addTool(t);		

	    ViewerApp.display(cmp);
	}
}
