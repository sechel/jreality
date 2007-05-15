package de.jreality.hochtief;

import java.awt.Color;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class EdgeDetector {
	
	public static int EDGE_POINTS_TYPE_NOEDGE=0;
	public static int EDGE_POINTS_TYPE_BEND=1;
	public static int EDGE_POINTS_TYPE_FACEBORDER=-1;
	public static int EDGE_POINTS_TYPE_SINGLEPOINT=-2;
	
	public static int[][] detect(double varianzThreshold, double maxNbhDistance, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length; int N=depth[0].length;
		int[][] edgeId=new int[M][N]; 
		double[] nullVec=new double[]{0,0,0};

		double maxNB=0;
		double minNB=10000;
		double maxCov=0;
		double minCov=10000;
		int usedPoints=0;
		double averageCov=0;
		
		double[][][] normals=Scan3DUtility.getVertexNormals(depthThreshold, depth, faceId);

		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){		
				int adjacentPointsCount=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId).length;
				if(adjacentPointsCount>2 && adjacentPointsCount<8)
					edgeId[i][j]=EDGE_POINTS_TYPE_FACEBORDER;
				else if(adjacentPointsCount>2){
					int neighborhoodSize=Scan3DUtility.getNeighborhoodSize(i, j, maxNbhDistance, depthThreshold, depth, faceId);			

					if(neighborhoodSize>maxNB) maxNB=neighborhoodSize;
					if(neighborhoodSize<minNB) minNB=neighborhoodSize;

					int[][] nbh=Scan3DUtility.getNeighborhood(i, j, neighborhoodSize, depthThreshold, depth, faceId);

					int localNormalsCount=0;
					for(int n=0;n<nbh.length;n++){
						if(normals[nbh[n][0]][nbh[n][1]]!=null && !Rn.equals(normals[i][j],nullVec))
							localNormalsCount++;
					}
					if(localNormalsCount>0){
						double[][] localNormals=new double[localNormalsCount+1][2];
						localNormalsCount=0;
						for(int n=0;n<nbh.length;n++){
							if(normals[nbh[n][0]][nbh[n][1]]!=null && !Rn.equals(normals[i][j],nullVec)){
								localNormals[localNormalsCount]=normals[nbh[n][0]][nbh[n][1]];
								localNormalsCount++;
							}
						}
						localNormals[localNormalsCount]=normals[i][j];

						double[] cov=Scan3DUtility.getCovarianzMatrix(localNormals);

						double max=0;
						for(int c=0;c<cov.length;c++){
							if(Math.abs(cov[c])>max)
								max=Math.abs(cov[c]);
						}
						
						if(max>maxCov) maxCov=max;
						if(max<minCov) minCov=max;
						
						averageCov+=max;
						usedPoints+=1;

						if(max>varianzThreshold)
							edgeId[i][j]=EDGE_POINTS_TYPE_BEND;
					}
				}else{
					edgeId[i][j]=EDGE_POINTS_TYPE_SINGLEPOINT;
				}

			}
			//System.out.println("edge detection "+(int)(Math.ceil((double)i/(double)M*100))+"% finished");
		}

		System.out.println("maxNB="+maxNB);
		System.out.println("minNB="+minNB);
		System.out.println("maxCov="+maxCov);
		System.out.println("minCov="+minCov);
		averageCov=averageCov/(double)usedPoints;
		System.out.println("averageCov="+averageCov);
		
		return edgeId;
	}
	
	public static SceneGraphComponent getEdgePointsSgc(int edgeType, Color edgePointColor, int[][] edgeId, int[][] faceId, int[] faceSize, int minVertexCount, double[][] depth){
		int M=depth.length; int N=depth[0].length;
		SceneGraphComponent edgeNode=new SceneGraphComponent("edgePoints");
		edgeNode.setAppearance(new Appearance());
		edgeNode.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		edgeNode.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		edgeNode.getAppearance().setAttribute(CommonAttributes.POINT_SIZE,20.0);
		edgeNode.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,edgePointColor);
		
		int pointCount=0;
		for(int f=0;f<faceSize.length;f++){
			if(faceSize[f]>minVertexCount){
				for(int i=0;i<M;i++){
					for(int j=0;j<N;j++){
						if(faceId[i][j]==f && edgeId[i][j]==edgeType){
							pointCount++;
						}
					}
				}
				double[][] points=new double[pointCount][3];
				pointCount=0;
				for(int i=0;i<M;i++){
					for(int j=0;j<N;j++){
						if(faceId[i][j]==f && edgeId[i][j]==edgeType){
							points[pointCount]=Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
							pointCount++;
						}
					}
				}
				PointSetFactory edges=new PointSetFactory();
				edges.setVertexCount(pointCount);
				edges.setVertexCoordinates(points);
				edges.update();
				SceneGraphComponent cmp=new SceneGraphComponent();
				cmp.setGeometry(edges.getPointSet());
				edgeNode.addChild(cmp);	
				
				String type="no type";
				if(edgeType==EDGE_POINTS_TYPE_BEND) type="bend";
				if(edgeType==EDGE_POINTS_TYPE_FACEBORDER) type="faceBorder";
				System.out.println("edgePoints ("+type+"): "+pointCount+" in face "+f);
			}
		}
		
		return edgeNode;
	}

}
