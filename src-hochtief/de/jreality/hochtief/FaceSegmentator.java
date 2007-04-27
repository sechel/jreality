package de.jreality.hochtief;

import java.util.ArrayList;

import de.jreality.math.Rn;

/**
 * @author Nils Bleicher
 */ 

public class FaceSegmentator {
	private static double depthThreshold=0.05;
	
	public static double[][] smoothFace(int faceNr, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double[][] smoothedDepth=new double[M][N];		
		
		double maxMedianDist=0.5;		
		int maxNeighborhood=10;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){				
					//smoothedDepth[i][j]=median(i,j,3,faceNr,depth,faceId);
					//smoothedDepth[i][j]=median(i,j,maxMedianDist,maxNeighborhood,faceNr,depth,faceId);
					//smoothedDepth[i][j]=averageValue(i,j,6,faceNr,depth,faceId);
					smoothedDepth[i][j]=averageValue(i,j,maxMedianDist,maxNeighborhood,faceNr,depth,faceId);
				}else
					smoothedDepth[i][j]=depth[i][j];
			}
		}		
		return smoothedDepth;		
	}
	
	private static double median(int i, int j, int neighborhood, int faceNr, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		ArrayList<Double> depthValues=new ArrayList<Double>();
		int posI,posJ;
		for(int ii=i-neighborhood;ii<i+neighborhood+1;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-neighborhood;jj<j+neighborhood+1;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceNr && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold){
					
					int listPos=0;
					while(listPos<depthValues.size() && depthValues.get(listPos)<depth[posI][posJ])
						listPos++;
					depthValues.add(listPos,depth[posI][posJ]);
					
				}
			}
		}
		if(depthValues.size()>1)
			return depthValues.get(depthValues.size()/2);
		else
			return depthValues.get(0);
	}
	
	//median adapted to median-distance to surounding face-points 
	//maxDistance is the the distance from that all median-distances above will result a neighborhood=1 and all smaller median-distances will result a neighborhood>1
	private static double median(int i, int j, double maxDistance, int maxNeighborhood, int faceNr, double[][] depth, int[][] faceId){
		int neighborhood=(int)Math.ceil(maxDistance/medianDistance(i, j, faceNr, depth, faceId));
		if(neighborhood>maxNeighborhood) neighborhood=maxNeighborhood;
		return median(i, j, neighborhood, faceNr, depth, faceId);
	}
	
	public static double medianDistance(int i, int j, int faceNr, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double[] p=SimpleDepthFaceExtractor.convertDepthPoint(i, j, depth[i][j], M, N);
		ArrayList<Double> distValues=new ArrayList<Double>();
		int posI,posJ;		
		for(int ii=i-1;ii<i+2;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-1;jj<j+2;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceNr && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold){

					double[] p2=SimpleDepthFaceExtractor.convertDepthPoint(posI, posJ, depth[posI][j], M, N);
					double dist=Rn.euclideanDistance(p, p2);
					int listPos=0;
					while(listPos<distValues.size() && distValues.get(listPos)<dist)
						listPos++;
					distValues.add(listPos,dist);

				}
			}
		}
		if(distValues.size()>1)
			return distValues.get(distValues.size()/2);
		else
			return distValues.get(0);

	}
	
	private static double averageValue(int i, int j, int neighborhood, int faceNr, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double averageValue=0;
		int vertexCount=0;
		int posI,posJ;
		for(int ii=i-neighborhood;ii<i+neighborhood+1;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-neighborhood;jj<j+neighborhood+1;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceNr && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold){
					
					averageValue+=depth[posI][posJ];
					vertexCount++;
					
				}
			}
		}
		return averageValue/(double)vertexCount;
	}
	
	//averageValue adapted to average-distance to surounding face-points 
	//maxDistance is the the distance from that all median-distances above will result a neighborhood=1 and all smaller median-distances will result a neighborhood>1
	private static double averageValue(int i, int j, double maxDistance, int maxNeighborhood, int faceNr, double[][] depth, int[][] faceId){
		int neighborhood=(int)Math.ceil(maxDistance/averageDistance(i, j, faceNr, depth, faceId));
		if(neighborhood>maxNeighborhood) neighborhood=maxNeighborhood;
		return averageValue(i, j, neighborhood, faceNr, depth, faceId);
	}
	
	public static double averageDistance(int i, int j, int faceNr, double[][] depth, int[][] faceId){
		double smoothThreshold=0.03;
		
		int M=depth.length;
		int N=depth[0].length;
		double averageValue=0;
		int vertexCount=0;
		double[] p=SimpleDepthFaceExtractor.convertDepthPoint(i, j, depth[i][j], M, N);
		int posI,posJ;		
		for(int ii=i-1;ii<i+2;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-1;jj<j+2;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceNr && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold){

					double[] p2=SimpleDepthFaceExtractor.convertDepthPoint(posI, posJ, depth[posI][j], M, N);
					averageValue+=Rn.euclideanDistance(p, p2);
					vertexCount++;

				}
			}
		}
		averageValue=averageValue/(double)vertexCount;
		if(Math.abs(averageValue-depth[i][j])<smoothThreshold)
			return averageValue;
		else
			return depth[i][j];
	}
	
}
