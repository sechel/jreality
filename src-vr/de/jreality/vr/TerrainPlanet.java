package de.jreality.vr;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.SceneGraphComponent;

public class TerrainPlanet {
	
	/**
	 * @author bleicher
	 *  
	 */
	public static SceneGraphComponent createPlanet(int resolution, double radius, double roughness, boolean flattenPoles, boolean setDefaultTexCoords){
		SceneGraphComponent sgc=new SceneGraphComponent("planet");
		resolution*=4;
		double phiX;
		double phiY;
		double rg=1;
		double[][] points=new double[resolution*resolution][3];	
		for(int i=0; i<resolution; i++){
			for(int j=0; j<resolution; j++){
				phiX=2*Math.PI*(double)i/(double)(resolution);
				phiY=Math.PI*(double)j/(double)(resolution-1)+Math.PI/2;
				rg=roughness*radius*2*(Math.random()-0.5);
				if(flattenPoles)
					rg*=0.5*(1-Math.cos(2*phiY+Math.PI));
				points[i*resolution+j][0]=(rg+radius)*Math.cos(phiX)*Math.cos(phiY);				
				points[i*resolution+j][2]=(rg+radius)*Math.sin(phiX)*Math.cos(phiY);	
				points[i*resolution+j][1]=(rg+radius)*Math.sin(phiY);
			}		
		}	
		
		int[][] inds=new int[resolution*(resolution-1)][4];
		int i2;
		for(int i=0; i<resolution; i++){
			i2=i+1;
			if(i2>resolution-1) i2=0;
			inds[i*(resolution-1)][0]=0;
			inds[i*(resolution-1)][3]=i*resolution+1;
			inds[i*(resolution-1)][2]=i2*resolution+1;
			inds[i*(resolution-1)][1]=0;
			for(int j=1; j<resolution-2; j++){
				inds[i*(resolution-1)+j][0]=i*resolution+j;
				inds[i*(resolution-1)+j][3]=i*resolution+j+1;
				inds[i*(resolution-1)+j][2]=i2*resolution+j+1;
				inds[i*(resolution-1)+j][1]=i2*resolution+j;
			}
			inds[i*(resolution-1)+resolution-2][0]=(i+1)*resolution-2;
			inds[i*(resolution-1)+resolution-2][3]=resolution*resolution-1;
			inds[i*(resolution-1)+resolution-2][2]=resolution*resolution-1;
			inds[i*(resolution-1)+resolution-2][1]=(i2+1)*resolution-2;
		}	
		
		double[][] texCoords=null;
		if(setDefaultTexCoords){
			texCoords=new double[resolution*resolution][2];
			for(int i=0; i<resolution; i++){
				for(int j=0; j<resolution; j++){
					texCoords[i*resolution+j]=new double[] {i,j};
				}
			}
		}
		
		IndexedFaceSetFactory ifs=new IndexedFaceSetFactory();
		ifs.setVertexCount(resolution*resolution);
		ifs.setVertexCoordinates(points);
		ifs.setFaceCount(resolution*(resolution-1));
		ifs.setFaceIndices(inds);	
		if(setDefaultTexCoords)
			ifs.setVertexTextureCoordinates(texCoords);
		ifs.setGenerateEdgesFromFaces(true);
		ifs.setGenerateVertexNormals(false);
		ifs.setGenerateFaceNormals(true);
		ifs.update();	
		sgc.setGeometry(ifs.getGeometry());
		
		return sgc;
	}
	
	
}
