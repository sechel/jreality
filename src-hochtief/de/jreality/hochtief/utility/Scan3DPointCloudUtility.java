package de.jreality.hochtief.utility;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBHelper;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

public class Scan3DPointCloudUtility {
	
	
	public static SceneGraphComponent projectPointCloud(ArrayList<double[]> points, ArrayList<byte[]> vertexColors, double[] faceCentroid, double[] faceDir1, double[] faceDir2, double max1, double min1, double max2, double min2, double texRes){
		
		
		System.out.println("projecting "+points.size()+" points");
		
		Rn.normalize(faceDir1, faceDir1);
		Rn.normalize(faceDir2, faceDir2);
		
		double[][] faceVertices={
				Rn.add(null, Rn.times(null, max1, faceDir1), Rn.times(null, max2, faceDir2)),
				Rn.add(null, Rn.times(null, min1, faceDir1), Rn.times(null, max2, faceDir2)),
				Rn.add(null, Rn.times(null, min1, faceDir1), Rn.times(null, min2, faceDir2)),
				Rn.add(null, Rn.times(null, max1, faceDir1), Rn.times(null, min2, faceDir2))
		};
		for(int i=0; i<faceVertices.length;i++)
			Rn.add(faceVertices[i], faceVertices[i], faceCentroid);
		
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
		

		
		int texWidth=(int)((max1-min1)/texRes);
		int texHeight=(int)((max2-min2)/texRes);		
		
		System.out.println("texWidth="+texWidth);
		System.out.println("texHeight="+texHeight);
		
		
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
				color[c]=colorTemp[c];
			color[3]=255;
			
			
			double[] point=new double[3];
			double[] pointTemp=points.get(i);
			for(int p=0;p<3;p++)
				point[p]=pointTemp[p];
			Rn.subtract(point, point, faceCentroid);
			
			int x=(int)((Rn.innerProduct(point, faceDir1)-min1)/(max1-min1)*(double)(texWidth-1));
			int y=(int)((Rn.innerProduct(point, faceDir2)-min2)/(max2-min2)*(double)(texHeight-1));

			if(matchCounter[x][y]>0){
				int[] oldColor=new int[4];
				raster.getPixel(x, y, oldColor);
				color[0]=(int)((double)(color[0]+matchCounter[x][y]*oldColor[0])/(double)(matchCounter[x][y]+1));
				color[1]=(int)((double)(color[1]+matchCounter[x][y]*oldColor[1])/(double)(matchCounter[x][y]+1));
				color[2]=(int)((double)(color[2]+matchCounter[x][y]*oldColor[2])/(double)(matchCounter[x][y]+1));
			}
			
			if(i%100==0)
				System.out.println("x,y="+x+","+y+"; color="+color[0]+","+color[1]+","+color[2]+","+color[3]);
			
			raster.setPixel(x, y, color);	
			matchCounter[x][y]++;
		}


		
		
//		for(int x=0;x<texWidth;x++){
//			for(int y=0;y<texHeight;y++){
//				raster.setPixel(x, y, new int[] {(int)((double)(256*x)/(double)texWidth),0,0,255});				
//			}
//		}
		
//		img.setData(raster);
		
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		sgc.setGeometry(ifsf.getGeometry());
		sgc.setAppearance(new Appearance());
		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
		sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,true);
		
		ImageData imgData=new ImageData(img);
		
		
		System.err.println("write tex..");
		RIBHelper.writeTexture(imgData, "texTest");
		System.err.println("..completed");
		
//		ImageData imgData=null;
//		try {
//			imgData = ImageData.load(Input.getInput("textures/outfactory3.png"));
//		} catch (IOException e) {e.printStackTrace();}
		
		TextureUtility.createTexture(sgc.getAppearance(),"polygonShader", imgData, false);
		
		return sgc;
	}
	
	
	

}
