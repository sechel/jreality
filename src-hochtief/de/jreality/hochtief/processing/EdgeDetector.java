package de.jreality.hochtief.processing;

import java.awt.Color;
import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;

import de.jreality.geometry.PointSetFactory;
import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class EdgeDetector {
	
	public static int POINT_TYPE_INSIDEFACE=0;
	public static int POINT_TYPE_BEND=-3;
	public static int POINT_TYPE_FACEBORDER=-2;
	public static int POINT_TYPE_SINGLEPOINT=-1;
	
	public static int[][] detect(double varianzThreshold, double maxNbhDistance, double depthThreshold, double[][] depth, int[][] faceId, int[] faceSize, double minVertexCount){
		
		double maxEdgeAngle=3.0/4.0*Math.PI; //<=pi
		double maxErrorAngle=1.0/15.0*Math.PI;
		
		double minVarEV3forEdge=2.0*Math.sin((Math.PI-maxEdgeAngle)/2.0)-Math.sin(maxErrorAngle/2.0);
		double maxVarEV2forEdge=Math.cos((Math.PI-maxEdgeAngle)/2.0)+Math.sin(maxErrorAngle/2.0);
		double maxVarEV1forEdge=2.0*Math.sin(maxErrorAngle/2.0);
		
		System.out.println("minVarEV3forEdge: "+minVarEV3forEdge);
		System.out.println("maxVarEV2forEdge: "+maxVarEV2forEdge);
		System.out.println("maxVarEV1forEdge: "+maxVarEV1forEdge);		
		
		int M=depth.length; int N=depth[0].length;
		int[][] edgeId=new int[M][N]; 
		double[] nullVec=new double[]{0,0,0};

		double maxNB=0;
		double minNB=10000;
		double maxMaxCov=0;
		double minMaxCov=10000;
		int usedPoints=0;
		double averageMaxCov=0;
		
		double[][][] normals=Scan3DUtility.getVertexNormals(depthThreshold, depth, faceId);
		double[][] maxVar=new double[M][N];

		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceSize[faceId[i][j]]<minVertexCount)
					edgeId[i][j]=POINT_TYPE_SINGLEPOINT;
				else{
					int[][] oneNbh=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId);
					if(oneNbh.length>2 && oneNbh.length<8)
						edgeId[i][j]=POINT_TYPE_FACEBORDER;
					else if(oneNbh.length>2){
						int neighborhoodSize=Scan3DUtility.getNeighborhoodSize(i, j, oneNbh, maxNbhDistance, depthThreshold, depth, faceId);			

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

							DenseMatrix covMtx=new DenseMatrix(Scan3DUtility.getCovarianzMatrix(localNormals));
							SymmPackEVD evd=null;
							try {
								evd = SymmPackEVD.factorize(covMtx);
							} catch (NotConvergedException e) {e.printStackTrace();}

							double[] ev=evd.getEigenvalues();  //ev[0]<=ev[1]<=ev[2]

							for(int k=0;k<ev.length;k++)
								ev[k]=2.0*Math.sqrt(ev[k]);

							maxVar[i][j]=ev[2];

							if(ev[2]>maxMaxCov) maxMaxCov=ev[2];
							if(ev[2]<minMaxCov) minMaxCov=ev[2];
							usedPoints++;
							averageMaxCov+=ev[2];

//							if(ev[2]>=minVarEV3forEdge){
//							edgeId[i][j]=POINT_TYPE_BEND;
//							}
//							if(ev[2]>=minVarEV3forEdge && ev[1]<=maxVarEV2forEdge && ev[0]<=maxVarEV1forEdge){
//							edgeId[i][j]=POINT_TYPE_BEND;
//							}
							if((ev[2]>=minVarEV3forEdge && ev[1]>=1.0-maxVarEV2forEdge && ev[1]<=maxVarEV2forEdge && ev[0]<=maxVarEV1forEdge)
									|| (ev[2]>=minVarEV3forEdge && ev[1]>=minVarEV3forEdge && ev[0]>=minVarEV3forEdge)){
								edgeId[i][j]=POINT_TYPE_BEND;
							}



						}
					}else{
						edgeId[i][j]=POINT_TYPE_SINGLEPOINT;
					}
				}
			}
			//System.out.println("edge detection "+(int)(Math.ceil((double)i/(double)M*100))+"% finished");
		}

		System.out.println("maxNB="+maxNB);
		System.out.println("minNB="+minNB);
		System.out.println("maxMaxCov="+maxMaxCov);
		System.out.println("minMaxCov="+minMaxCov);
		averageMaxCov=averageMaxCov/(double)usedPoints;
		System.out.println("averageMaxCov="+averageMaxCov);
		
		
		edgeId=deleteSinglePointEdges(edgeId, depthThreshold, depth, faceId);
