package de.jreality.hochtief;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;

public class Scan3DUtility {
	
	
	public static double[] convert3DCoordinateToDepthValue(double x,double y,double z,int N,int M){
		double phi = Math.atan2(y, x);
		double theta = Math.atan2(z, Math.sqrt(x * x + y * y));
		double n = Math.round((phi + Math.PI) / 2 / Math.PI  * (N - 1));
		double m = Math.round((-theta + Math.PI / 2)
				/ (Math.PI - (Math.PI / 2 - 1.1306075316023216)) * (M - 1));
		double d= Math.sqrt(x * x + y * y + z * z);		
		return new double[]{n,m,d};		
	}
	
	public static double[] convertDepthValueTo3DCoordinate(int i, int j, double depth, int M, int N){
		double phi = j * 2 * Math.PI / (N - 1);
		double theta = -i
				* (Math.PI - (Math.PI / 2 - 1.1306075316023216))
				/ (M - 1) + Math.PI / 2;		
		return new double[] {
				depth * Math.cos(phi) * Math.cos(theta),
				depth * Math.sin(phi) * Math.cos(theta),
				depth * Math.sin(theta)
		};
	}
	
	//private static double depthThreshold=0.05;
	
	
	public static double[][][] getVertexNormals(double depthThreshold, double [][] depth, int[][] faceId){
		double[][][] normals=new double[depth.length][depth[0].length][];
		for(int i=0;i<depth.length;i++){
			for(int j=0;j<depth[0].length;j++){	
				normals[i][j]=Scan3DUtility.getVertexNormal(i, j, depthThreshold, depth, faceId);		
			}
		}
		return normals;
	}
	
	public static double[] getVertexNormal(int i, int j, double depthThreshold, double [][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		
//		8-neighborhood -> which triangulation good for v-Ns?
//		int[][] nbh={{i+1,j-1},{i+1,j},{i+1,j+1},{i,j+1},{i-1,j+1},{i-1,j},{i-1,j-1},{i,j-1},{i+1,j-1}};
//		for(int ii=0;ii<nbh.length;ii++){
//			if(nbh[ii][0]<0) nbh[ii][0]=M+nbh[ii][0];
//			if(nbh[ii][0]>=M) nbh[ii][0]=nbh[ii][0]-M;
//			if(nbh[ii][1]<0) nbh[ii][1]=N+nbh[ii][1];
//			if(nbh[ii][1]>=N) nbh[ii][1]=nbh[ii][1]-N;
//		}
		
		int[][] nbh=getSorted1Nbh(i, j, depthThreshold, depth, faceId);
		
//		System.out.println("\nnbh "+i+", "+j+" :");
//		for(int v=0;v<nbh.length;v++){
//			System.out.println(nbh[v][0]+", "+nbh[v][1]);
//		}
		
	
		if(nbh.length<2) return new double[] {0,0,0};
		
		double[][] edges=new double[nbh.length][];
		double[] vCoord=convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
		for(int ii=0;ii<edges.length;ii++){
			edges[ii]=Rn.subtract(null, convertDepthValueTo3DCoordinate(nbh[ii][0], nbh[ii][1], depth[nbh[ii][0]][nbh[ii][1]], M, N), vCoord);
		}
		
		double[][] fNormals=new double[edges.length-1][];
		for(int ii=0;ii<fNormals.length;ii++){
			fNormals[ii]=Rn.normalize(null,Rn.crossProduct(null, edges[ii], edges[ii+1]));			
		}
		
		double[] vNormal=new double[3];
		double factor=1;
		for(int ii=0;ii<fNormals.length;ii++){
			//if(faceId[nbh[ii][0]][nbh[ii][1]]==faceId[i][j] && Math.abs(depth[nbh[ii][0]][nbh[ii][1]]-depth[i][j])<=depthThreshold*Math.min(depth[nbh[ii][0]][nbh[ii][1]],depth[i][j])){
				//MWA
				factor=Math.asin(Rn.euclideanNorm(Rn.crossProduct(null, edges[ii], edges[ii+1]))
						/(Rn.euclideanNorm(edges[ii])*Rn.euclideanNorm(edges[ii+1])));
				
				//MWSELR
//				factor=Rn.euclideanNorm(Rn.crossProduct(null, edges[ii], edges[ii+1]))
//						/(Math.pow(Rn.euclideanNorm(edges[ii]),2)*Math.pow(Rn.euclideanNorm(edges[ii+1]),2));
				
				Rn.add(vNormal, vNormal, Rn.times(null, factor, fNormals[ii]));
			//}
		}
		return Rn.normalize(vNormal,vNormal);	
		
//		Rn.normalize(vNormal,vNormal);
//		double phi = Math.atan2(vNormal[1], vNormal[0]);
//		double theta = Math.atan2(vNormal[2], Math.sqrt(vNormal[0] * vNormal[0] + vNormal[1] * vNormal[1]));
//		return new double[] {phi,theta};
	}
	
	public static double[] getCovarianzMatrix(double[][] data){
		int count=data.length;
		int dim=data[0].length;
		double[] center=new double[dim];
		for(int i=0;i<count;i++)
			Rn.add(center, center, data[i]);
		Rn.times(center, 1/(double)count, center);
		double[][] centeredData=new double[data.length][data[0].length];
		for(int i=0;i<count;i++)
			Rn.subtract(centeredData[i], data[i], center);
		double[] cov=new double[dim*dim];
		double entry;
		for(int d1=0;d1<dim;d1++){
			for(int d2=0;d2<dim;d2++){
				entry=0;
				for(int i=0;i<count;i++)
					entry+=(centeredData[i][d1]*centeredData[i][d2]);
				entry=entry/(count-1);
				cov[d1*dim+d2]=entry;				
			}
		}
		return cov;		
	}
	
	public static int[][] getNeighborhood(int i, int j, int neighborhoodSize, double depthThreshold, double[][] depth, int[][] faceId){
		ArrayList<int[]> nbh=new ArrayList<int[]>();		
		int M=depth.length;
		int N=depth[0].length;
		int posI,posJ;
		for(int ii=i-neighborhoodSize;ii<i+neighborhoodSize+1;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-neighborhoodSize;jj<j+neighborhoodSize+1;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(    (!(ii==i && jj==j))
					&& faceId[posI][posJ]==faceId[i][j] 
					&& Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold*Math.min(depth[posI][posJ],depth[i][j])){
						nbh.add(new int[]{posI,posJ});					
				}
			}
		}
		int[][] neighborhood=new int[nbh.size()][];
		for(int t=0;t<nbh.size();t++)
			neighborhood[t]=nbh.get(t);
		return neighborhood;
	}
	

