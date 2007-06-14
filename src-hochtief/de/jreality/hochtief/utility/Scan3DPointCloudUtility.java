package de.jreality.hochtief.utility;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBHelper;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

public class Scan3DPointCloudUtility {
	
	
	public static SceneGraphComponent projectPointCloud(ArrayList<double[]> points, ArrayList<byte[]> vertexColors, double[] faceCenteroid, double[] faceDir1, double[] faceDir2, double max1, double min1, double max2, double min2, double texRes){
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		
		Rn.normalize(faceDir1, faceDir1);
		Rn.normalize(faceDir2, faceDir2);

		int texWidth=(int)((max1-min1)/texRes);
		int texHeight=(int)((max2-min2)/texRes);		
		
		System.out.println("texWidth="+texWidth);
		System.out.println("texHeight="+texHeight);
		
		if(texWidth<=0||texHeight<=0) return sgc;
		
		BufferedImage img = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = img.getRaster();
		
		for(int x=0;x<texWidth;x++){
			for(int y=0;y<texHeight;y++){
				raster.setPixel(x, y, new int[] {0,0,0,0});				
			}
		}
		
		int[][] matchCounter=new int[texWidth][texHeight];
		
		for(int i=0;i<points.size();i++){
			
			int[] color=new int[4];
			byte[] colorTemp=vertexColors.get(i);
			for(int c=0;c<3;c++)
				color[c]=colorTemp[c]+(int)((double)255/2.0);;
			color[3]=255;
			
			
			double[] point=new double[3];
			double[] pointTemp=points.get(i);
			for(int p=0;p<3;p++)
				point[p]=pointTemp[p];
			Rn.subtract(point, point, faceCenteroid);
			
			//calc pixel-coordinates
			int x=(int)((Rn.innerProduct(point, faceDir1)-min1)/(max1-min1)*(double)(texWidth-1));
			int y=(int)((Rn.innerProduct(point, faceDir2)-min2)/(max2-min2)*(double)(texHeight-1));

			//average colors in one Pixel
			if(matchCounter[x][y]>0){
				int[] oldColor=new int[4];
				oldColor=raster.getPixel(x, y, oldColor);
				color[0]=(int)((double)(color[0]+matchCounter[x][y]*oldColor[0])/(double)(matchCounter[x][y]+1));
				color[1]=(int)((double)(color[1]+matchCounter[x][y]*oldColor[1])/(double)(matchCounter[x][y]+1));
				color[2]=(int)((double)(color[2]+matchCounter[x][y]*oldColor[2])/(double)(matchCounter[x][y]+1));
			}
		
			raster.setPixel(x, y, color);	
			matchCounter[x][y]++;
		}
		
		//debug:
		int pixelCount=0;
		for(int i=0;i<matchCounter.length;i++){
			for(int j=0;j<matchCounter[0].length;j++){
				if(matchCounter[i][j]!=0)
					pixelCount++;
			}			
		}
		System.out.println("used Pixels: "+pixelCount+"/"+(texWidth*texHeight));
		
		
	
		double[][] faceVertices={
				Rn.add(null, Rn.times(null, max1, faceDir1), Rn.times(null, max2, faceDir2)),
				Rn.add(null, Rn.times(null, min1, faceDir1), Rn.times(null, max2, faceDir2)),
				Rn.add(null, Rn.times(null, min1, faceDir1), Rn.times(null, min2, faceDir2)),
				Rn.add(null, Rn.times(null, max1, faceDir1), Rn.times(null, min2, faceDir2))
		};
		for(int i=0; i<faceVertices.length;i++)
			Rn.add(faceVertices[i], faceVertices[i], faceCenteroid);
		
		IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
		ifsf.setVertexCount(faceVertices.length);
		ifsf.setVertexCoordinates(faceVertices);
		ifsf.setFaceCount(1);
		ifsf.setFaceIndices(new int[][] {{0,1,2,3}});
		ifsf.setVertexTextureCoordinates(new double[][] {{1,1},{0,1},{0,0},{1,0}});
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		
		sgc.setGeometry(ifsf.getGeometry());
		sgc.setAppearance(new Appearance());
		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
		sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		
		ImageData imgData=new ImageData(img);		
		TextureUtility.createTexture(sgc.getAppearance(),"polygonShader", imgData, false);
		
		//debug:
//		RIBHelper.writeTexture(imgData, "pointCloudTest"+(texWidth*texHeight));
		
		
		//debug:
//		SceneGraphComponent origPointCloudSgc=new SceneGraphComponent("origPointCloud");
//		double[][] verts=new double[points.size()][3];
//		for(int i=0;i<verts.length;i++)
//			verts[i]=points.get(i);
//		PointSetFactory origPointCloud=new PointSetFactory();
//		origPointCloud.setVertexCount(verts.length);
//		origPointCloud.setVertexCoordinates(verts);
//		origPointCloud.update();
//		origPointCloudSgc.setGeometry(origPointCloud.getPointSet());
//		origPointCloudSgc.setAppearance(new Appearance());
//		origPointCloudSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		origPointCloudSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		sgc.addChild(origPointCloudSgc);
		
		return sgc;
	}
	
	
	

}
