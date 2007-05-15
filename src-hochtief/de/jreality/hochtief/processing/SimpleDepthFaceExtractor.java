package de.jreality.hochtief.processing;

import java.util.ArrayList;

/**
 * @author Nils Bleicher
 */

public class SimpleDepthFaceExtractor {
	
	private double[][] depth;
	private int M, N;
	private int[][]	faceId;
	private int faceIdCount;
	private int[] faceSizeCounter;
	
	public SimpleDepthFaceExtractor(double[][] depth){
		this.depth=depth;
		M=depth.length;
		N=depth[0].length;		
	}	

	public void process(double depthThreshold){
		faceId=new int[M][N];
		faceIdCount=0;
		ArrayList<Integer> faceIdMap=new ArrayList<Integer>();
		boolean connectI, connectJ;	
		
		for(int i=0;i<M;i++){ 
			for(int j=0;j<N;j++){
				connectI=false;
				if(i-1>=0 && depth[i][j]!=0 && depth[i-1][j]!=0){
					if(Math.abs(depth[i][j]-depth[i-1][j])<depthThreshold*Math.min(depth[i][j],depth[i-1][j]))
						connectI=true;					
				}
				connectJ=false;
				if(j-1>=0 && depth[i][j]!=0 && depth[i][j-1]!=0){
					if(Math.abs(depth[i][j]-depth[i][j-1])<depthThreshold*Math.min(depth[i][j],depth[i-1][j]))
						connectJ=true;					
				}
				
				if(connectJ){					
					faceId[i][j]=faceId[i][j-1];
					if(connectI){
						if(faceId[i-1][j]!=faceId[i][j-1]){								
							int minId=Math.min(faceIdMap.get(faceId[i-1][j]), faceIdMap.get(faceId[i][j-1]));
							int maxId=Math.max(faceIdMap.get(faceId[i-1][j]), faceIdMap.get(faceId[i][j-1]));
							if(minId!=maxId){
								for(int fid=maxId;fid<faceIdMap.size();fid++){
									if(faceIdMap.get(fid)==maxId)
										faceIdMap.set(fid, minId);
								}									
							}
						}
					}
				}else if(connectI){
					faceId[i][j]=faceId[i-1][j];
				}else{
					faceId[i][j]=faceIdCount;
					faceIdMap.add(new Integer(faceIdCount));
					faceIdCount++;	
				}
			}			
		}

		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(!(faceId[i][j]==faceIdMap.get(faceId[i][j]))){
					faceId[i][j]=faceIdMap.get(faceId[i][j]);
				}
			}
		}	
	}
	
	
	public void calculateFaceSizes(){
		faceSizeCounter=new int[faceIdCount];
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				faceSizeCounter[faceId[i][j]]++;
			} 
		}
	}
	
	
	public int[][] getFaceIds(){
		return faceId;
	}
	
	public int[] getFaceSizes(){
		if(faceSizeCounter==null) calculateFaceSizes();
		return faceSizeCounter;
	}
	
}
