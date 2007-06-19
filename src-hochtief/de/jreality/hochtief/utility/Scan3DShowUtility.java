package de.jreality.hochtief.utility;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
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
		linesSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
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
	
	public static double[][] getFaceVertices(int faceNr, int faceSize, double[][] depth, int[][] faceId){		
		int M=depth.length;
		int N=depth[0].length;		
		double[][] verts=new double[faceSize][3];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){					
					double[] p=Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
					verts[vertexCount][0]=p[0];
					verts[vertexCount][1]=p[1];
					verts[vertexCount][2]=p[2];
					vertexCount++;
				}
			} 
		}
		return verts;
	}
	
	public static double[][] getTextureCoordinates(int faceNr, int faceSize, int[][] faceId, double offset){
		int M=faceId.length;
		int N=faceId[0].length;	
		double[][] textureCoordinates = new double[faceSize][2];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){
					textureCoordinates[vertexCount][0] = -(double) (j-2) / (N-1) + (offset / (2 * Math.PI) + 0.5);
					textureCoordinates[vertexCount][1] = (double) (i-4) / (M-1) ;
					vertexCount++;
				}
			}
		}
		return textureCoordinates;
	}
	
	
	
	public static int[][] triangulate(int faceNr, int[][] faceId, double[][] depth, double depthThreshold){		
		int M=depth.length;
		int N=depth[0].length;		
		int[][] vertexLabel=new int[M][N];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){
					vertexLabel[i][j]=vertexCount;
					vertexCount++;					
				}else
					vertexLabel[i][j]=-1;
			}
		}
		
		LinkedList<int[]> indices=new LinkedList<int[]>();
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){	
				int i_1=i-1;
				if(i_1<0) i_1=M-1;
				int j_1=j-1;
				if(j_1<0) j_1=N-1;
				if(faceId[i][j]==faceNr){
					if(faceId[(i+1)%M][j]==faceNr){
						//if(Math.abs(depth[i][j]-depth[(i+1)%M][j])<depthThreshold*Math.min(depth[i][j],depth[(i+1)%M][j])){
						if(faceId[i][j_1]==faceNr){
							int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][j],vertexLabel[i][j_1]};
							if(triangleIsEquable(i, j, (i+1)%M, j, i, j_1,depthThreshold,depth)){
								indices.add(newTriangle);			
							}
						}
						if(faceId[(i+1)%M][(j+1)%N]==faceNr){
							int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][(j+1)%N],vertexLabel[(i+1)%M][j]};
							if(triangleIsEquable(i, j, (i+1)%M, j, (i+1)%M, (j+1)%N,depthThreshold,depth)){
								indices.add(newTriangle);	
							}
						}
						//}
					}
					if(faceId[i_1][j]!=faceNr && faceId[i_1][(j+1)%N]==faceNr && faceId[i][(j+1)%N]==faceNr){
						int[] newTriangle={vertexLabel[i][j],vertexLabel[i_1][(j+1)%N],vertexLabel[i][(j+1)%N]};
						if(triangleIsEquable(i, j, i_1, (j+1)%N, i, (j+1)%N,depthThreshold,depth)){
							indices.add(newTriangle);
						}
					}
					if(faceId[(i+1)%M][j]!=faceNr && faceId[(i+1)%M][j_1]==faceNr && faceId[i][j_1]==faceNr){
						int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][j_1],vertexLabel[i][j_1]};
						if(triangleIsEquable(i, j, (i+1)%M, j_1, i, j_1,depthThreshold,depth)){
							indices.add(newTriangle);	
						}
					}
				}
			}
		}
		
		int[][] triangles=new int[indices.size()][];
		Iterator<int[]> it=indices.iterator();
		for(int t=0;t<indices.size();t++)
			triangles[t]=it.next();
		return triangles;
	}
	
	public static boolean triangleIsEquable(int i1, int j1, int i2, int j2, int i3, int j3, double depthThreshold4Triangle, double[][] depth){	
		double d1=Math.abs(depth[i1][j1]-depth[i2][j2]);
		double d2=Math.abs(depth[i1][j1]-depth[i3][j3]);
		double d3=Math.abs(depth[i3][j3]-depth[i2][j2]);		
		double max=Math.max(d1, Math.max(d2,d3));	

		if(max==d1){
			if(max<depthThreshold4Triangle*Math.min(depth[i1][j1],depth[i2][j2])) return true;
		}else if(max==d2){
			if(max<depthThreshold4Triangle*Math.min(depth[i1][j1],depth[i3][j3])) return true;
		}else if(max==d3){
			if(max<depthThreshold4Triangle*Math.min(depth[i3][j3],depth[i2][j2])) return true;
		}
		return false;
	}
	
	public static void showFaceVertices(int minFaceSize, int[] faceSizeCounter, int[][] faceId, double[][] depth){		
		//int[] faceSizeCounter=getFaceSizes();	
	
		SceneGraphComponent sceneRoot=new SceneGraphComponent();

		for(int f=0;f<faceSizeCounter.length;f++){
			if(faceSizeCounter[f]>minFaceSize){

				double[][] verts=getFaceVertices(f, faceSizeCounter[f],depth,faceId);
				
				PointSetFactory psf=new PointSetFactory();
				psf.setVertexCount(faceSizeCounter[f]);
				psf.setVertexCoordinates(verts);
				psf.update();
				
				SceneGraphComponent sgc=new SceneGraphComponent();
				sgc.setGeometry(psf.getGeometry());
				sceneRoot.addChild(sgc);
			}
		}
		
		sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ViewerApp.display(sceneRoot);
	}
	public static void showGenerateTriangulation(int minVertexCount, double depthThreshold, int[] faceSize, int[][] faceId, double[][] depth, String texturePath, double texOffset){
		showGenerateTriangulation(minVertexCount, depthThreshold, faceSize, faceId, depth, texturePath, texOffset, null);
	}
	
	public static void showGenerateTriangulation(int minVertexCount, double depthThreshold, int[] faceSize, int[][] faceId, double[][] depth, String texturePath, double texOffset, SceneGraphComponent child){
		SceneGraphComponent sceneRoot=new SceneGraphComponent();
		sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH,0.001);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);

		if(child!=null) sceneRoot.addChild(child);
		
		ImageData img=null;
		if(texturePath!=null){
			try {
				img = ImageData.load(Input.getInput(texturePath));
			} catch (IOException e) {}
		}
		
		for(int i=0;i<faceSize.length;i++){
			if(faceSize[i]>minVertexCount){
				int[][] faceInds=triangulate(i, faceId, depth, depthThreshold);
				if(faceInds.length>0){
					//System.out.println("creating face "+i);
					IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
					ifsf.setVertexCount(faceSize[i]);
					ifsf.setVertexCoordinates(getFaceVertices(i, faceSize[i], depth, faceId));
					ifsf.setFaceCount(faceInds.length);
					ifsf.setFaceIndices(faceInds);
					if(texturePath!=null)
						ifsf.setVertexTextureCoordinates(getTextureCoordinates(i, faceSize[i], faceId, texOffset));
					ifsf.setGenerateEdgesFromFaces(true);
					ifsf.setGenerateFaceNormals(true);
					ifsf.setGenerateVertexNormals(true);
					ifsf.update();
					PickUtility.setPickable(ifsf.getGeometry(), false);

					SceneGraphComponent sgc=new SceneGraphComponent("face "+i);
					sgc.setGeometry(ifsf.getGeometry());
					sgc.setAppearance(new Appearance());
					if(texturePath!=null)
						TextureUtility.createTexture(sgc.getAppearance(),"polygonShader", img, false);
					sceneRoot.addChild(sgc);

					//System.out.println("vertexCount: "+faceSize[i]);
					//System.out.println("faceCount:   "+faceInds.length+"\n");
				}
			}			
		}
		
		MatrixBuilder.euclidean().translate(0,0,2.4).assignTo(sceneRoot);
		
		ViewerApp vApp=ViewerVR.mainImpl(new String[]{});
		int index=0;
		while(vApp.getSceneRoot().getChildComponent(index).getName()!="scene")
			index++;		
		vApp.getSceneRoot().getChildComponent(index).addChild(sceneRoot);
	
//		ViewerApp.display(sceneRoot);
		
	}
	

}
