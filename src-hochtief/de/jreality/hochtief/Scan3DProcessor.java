package de.jreality.hochtief;

import java.awt.Color;
import java.io.IOException;

import de.jreality.reader.AbstractReader;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

/**
 * @author Nils Bleicher
 */

public class Scan3DProcessor extends AbstractReader{
	
	private final int N = 1010 / 2;
	private final int M = 431;	
	private final double depthThreshold=0.05;
	private final int minVertexCount=10000;	
	
	//edge detection
	private final double normalVarianzThreshold=0.1;
	private final double maxNeighborhoodDistance=0.1;
	
	public Scan3DProcessor(){}
	
	public void process(String filePath, String texturePath){
		Scan3DLoader loader=new Scan3DLoader(M,N);
		try {
			loader.setInput(Input.getInput(filePath));
		} catch (IOException e) {e.printStackTrace();}
		double[][] depth=loader.getDepth();
//		byte[][] colorR=loader.getColorR(); byte[][] colorG=loader.getColorG(); byte[][] colorB=loader.getColorB();
		
		SimpleDepthFaceExtractor sdfe=new SimpleDepthFaceExtractor(depth);
		sdfe.process(depthThreshold);
		int[][] faceId=sdfe.getFaceIds();
		int[] faceSize=sdfe.getFaceSizes();
		
//		double[][] smoothedDepth;
//		for(int f=0;f<faceSize.length;f++){
//			if(faceSize[f]>minVertexCount){
//				smoothedDepth=FaceSegmentator.smoothFace(f, depth, sdfe.getFaceIds());
//				for(int i=0;i<M;i++){
//					for(int j=0;j<N;j++){
//						if(sdfe.getFaceIds()[i][j]==f)
//							depth[i][j]=smoothedDepth[i][j];
//					}
//				}
//			}
//		}
		
//		double[][][] normals=Scan3DUtility.getVertexNormals(0.05, depth, sdfe.getFaceIds());
//		for(int f=0;f<faceSize.length;f++){
//			if(faceSize[f]>minVertexCount){
//				Scan3DShowUtility.showNormals(0.1, f, normals, depth, sdfe.getFaceIds());
//			}
//		}
		
		int[][] edgeId=EdgeDetector.detect(normalVarianzThreshold, maxNeighborhoodDistance, depthThreshold, depth, faceId);		
		SceneGraphComponent innerEdgePointsNode=EdgeDetector.getEdgePointsSgc(EdgeDetector.EDGE_POINTS_TYPE_BEND,Color.RED,edgeId, faceId, faceSize, minVertexCount, depth);
		SceneGraphComponent borderEdgePointsNode=EdgeDetector.getEdgePointsSgc(EdgeDetector.EDGE_POINTS_TYPE_FACEBORDER,Color.GREEN,edgeId, faceId, faceSize, minVertexCount, depth);
		SceneGraphComponent edgePointsNode=new SceneGraphComponent();
		edgePointsNode.addChild(innerEdgePointsNode);
		edgePointsNode.addChild(borderEdgePointsNode);
		
		Scan3DShowUtility.showGenerateTriangulation(minVertexCount, depthThreshold, faceSize, faceId, depth, texturePath, loader.getPhiOffset(),edgePointsNode);		
	}

	public static void main(String[] args) {
		String path=args[0];
//		String filePath=path+"INHOUSE_II_002_ss10.pts";
//		String texturePath=path+"INHOUSE_II_002_color_quartersize2.jpg";
		String filePath=path+"INHOUSE_II_003_ss10.pts";
		String texturePath=path+"INHOUSE_II_003_color_quartersize2.jpg";
		Scan3DProcessor pcp=new Scan3DProcessor();
		pcp.process(filePath, texturePath);
	}
}
