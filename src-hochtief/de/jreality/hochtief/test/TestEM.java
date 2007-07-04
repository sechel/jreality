package de.jreality.hochtief.test;

import java.awt.Color;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.hochtief.pointclouds.ExpectationMaximation;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;

public class TestEM {
	
	public static void main(String[] args) {
		
		int componentCount=20;
		int findComponentsCount=20;
		int pointCount=2000;
		double range=15.0;
		double meanVar=3;
		
		double[][] centeroid=new double[componentCount][3];
		double[] var=new double[componentCount];
		for(int c=0;c<componentCount;c++){
			centeroid[c][0]=Math.random()*range-0.5*range;
			centeroid[c][1]=Math.random()*range-0.5*range;
			centeroid[c][2]=Math.random()*range-0.5*range;
			var[c]=2*(0.5*Math.random()+0.5)*meanVar;
		}
		
		
		
		double[][] points=new double[pointCount][];
		for(int i=0;i<pointCount;i++){
			double comp=Math.random()*(double)componentCount;
			comp=Math.floor(comp);			
			points[i]=new double[3];
			
			for(int d=0;d<3;d++){
				points[i][d]=var[(int)comp]*(0.5*Math.random()-0.5);
			}
			Rn.add(points[i], points[i], centeroid[(int)comp]);			
		}
		
		
		PointSetFactory psf=new PointSetFactory();
		psf.setVertexCount(pointCount);
		psf.setVertexCoordinates(points);
		psf.update();
		
		SceneGraphComponent pointsSgc=new SceneGraphComponent();
		pointsSgc.setGeometry(psf.getGeometry());
		pointsSgc.setAppearance(new Appearance());
		pointsSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pointsSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, false);
//		pointsSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 200.0);
		
//		System.out.println("points:\n"+Rn.toString(points));
//		System.out.println("centeroids:\n"+Rn.toString(centeroid));
		
		
		//////////////////////
		double[][] params=ExpectationMaximation.calculateParameters(findComponentsCount,0.0000001,points);
		SceneGraphComponent compSgc=new SceneGraphComponent();
		for(int c=0;c<findComponentsCount;c++){
			double[] center=new double[]{params[c][0],params[c][1],params[c][2]};
			DenseMatrix cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			
			//System.out.println(Rn.toString(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}}));
			
			SymmPackEVD evd=null;
			try {
				evd = SymmPackEVD.factorize(cov);
			} catch (NotConvergedException e) {e.printStackTrace();}
			
			//double[] ev=evd.getEigenvalues();
			DenseMatrix eig=evd.getEigenvectors();
			double[] faceDir1=new double[] {eig.get(0, 2),eig.get(1, 2),eig.get(2, 2)};
			double[] faceDir2=new double[] {eig.get(0, 1),eig.get(1, 1),eig.get(2, 1)};
			double[] faceDir3=new double[] {eig.get(0, 0),eig.get(1, 0),eig.get(2, 0)};
			
			Rn.times(faceDir1, evd.getEigenvalues()[2], faceDir1);
			Rn.times(faceDir2, evd.getEigenvalues()[1], faceDir2);
			Rn.times(faceDir3, evd.getEigenvalues()[0], faceDir3);
			
			double[][] compPoints=new double[4][];
			compPoints[0]=center;
			compPoints[1]=Rn.add(null, center, faceDir1);
			compPoints[2]=Rn.add(null, center, faceDir2);
			compPoints[3]=Rn.add(null, center, faceDir3);
			
			IndexedLineSetFactory ilsf=new IndexedLineSetFactory();
			ilsf.setVertexCount(4);
			ilsf.setVertexCoordinates(compPoints);
			ilsf.setLineCount(3);
			ilsf.setEdgeIndices(new int[][] {{0,1},{0,2},{0,3}});
			ilsf.setVertexLabels(new String[] {""+(c+1),"","",""});
			ilsf.update();
			
			SceneGraphComponent thisCompSgc=new SceneGraphComponent();
			thisCompSgc.setGeometry(ilsf.getGeometry());
			thisCompSgc.setAppearance(new Appearance());
			thisCompSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			thisCompSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
			thisCompSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLUE);
			thisCompSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
			compSgc.addChild(thisCompSgc);
		}
		
		
		
		////////////////////////
		
		SceneGraphComponent sgc=new SceneGraphComponent();
		sgc.addChild(pointsSgc);
		sgc.addChild(compSgc);
		
		ViewerApp.display(sgc);
		
	}
	
	
	

}
