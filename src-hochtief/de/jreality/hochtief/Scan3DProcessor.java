package de.jreality.hochtief;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.PointSetFactory;
import de.jreality.reader.AbstractReader;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Input;

/**
 * @author Nils Bleicher
 */

public class Scan3DProcessor extends AbstractReader{
	
	private final int N = 1010 / 2;
	private final int M = 431;
	
	
	private double[][] depth;

	private byte[][] colorR;
	private byte[][] colorG;
	private byte[][] colorB;
	
	private String filePath;
	private String texturePath;
	
	public Scan3DProcessor(String filePath, String texturePath){
		this.filePath=filePath;
		this.texturePath=texturePath;
	}
	

	
	public void process(){

		
		Scan3DLoader loader=new Scan3DLoader(M,N);
		try {
			loader.setInput(Input.getInput(filePath));
		} catch (IOException e) {e.printStackTrace();}
		depth=loader.getDepth();
		colorR=loader.getColorR();
		colorG=loader.getColorG();
		colorB=loader.getColorB();
		
		int minVertexCount=10000;
		SimpleDepthFaceExtractor sdfe=new SimpleDepthFaceExtractor(depth);
		sdfe.setDepthThreshold(0.05);
		sdfe.process();
		//sdfe.show(minVertexCount);
//		int[] faceSize=sdfe.getFaceSizes();
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
		
		
		
//		int[] faceSize=sdfe.getFaceSizes();
//		double[][][] normals=Scan3DUtility.getVertexNormals(0.05, depth, sdfe.getFaceIds());
//		for(int f=0;f<faceSize.length;f++){
//			if(faceSize[f]>minVertexCount){
//				Scan3DShowUtility.showNormals(0.1, f, normals, depth, sdfe.getFaceIds());
//			}
//		}
		
		
		
		int[][] edgeId=EdgeDetector.detect(0.1, 0.05, 0.1 , depth, sdfe.getFaceIds());		
		SceneGraphComponent innerEdgePointsNode=EdgeDetector.getEdgePointsSgc(EdgeDetector.EDGE_POINTS_TYPE_BEND,Color.RED,edgeId, sdfe.getFaceIds(), sdfe.getFaceSizes(), minVertexCount, depth);
		SceneGraphComponent borderEdgePointsNode=EdgeDetector.getEdgePointsSgc(EdgeDetector.EDGE_POINTS_TYPE_FACEBORDER,Color.GREEN,edgeId, sdfe.getFaceIds(), sdfe.getFaceSizes(), minVertexCount, depth);
		SceneGraphComponent edgePointsNode=new SceneGraphComponent();
		edgePointsNode.addChild(innerEdgePointsNode);
		edgePointsNode.addChild(borderEdgePointsNode);
		
		sdfe.showTriangulation(minVertexCount, texturePath, loader.getPhiOffset(),edgePointsNode);
				
	}

	public static void main(String[] args) {
		String path=args[0];
//		String filePath=path+"INHOUSE_II_002_ss10.pts";
//		String texturePath=path+"INHOUSE_II_002_color_quartersize2.jpg";
		String filePath=path+"INHOUSE_II_003_ss10.pts";
		String texturePath=path+"INHOUSE_II_003_color_quartersize2.jpg";
		Scan3DProcessor pcp=new Scan3DProcessor(filePath, texturePath);
		pcp.process();
	}
}