	public static int[][] getSorted1Nbh(int i, int j, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		
		int[][] nbh=new int[][] {{i,j+1},{i-1,j+1},{i-1,j},{i-1,j-1},{i,j-1},{i+1,j-1},{i+1,j},{i+1,j+1}};
		ArrayList<int[]> nbhList=new ArrayList<int[]>();
		int addI,addJ;
		for(int n=0;n<nbh.length;n++){
			addI=nbh[n][0];
			if(addI<0) addI=M+addI;
			if(addI>=M) addI=addI-M;
			addJ=nbh[n][1];
			if(addJ<0) addJ=N+addJ;
			if(addJ>=N) addJ=addJ-N;
			
			if(    faceId[addI][addJ]==faceId[i][j] 
				&& Math.abs(depth[addI][addJ]-depth[i][j])<=depthThreshold*Math.min(depth[addI][addJ],depth[i][j])){
						nbhList.add(new int[]{addI,addJ});	
			}
		}
		int[][] neighborhood=new int[nbhList.size()][];
		for(int t=0;t<nbhList.size();t++)
			neighborhood[t]=nbhList.get(t);
		return neighborhood;		
	}
	
	public static double averageDistance(int i, int j, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		int[][] nbh=getNeighborhood(i, j, 1, depthThreshold, depth, faceId);	
		double averageValue=0;
		int vertexCount=0;
		double[] p=convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);	
		for(int n=0;n<nbh.length;n++){
			double[] p2=convertDepthValueTo3DCoordinate(nbh[n][0], nbh[n][1], depth[nbh[n][0]][nbh[n][1]], M, N);
			averageValue+=Rn.euclideanDistance(p, p2);
			vertexCount++;
		}
		return averageValue/(double)vertexCount;
	}
	
//	maxDistance is the the distance from that all average-distances above will result a neighborhood=1 and all smaller average-distances will result a neighborhood>1
	public static int getNeighborhoodSize(int i, int j, double depthThreshold, double maxDistance, double[][] depth, int[][] faceId){
		return (int)Math.ceil(maxDistance/averageDistance(i, j, depthThreshold, depth, faceId));
	}
	
}