//		edgeId=thinEdgeRegions(maxVar, edgeId, depthThreshold, depth, faceId);
//		edgeId=deleteSmallEdges(edgeId, depthThreshold, depth, faceId);
		
		
		edgeId=dilateBends(edgeId, depthThreshold, depth, faceId);
		edgeId=dilateBends(edgeId, depthThreshold, depth, faceId);
		edgeId=erodeBends(edgeId, depthThreshold, depth, faceId);
		edgeId=erodeBends(edgeId, depthThreshold, depth, faceId);

		
		return edgeId;
	}
	
	private static int[][] deleteSinglePointEdges( int [][] edgeId,double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length; int N=depth[0].length;		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(edgeId[i][j]==POINT_TYPE_BEND){
					int[][] nbh=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId);
					int nbhEdgePointCount=0;
					for(int n=0;n<nbh.length;n++){
						if(edgeId[nbh[n][0]][nbh[n][1]]==POINT_TYPE_BEND || edgeId[nbh[n][0]][nbh[n][1]]==POINT_TYPE_FACEBORDER || edgeId[nbh[n][0]][nbh[n][1]]==POINT_TYPE_SINGLEPOINT)
							nbhEdgePointCount++;
					}
					if(nbhEdgePointCount<1){
						edgeId[i][j]=POINT_TYPE_INSIDEFACE;
					}
				}
			}
		}
		return edgeId;
	}
	
