package de.jreality.hochtief;

import java.awt.Color;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.vr.ViewerVR;

public class Scan3DShowUtility {
	
	
	public static void showNormals(double normalLength, int faceNr, double[][][] normals, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double[] nullVec=new double[]{0,0,0};
		
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr && !Rn.equals(normals[i][j],nullVec)){
					vertexCount++;
				}
			}
		}
		
		double[][] lineVertices=new double[2*vertexCount][3];
		int[][] lineIndices=new int[vertexCount][2];
		double[][] pointVertices=new double[vertexCount][3];
		
		vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr && !Rn.equals(normals[i][j],nullVec)){
					double[] p1=Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
					double[] p2=Rn.add(null, p1, Rn.times(null, normalLength, normals[i][j]));
					pointVertices[vertexCount]=p2;
					lineVertices[2*vertexCount]=p1;
					lineVertices[2*vertexCount+1]=p2;
					lineIndices[vertexCount]=new int[] {2*vertexCount,2*vertexCount+1};
					vertexCount++;
				}
			}
		}
		
		IndexedLineSetFactory lines=new IndexedLineSetFactory();
		lines.setVertexCount(2*vertexCount);
		lines.setVertexCoordinates(lineVertices);
		lines.setLineCount(vertexCount);
		lines.setEdgeIndices(lineIndices);
		lines.update();
		SceneGraphComponent linesSgc=new SceneGraphComponent();
		linesSgc.setAppearance(new Appearance());
		linesSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		linesSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		linesSgc.getAppearance().setAttribute(CommonAttributes.POINT_SIZE, 4.0);
		linesSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
//		linesSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 4.0);	
		linesSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		linesSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, 0.01);
		linesSgc.setGeometry(lines.getGeometry());
		
		PointSetFactory points=new PointSetFactory();
		points.setVertexCount(vertexCount);
		points.setVertexCoordinates(pointVertices);
		points.update();
		SceneGraphComponent pointsSgc=new SceneGraphComponent();
		pointsSgc.setAppearance(new Appearance());
		pointsSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pointsSgc.getAppearance().setAttribute(CommonAttributes.POINT_SIZE, 20.0);
		pointsSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		pointsSgc.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.02);
		pointsSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.GREEN);
		pointsSgc.setGeometry(points.getGeometry());
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		sgc.addChild(linesSgc);
		sgc.addChild(pointsSgc);
		
		//ViewerApp.display(sgc);
		MatrixBuilder.euclidean().translate(0,0,2.4).assignTo(sgc);
		
		ViewerApp vApp=ViewerVR.mainImpl(new String[]{});
		int index=0;
		while(vApp.getSceneRoot().getChildComponent(index).getName()!="scene")
			index++;		
		vApp.getSceneRoot().getChildComponent(index).addChild(sgc);
		
	}
	

}
