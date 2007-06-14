package de.jreality.hochtief.processing;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.vr.ViewerVR;

public class EdgeSplitFaceExtractor {
	
	private double[][] depth;
	private int M, N;
	private int[][]	splitFaceId;
	private int splitFaceIdCount;
	private int[] splitFaceSizeCounter;
	
	public EdgeSplitFaceExtractor(double[][] depth){
		this.depth=depth;
		M=depth.length;
		N=depth[0].length;		
	}
	
	public void splitFaces(int[][] edgeId, int[][] faceId, int[] faceSize, int minVertexCount, double depthThreshold){		
		splitFaceId=edgeId;
		splitFaceIdCount=0;
		ArrayList<Integer> splitFaceIdMap=new ArrayList<Integer>();
		boolean connectI, connectJ;	
		int formerI, formerJ;
		
		for(int i=0;i<M;i++){ 
			formerI=i-1;
			//if(formerI<0) formerI=M-1;
			for(int j=0;j<N;j++){
				formerJ=j-1;
				//if(formerJ<0) formerJ=N-1;
				connectI=false;
				if(formerI>=0 && depth[i][j]!=0 && depth[formerI][j]!=0){
					if(edgeId[i][j]>=0 && edgeId[formerI][j]>=0 
							&& faceId[i][j]==faceId[formerI][j]
							&& Math.abs(depth[i][j]-depth[formerI][j])<depthThreshold*Math.min(depth[i][j],depth[formerI][j]))
						connectI=true;					
				}
				connectJ=false;
				if(formerJ>=0 && depth[i][j]!=0 && depth[i][formerJ]!=0){
					if(edgeId[i][j]>=0 && edgeId[i][formerJ]>=0  
							&& faceId[i][j]==faceId[i][formerJ] 
							&& Math.abs(depth[i][j]-depth[i][formerJ])<depthThreshold*Math.min(depth[i][j],depth[i][formerJ]))
						connectJ=true;					
				}
				
				if(connectJ){					
					splitFaceId[i][j]=splitFaceId[i][formerJ];
					if(connectI){
						if(splitFaceId[formerI][j]!=splitFaceId[i][formerJ]){								
							int minId=Math.min(splitFaceIdMap.get(splitFaceId[formerI][j]), splitFaceIdMap.get(splitFaceId[i][formerJ]));
							int maxId=Math.max(splitFaceIdMap.get(splitFaceId[formerI][j]), splitFaceIdMap.get(splitFaceId[i][formerJ]));
							if(minId!=maxId){
								for(int fid=maxId;fid<splitFaceIdMap.size();fid++){
									if(splitFaceIdMap.get(fid)==maxId)
										splitFaceIdMap.set(fid, minId);
								}									
							}
						}
					}
				}else if(connectI){
					splitFaceId[i][j]=splitFaceId[formerI][j];
				}else{
					splitFaceId[i][j]=splitFaceIdCount;
					splitFaceIdMap.add(new Integer(splitFaceIdCount));
					splitFaceIdCount++;	
				}
			}			
		}

		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(!(splitFaceId[i][j]==splitFaceIdMap.get(splitFaceId[i][j]))){
					splitFaceId[i][j]=splitFaceIdMap.get(splitFaceId[i][j]);
				}
			}
		}	
	}

	public void calculateSplittedFaceSizes(){
		splitFaceSizeCounter=new int[splitFaceIdCount];
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				splitFaceSizeCounter[splitFaceId[i][j]]++;
			} 
		}
	}
	
	public int[][] getSplittedFaceIds(){
		return splitFaceId;
	}
	
	public int[] getSplittedFaceSizes(){
		if(splitFaceSizeCounter==null) calculateSplittedFaceSizes();
		return splitFaceSizeCounter;
	}
	
	
//	public void showSplittedFaces(int[][] splittedFaceId, int[] splittedFaceSize, int[][] faceId, int[] faceSize, int depthThreshold, int minVertexCount, double[][] depth, SceneGraphComponent child){
//		
//		SceneGraphComponent sceneRoot=new SceneGraphComponent();
//		sceneRoot.setAppearance(new Appearance());
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
////		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH,0.001);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
//		sceneRoot.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
//
//		if(child!=null) sceneRoot.addChild(child);
//
//		
//		for(int i=0;i<faceSize.length;i++){
//			if(faceSize[i]>minVertexCount){
//				int[][] faceInds=triangulate(i, splittedFaceId, depth, depthThreshold);
//				if(faceInds.length>0){
//					System.out.println("creating face "+i);
//					IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
//					ifsf.setVertexCount(faceSize[i]);
//					ifsf.setVertexCoordinates(getFaceVertices(i, faceSize[i], depth, splittedFaceId));
//					ifsf.setFaceCount(faceInds.length);
//					ifsf.setFaceIndices(faceInds);
//					if(texturePath!=null)
//						ifsf.setVertexTextureCoordinates(getTextureCoordinates(i, faceSize[i], splittedFaceId, texOffset));
//					ifsf.setGenerateEdgesFromFaces(true);
//					ifsf.setGenerateFaceNormals(true);
//					ifsf.setGenerateVertexNormals(true);
//					PickUtility.setPickable(ifsf.getGeometry(), false);
//					ifsf.update();
//
//					SceneGraphComponent sgc=new SceneGraphComponent("face "+i);
//					sgc.setGeometry(ifsf.getGeometry());
//					sgc.setAppearance(new Appearance());
//					if(texturePath!=null)
//						TextureUtility.createTexture(sgc.getAppearance(),"polygonShader", img, false);
//					sceneRoot.addChild(sgc);
//
//					System.out.println("vertexCount: "+faceSize[i]);
//					System.out.println("faceCount:   "+faceInds.length+"\n");
//				}
//			}			
//		}
//		
//		MatrixBuilder.euclidean().translate(0,0,2.4).assignTo(sceneRoot);
//		
//		ViewerApp vApp=ViewerVR.mainImpl(new String[]{});
//		int index=0;
//		while(vApp.getSceneRoot().getChildComponent(index).getName()!="scene")
//			index++;		
//		vApp.getSceneRoot().getChildComponent(index).addChild(sceneRoot);
//		
//	}

}
