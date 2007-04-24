package de.jreality.hochtief;

import java.io.IOException;
import de.jreality.reader.AbstractReader;
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
		int[] faceSize=sdfe.getFaceSizes();
		double[][] smoothedDepth;
		for(int f=0;f<faceSize.length;f++){
			if(faceSize[f]>minVertexCount){
				smoothedDepth=FaceSegmentator.smoothFace(f, depth, sdfe.getFaceIds());
				for(int i=0;i<M;i++){
					for(int j=0;j<N;j++){
						if(sdfe.getFaceIds()[i][j]==f)
							depth[i][j]=smoothedDepth[i][j];
					}
				}
			}
		}

		
		
		
		sdfe.showTriangulation(minVertexCount, texturePath, loader.getPhiOffset());
				
	}

	public static void main(String[] args) {
		String path=args[0];
//		String filePath=path+"INHOUSE_II_002_ss10.pts";
//		String texturePath=path+"INHOUSE_II_002_color_quartersize.jpg";
		String filePath=path+"INHOUSE_II_003_ss10.pts";
		String texturePath=path+"INHOUSE_II_003_color_quartersize2.jpg";
		Scan3DProcessor pcp=new Scan3DProcessor(filePath, texturePath);
		pcp.process();
	}
}
