package de.jreality.hochtief.processing;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;

import de.jreality.hochtief.utility.Scan3DPointCloudUtility;
import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;

public class PointCloudSimplifier {

	
	public static SceneGraphComponent getSimplifiedPointCloud(double texRes, int[][] edgeId, double[][] depth, byte[][] colorR, byte[][] colorG, byte[][] colorB){
		int M=depth.length;  int N=depth[0].length;
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		ArrayList<double[]> singlePoints=new ArrayList<double[]>();
		ArrayList<byte[]> colors=new ArrayList<byte[]>();
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){				
				if(edgeId[i][j]==EdgeDetector.POINT_TYPE_SINGLEPOINT){
					singlePoints.add(Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N));
					colors.add(new byte[]{colorR[i][j],colorG[i][j],colorB[i][j],1});					
				}				
			}			
		}
		
		DenseMatrix covMtx=new DenseMatrix(Scan3DUtility.getCovarianzMatrix(singlePoints));
		
		SymmPackEVD evd=null;
		try {
			evd = SymmPackEVD.factorize(covMtx);
		} catch (NotConvergedException e) {e.printStackTrace();}
		
		//double[] ev=evd.getEigenvalues();
		DenseMatrix eig=evd.getEigenvectors();
		
		double[] faceDir1=new double[] {eig.get(0, 2),eig.get(1, 2),eig.get(2, 2)};
		double[] faceDir2=new double[] {eig.get(0, 1),eig.get(1, 1),eig.get(2, 1)};
		Rn.normalize(faceDir1, faceDir1);
		Rn.normalize(faceDir2, faceDir2);
		
		
		double[] centeroid=new double[3];
		for(int i=0;i<singlePoints.size();i++)
			Rn.add(centeroid, centeroid, singlePoints.get(i)); 
		Rn.times(centeroid, 1/(double)singlePoints.size(), centeroid);
		
		
		double max1=0,min1=0,max2=0,min2=0;
		
		double[] point;
		double dist;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){				
				if(edgeId[i][j]==EdgeDetector.POINT_TYPE_SINGLEPOINT){
					point=Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
					Rn.subtract(point, point, centeroid);
					dist=Rn.innerProduct(faceDir1, point);
					if(dist>max1) max1=dist; 
					if(dist<min1) min1=dist; 
					dist=Rn.innerProduct(faceDir2, point);
					if(dist>max2) max2=dist; 
					if(dist<min2) min2=dist; 					
				}				
			}			
		}
		
		
		sgc.addChild(Scan3DPointCloudUtility.projectPointCloud(singlePoints, colors, centeroid, faceDir1, faceDir2, max1, min1, max2, min2, texRes));

		return sgc;
	}
	
	
	
}
