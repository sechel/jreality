package de.jreality.hochtief.processing;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;

import de.jreality.hochtief.pointclouds.ExpectationMaximation;
import de.jreality.hochtief.utility.Scan3DPointCloudUtility;
import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;

public class PointCloudSimplifier {

	public static SceneGraphComponent getSimplifiedPointCloud(double texRes, int[][] edgeId, double[][] depth, byte[][] colorR, byte[][] colorG, byte[][] colorB){
		int componentCount=20;
		double minProbChange=0.05;		
		
		int M=depth.length;  int N=depth[0].length;
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		ArrayList<double[]> singlePoints=new ArrayList<double[]>();
		ArrayList<byte[]> colors=new ArrayList<byte[]>();
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){				
				if(edgeId[i][j]==EdgeDetector.POINT_TYPE_SINGLEPOINT){
					singlePoints.add(Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N));
					colors.add(new byte[]{colorR[i][j],colorG[i][j],colorB[i][j]});					
				}				
			}			
		}
		
		double[][] points=new double[singlePoints.size()][3];
		points=singlePoints.toArray(points);
		
		double[][] params=ExpectationMaximation.calculateParameters(componentCount, minProbChange, points);
		int[] compId=ExpectationMaximation.evalPoints(points, params);
		
		for(int c=0;c<componentCount;c++){
			double[] centeroid=new double[] {params[c][0],params[c][1],params[c][2]};
			DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			SymmPackEVD evd=null;
			try {
				evd = SymmPackEVD.factorize(covMtx);
			} catch (NotConvergedException e) {e.printStackTrace();}
			
			//double[] ev=evd.getEigenvalues();
			DenseMatrix eig=evd.getEigenvectors();
			
			double[] faceDir1=new double[] {eig.get(0, 2),eig.get(1, 2),eig.get(2, 2)};
			double[] faceDir2=new double[] {eig.get(0, 1),eig.get(1, 1),eig.get(2, 1)};
			double[] faceDir3=new double[] {eig.get(0, 0),eig.get(1, 0),eig.get(2, 0)};
			
			ArrayList<double[]> componentPoints=new ArrayList<double[]>();
			ArrayList<byte[]> componentColors=new ArrayList<byte[]>();
			for(int i=0;i<points.length;i++){
				if(compId[i]==c){
					componentPoints.add(points[i]);
					componentColors.add(colors.get(i));
				}
			}
			
			double max1=0,min1=0,max2=0,min2=0;		
			double[] point;
			double dist;		
			for(int i=0;i<componentPoints.size();i++){
				point=Rn.subtract(null, componentPoints.get(i), centeroid);
				dist=Rn.innerProduct(faceDir1, point);
				if(dist>max1) max1=dist; 
				if(dist<min1) min1=dist; 
				dist=Rn.innerProduct(faceDir2, point);
				if(dist>max2) max2=dist; 
				if(dist<min2) min2=dist; 			
			}			
			sgc.addChild(Scan3DPointCloudUtility.projectPointCloud(componentPoints, componentColors, centeroid, faceDir1, faceDir2, max1, min1, max2, min2, texRes));

			
		}
		return sgc;
	}
	
	
	
	
//	public static SceneGraphComponent getSimplifiedPointCloud(double texRes, int[][] edgeId, double[][] depth, byte[][] colorR, byte[][] colorG, byte[][] colorB){
//		int M=depth.length;  int N=depth[0].length;
//		
//		SceneGraphComponent sgc=new SceneGraphComponent();
//		ArrayList<double[]> singlePoints=new ArrayList<double[]>();
//		ArrayList<byte[]> colors=new ArrayList<byte[]>();
//		
//		for(int i=0;i<M;i++){
//			for(int j=0;j<N;j++){				
//				if(edgeId[i][j]==EdgeDetector.POINT_TYPE_SINGLEPOINT){
//					singlePoints.add(Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N));
//					colors.add(new byte[]{colorR[i][j],colorG[i][j],colorB[i][j]});					
//				}				
//			}			
//		}
//		
//		DenseMatrix covMtx=new DenseMatrix(Scan3DUtility.getCovarianzMatrix(singlePoints));
//		
//		SymmPackEVD evd=null;
//		try {
//			evd = SymmPackEVD.factorize(covMtx);
//		} catch (NotConvergedException e) {e.printStackTrace();}
//		
//		//double[] ev=evd.getEigenvalues();
//		DenseMatrix eig=evd.getEigenvectors();
//		
//		//ev(faceDir1)>ev(faceDir2)>ev(faceDir3)
//		double[] faceDir1=new double[] {eig.get(0, 2),eig.get(1, 2),eig.get(2, 2)};
//		double[] faceDir2=new double[] {eig.get(0, 1),eig.get(1, 1),eig.get(2, 1)};
//		double[] faceDir3=new double[] {eig.get(0, 0),eig.get(1, 0),eig.get(2, 0)};
//		
//		double[] centeroid=new double[3];
//		for(int i=0;i<singlePoints.size();i++)
//			Rn.add(centeroid, centeroid, singlePoints.get(i)); 
//		Rn.times(centeroid, 1/(double)singlePoints.size(), centeroid);
//		
//		double max1=0,min1=0,max2=0,min2=0;		
//		double[] point;
//		double dist;		
//		for(int i=0;i<singlePoints.size();i++){
//			point=Rn.subtract(null, singlePoints.get(i), centeroid);
//			dist=Rn.innerProduct(faceDir1, point);
//			if(dist>max1) max1=dist; 
//			if(dist<min1) min1=dist; 
//			dist=Rn.innerProduct(faceDir2, point);
//			if(dist>max2) max2=dist; 
//			if(dist<min2) min2=dist; 			
//		}			
//		sgc.addChild(Scan3DPointCloudUtility.projectPointCloud(singlePoints, colors, centeroid, faceDir1, faceDir2, max1, min1, max2, min2, texRes));
//
////		max1=0; min1=0;	max2=0; min2=0;			
////		for(int i=0;i<singlePoints.size();i++){
////			point=Rn.subtract(null, singlePoints.get(i), centeroid);
////			dist=Rn.innerProduct(faceDir3, point);
////			if(dist>max1) max1=dist; 
////			if(dist<min1) min1=dist; 
////			dist=Rn.innerProduct(faceDir2, point);
////			if(dist>max2) max2=dist; 
////			if(dist<min2) min2=dist; 			
////		}			
////		sgc.addChild(Scan3DPointCloudUtility.projectPointCloud(singlePoints, colors, centeroid, faceDir3, faceDir2, max1, min1, max2, min2, texRes));
//
//		return sgc;
//	}
	
	
	
}
