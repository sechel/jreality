package de.jreality.hochtief;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;

/**
 * @author Nils Bleicher
 */

public class SimpleDepthFaceExtractor {
	
	private double depthThreshold=0.05;
	
	private double[][] depth;
	private int M, N;
	private int[][]	faceId;
	private int faceIdCount;
//	private int[] faceSizeCounter;
	
	public SimpleDepthFaceExtractor(double[][] depth){
		this.depth=depth;
		M=depth.length;
		N=depth[0].length;		
	}
	
	public void setDepthThreshold(double t){
		depthThreshold=t;
	}	

	public void process(){
		faceId=new int[M][N];
		faceIdCount=0;
		ArrayList<Integer> faceIdMap=new ArrayList<Integer>();
		boolean connectI, connectJ;	
		
		for(int i=0;i<M;i++){ 
			for(int j=0;j<N;j++){
				connectI=false;
				if(i-1>=0 && depth[i][j]!=0 && depth[i-1][j]!=0){
					if(Math.abs(depth[i][j]-depth[i-1][j])<depthThreshold*0.5*(depth[i][j]+depth[i-1][j]))
						connectI=true;					
				}
				connectJ=false;
				if(j-1>=0 && depth[i][j]!=0 && depth[i][j-1]!=0){
					if(Math.abs(depth[i][j]-depth[i][j-1])<depthThreshold*0.5*(depth[i][j]+depth[i][j-1]))
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
	
	public int[][] getFaceIds(){
		return faceId;
	}
	public int[] getFaceSizes(){
		int[] faceSizeCounter=new int[faceIdCount];
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				faceSizeCounter[faceId[i][j]]++;
			} 
		}
		return faceSizeCounter;
	}
	
	public static double[][] getFaceVertices(int faceNr, int faceSize, double[][] depth, int[][] faceId){		
		int M=depth.length;
		int N=depth[0].length;		
		double[][] verts=new double[faceSize][3];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){
					
					double[] p=convertDepthPoint(i, j, depth[i][j], M, N);
					
					verts[vertexCount][0]=p[0];
					verts[vertexCount][1]=p[1];
					verts[vertexCount][2]=p[2];
					vertexCount++;
				}
			} 
		}
		