//	private static int[][] thinEdgeRegions(double[][] maxVar, int [][] edgeId,double depthThreshold, double[][] depth, int[][] faceId){
//		int M=depth.length; int N=depth[0].length;	
//		
//		for(int i=0;i<M;i++){
//			for(int j=0;j<N;j++){
//				if(edgeId[i][j]==POINT_TYPE_BEND){
//					int[][] nbh=new int[][] {{i+1,j+1},{i,j+1},{i-1,j+1},{i-1,j},{i-1,j-1},{i,j-1},{i+1,j-1},{i+1,j},{i+1,j+1},{i,j+1}};		
//					ArrayList<int[]> effectiveNbh=Scan3DUtility.getSorted1NbhList(i, j, depthThreshold, depth, faceId);
//					int matchCount=0;
//					for(int n=1;n<nbh.length-1;n++){							
//						if(effectiveNbh.contains(nbh[n]) && effectiveNbh.contains(nbh[n-1]) && effectiveNbh.contains(nbh[n+1])){
//							if(   maxVar[i][j]>=maxVar[nbh[n-1][0]][nbh[n-1][1]] 
//							   && maxVar[i][j]>=maxVar[nbh[n+1][0]][nbh[n+1][1]]
//							   && ((maxVar[nbh[n][0]][nbh[n][1]]>=maxVar[nbh[n-1][0]][nbh[n-1][1]]    
//							   && maxVar[nbh[n][0]][nbh[n][1]]>=maxVar[nbh[n+1][0]][nbh[n+1][1]])
//							   || edgeId[nbh[n][0]][nbh[n][1]]==POINT_TYPE_FACEBORDER)){
//									matchCount++;
//							}		
//						}	
//						//if(matchCount>=2) break;
//					}
//					if(matchCount<2) edgeId[i][j]=POINT_TYPE_INSIDEFACE;
//				}
//			}
//		}
//		return edgeId;
//	}
	
	private static int[][] thinEdgeRegions(double[][] maxVar, int [][] edgeId,double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length; int N=depth[0].length;	
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(edgeId[i][j]==POINT_TYPE_BEND){
					int[][] nbh=new int[][] {{i+1,j+1},{i,j+1},{i-1,j+1},{i-1,j},{i-1,j-1},{i,j-1},{i+1,j-1},{i+1,j},{i+1,j+1},{i,j+1}};		
					ArrayList<int[]> effectiveNbh=Scan3DUtility.getSorted1NbhList(i, j, depthThreshold, depth, faceId);
					effectiveNbh.add(0,effectiveNbh.get(effectiveNbh.size()-1));
					effectiveNbh.add(effectiveNbh.get(1));
					
					int matchCount=0;
					
					for(int en=1;en<effectiveNbh.size()-1;en++){
						
						int nbhIndex=-1;
						for(int an=en;an<nbh.length-1;an++){
							if(effectiveNbh.get(en)[0]==nbh[an][0] && effectiveNbh.get(en)[1]==nbh[an][1]){
								if(effectiveNbh.get(en-1)[0]==nbh[an-1][0] && effectiveNbh.get(en-1)[1]==nbh[an-1][1]
								  && effectiveNbh.get(en+1)[0]==nbh[an+1][0] && effectiveNbh.get(en+1)[1]==nbh[an+1][1]){
									nbhIndex=an;
									break;
								}
							}
						}
				
						if(nbhIndex!=-1){
							if(   maxVar[i][j]>=maxVar[nbh[nbhIndex-1][0]][nbh[nbhIndex-1][1]] 
							   && maxVar[i][j]>=maxVar[nbh[nbhIndex+1][0]][nbh[nbhIndex+1][1]]
							   && ((maxVar[nbh[nbhIndex][0]][nbh[nbhIndex][1]]>=maxVar[nbh[nbhIndex-1][0]][nbh[nbhIndex-1][1]]    
							   && maxVar[nbh[nbhIndex][0]][nbh[nbhIndex][1]]>=maxVar[nbh[nbhIndex+1][0]][nbh[nbhIndex+1][1]])
							   || edgeId[nbh[nbhIndex][0]][nbh[nbhIndex][1]]==POINT_TYPE_FACEBORDER)){
									matchCount++;
							}		
						}	
						if(matchCount>=1) break;
					}
					if(matchCount<1) edgeId[i][j]=POINT_TYPE_INSIDEFACE;
				}
			}
		}
		return edgeId;
	}
	
	
	private static int[][] dilateBends(int[][] edgeId, double depthThreshold, double[][] depth, int[][] faceId) {
		int[][] newEdgeId=new int[edgeId.length][edgeId[0].length];
		for(int i=0;i<edgeId.length;i++){
			for(int j=0;j<edgeId[0].length;j++){
				if(newEdgeId[i][j]==0)
					newEdgeId[i][j]=edgeId[i][j];
				if(edgeId[i][j]==POINT_TYPE_BEND){				
					int[][] nbh=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId);
					for(int n=0;n<nbh.length;n++){
						if(edgeId[nbh[n][0]][nbh[n][1]]!=POINT_TYPE_FACEBORDER)
							newEdgeId[nbh[n][0]][nbh[n][1]]=POINT_TYPE_BEND;
					}
				}					
			}
		}
		return newEdgeId;
	}
	
	private static int[][] erodeBends(int[][] edgeId, double depthThreshold, double[][] depth, int[][] faceId) {
		int[][] newEdgeId=new int[edgeId.length][edgeId[0].length];		
		for(int i=0;i<edgeId.length;i++){
			for(int j=0;j<edgeId[0].length;j++){
				newEdgeId[i][j]=edgeId[i][j];
				if(edgeId[i][j]==POINT_TYPE_BEND){				
					int[][] nbh=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId);
					for(int n=0;n<nbh.length;n++){
						if(edgeId[nbh[n][0]][nbh[n][1]]!=POINT_TYPE_BEND && edgeId[nbh[n][0]][nbh[n][1]]!=POINT_TYPE_FACEBORDER){
							newEdgeId[i][j]=POINT_TYPE_INSIDEFACE;
							break;
						}
					}
				}				
			}
		}
		return newEdgeId;
	}



	public static SceneGraphComponent getEdgePointsSgc(int edgeType, Color edgePointColor, int[][] edgeId, int[][] faceId, int[] faceSize, int minVertexCount, double[][] depth){
		int M=depth.length; int N=depth[0].length;
		SceneGraphComponent edgeNode=new SceneGraphComponent();
		edgeNode.setAppearance(new Appearance());
		edgeNode.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		edgeNode.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		edgeNode.getAppearance().setAttribute(CommonAttributes.POINT_SIZE,80.0);
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
				if(pointCount>0){
					PointSetFactory edges=new PointSetFactory();
					edges.setVertexCount(pointCount);
					edges.setVertexCoordinates(points);
					edges.update();
					SceneGraphComponent cmp=new SceneGraphComponent();
					cmp.setGeometry(edges.getPointSet());
					edgeNode.addChild(cmp);
				}
				
				String type="no type";
				if(edgeType==POINT_TYPE_BEND) type="bend";
				if(edgeType==POINT_TYPE_FACEBORDER) type="faceBorder";
				System.out.println(type+"-points"+": "+pointCount+" in face "+f);
			}
		}
		
		return edgeNode;
	}

}