		return verts;
	}
	
	public static double[] convertDepthPoint(int i, int j, double depth, int M, int N){
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
	
	public double[][] getTextureCoordinates(int faceNr, int faceSize, double offset){
		double[][] textureCoordinates = new double[faceSize][2];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){
					textureCoordinates[vertexCount][0] = -(double) j / N + (offset / (2 * Math.PI) + 0.5);
					textureCoordinates[vertexCount][1] = (double) i / M;
					vertexCount++;
				}
			}
		}
		return textureCoordinates;
	}
	
	
	
	public int[][] triangulate(int faceNr){		
		int[][] vertexLabel=new int[M][N];
		int vertexCount=0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){
					vertexLabel[i][j]=vertexCount;
					vertexCount++;					
				}else
					vertexLabel[i][j]=-1;
			}
		}
		
		LinkedList<int[]> indices=new LinkedList<int[]>();
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){	
				int i_1=i-1;
				if(i_1<0) i_1=M-1;
				int j_1=j-1;
				if(j_1<0) j_1=N-1;
				if(faceId[i][j]==faceNr){
					if(faceId[(i+1)%M][j]==faceNr){
						if(Math.abs(depth[i][j]-depth[(i+1)%M][j])<depthThreshold*0.5*(depth[i][j]+depth[(i+1)%M][j])){
							if(faceId[i][j_1]==faceNr){
								int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][j],vertexLabel[i][j_1]};
								if(triangleIsEquable(i, j, (i+1)%M, j, i, j_1)){
									indices.add(newTriangle);			
								}
							}
							if(faceId[(i+1)%M][(j+1)%N]==faceNr){
								int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][(j+1)%N],vertexLabel[(i+1)%M][j]};
								if(triangleIsEquable(i, j, (i+1)%M, j, (i+1)%M, (j+1)%N)){
									indices.add(newTriangle);	
								}
							}
						}
					}
					if(faceId[i_1][j]!=faceNr && faceId[i_1][(j+1)%N]==faceNr && faceId[i][(j+1)%N]==faceNr){
						int[] newTriangle={vertexLabel[i][j],vertexLabel[i_1][(j+1)%N],vertexLabel[i][(j+1)%N]};
						if(triangleIsEquable(i, j, i_1, (j+1)%N, i, (j+1)%N)){
							indices.add(newTriangle);
						}
					}
					if(faceId[(i+1)%M][j]!=faceNr && faceId[(i+1)%M][j_1]==faceNr && faceId[i][j_1]==faceNr){
						int[] newTriangle={vertexLabel[i][j],vertexLabel[(i+1)%M][j_1],vertexLabel[i][j_1]};
						if(triangleIsEquable(i, j, (i+1)%M, j_1, i, j_1)){
							indices.add(newTriangle);	
						}
					}
				}
			}
		}
		
		int[][] triangles=new int[indices.size()][];
		Iterator<int[]> it=indices.iterator();
		for(int t=0;t<indices.size();t++)
			triangles[t]=it.next();
		return triangles;
	}
	
	private double minSideRatio=0.05;	
	private double depthTriangleThreshold=depthThreshold;//0.14;
	private boolean triangleIsEquable(int i1, int j1, int i2, int j2, int i3, int j3){	
		double d1=Math.abs(depth[i1][j1]-depth[i2][j2]);
		double d2=Math.abs(depth[i1][j1]-depth[i3][j3]);
		double d3=Math.abs(depth[i3][j3]-depth[i2][j2]);		
		double max=Math.max(d1, Math.max(d2,d3));	
		
//		double min=Math.min(d1, Math.min(d2,d3));
//		if(min/max<minSideRatio) return false;
////		if(min/max<minSideRatio || max>depthThreshold) return false;
//		else return true;
		
		if(max==d1){
			if(max<depthTriangleThreshold*0.5*(depth[i1][j1]+depth[i2][j2])) return true;
		}else if(max==d2){
			if(max<depthTriangleThreshold*0.5*(depth[i1][j1]+depth[i3][j3])) return true;
		}else if(max==d3){
			if(max<depthTriangleThreshold*0.5*(depth[i3][j3]+depth[i2][j2])) return true;
		}
		return false;
	}
	
	public void showFaceVertices(int minFaceSize){		
		int[] faceSizeCounter=getFaceSizes();	
	
		SceneGraphComponent sceneRoot=new SceneGraphComponent();

		for(int f=0;f<faceSizeCounter.length;f++){
			if(faceSizeCounter[f]>minFaceSize){

				double[][] verts=getFaceVertices(f, faceSizeCounter[f],depth,faceId);
				
				PointSetFactory psf=new PointSetFactory();
				psf.setVertexCount(faceSizeCounter[f]);
				psf.setVertexCoordinates(verts);
				psf.update();
				
				SceneGraphComponent sgc=new SceneGraphComponent();
				sgc.setGeometry(psf.getGeometry());
				sceneRoot.addChild(sgc);
			}
		}
		
		sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ViewerApp.display(sceneRoot);
	}
	
	public void showTriangulation(int minVertexCount, String texturePath, double texOffset){
		SceneGraphComponent sceneRoot=new SceneGraphComponent();
		sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		sceneRoot.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);

		int[] faceSize=getFaceSizes();
		
		ImageData img=null;
		try {
			img = ImageData.load(Input.getInput(texturePath));
		} catch (IOException e) {}
		
		for(int i=0;i<faceSize.length;i++){
			if(faceSize[i]>minVertexCount){
				int[][] faceInds=triangulate(i);
				if(faceInds.length>0){
					System.out.println("creating face "+i);
					IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
					ifsf.setVertexCount(faceSize[i]);
					ifsf.setVertexCoordinates(getFaceVertices(i, faceSize[i], depth, faceId));
					ifsf.setFaceCount(faceInds.length);
					ifsf.setFaceIndices(faceInds);
					ifsf.setVertexTextureCoordinates(getTextureCoordinates(i, faceSize[i], texOffset));
					ifsf.setGenerateEdgesFromFaces(true);
					ifsf.setGenerateFaceNormals(true);
					ifsf.setGenerateVertexNormals(true);
					ifsf.update();

					SceneGraphComponent sgc=new SceneGraphComponent();
					sgc.setGeometry(ifsf.getGeometry());
					sgc.setAppearance(new Appearance());
					TextureUtility.createTexture(sgc.getAppearance(),"polygonShader", img, false);
					sceneRoot.addChild(sgc);

					System.out.println("vertexCount: "+faceSize[i]);
					System.out.println("faceCount:   "+faceInds.length+"\n");
				}
			}			
		}
		
		ViewerApp.display(sceneRoot);
		
	}


	
	
}
