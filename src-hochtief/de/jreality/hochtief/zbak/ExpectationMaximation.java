package de.jreality.hochtief.zbak;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import de.jreality.math.Rn;

public class ExpectationMaximation {
	
	private static DenseMatrix unitMatrix=new DenseMatrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});
	
	public static double[][] calculateParameters(int componentCount, double minChange, double[][] points){
		//params: uj0, uj1, uj2, sigma00j, sigma01j, sigma02j, sigma10j, sigma11j, sigma12j, sigma20j, sigma21j, sigma22j, alphaj
		double[][] params=new double[componentCount][];
		params[0]=new double[] {0,0,0, 1,0,0, 0,1,0, 0,0,1, 1}; 
		
		
		double thisPX,lastPX;
		for(int currentComponentCount=1; currentComponentCount<=componentCount; currentComponentCount++){   //increase number of components
			System.out.println("maximizing over "+currentComponentCount+" components");
			lastPX=-499999999;
			thisPX=499999999;
			
			double[][] p=new double[points.length][currentComponentCount];
			
			while(thisPX-lastPX > minChange){
				lastPX=thisPX;
				
				double[][] centeroid=new double[currentComponentCount][];
				double[] det=new double[currentComponentCount];
				DenseMatrix cov;
				DenseMatrix[] invCov=new DenseMatrix[currentComponentCount];
				double[] alpha=new double[currentComponentCount];
				for(int c=0;c<currentComponentCount;c++){					
					centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			
					det[c]=det(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
					cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
					invCov[c]=new DenseMatrix(3,3);
					unitMatrix.solve(cov, invCov[c]);
					alpha[c]=params[c][12];
				}
				
				p=eStep(p, points,centeroid,det,invCov,alpha); // ->just use params to currentComponentCount
				params=mStep(p, points, params);
				
				thisPX=Math.log(p(points,centeroid,det,invCov,alpha));
			}
			
			if(currentComponentCount<componentCount){
				params=initNextComponent(currentComponentCount,params);
			}
		}		
		return params;
	}
	
	private static double[][] eStep(double[][] p, double[][] points,  double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha) {	
		for(int i=0;i<p.length;i++){
			double pSum=Math.log(p(points[i],centeroid,det,invCov,alpha));
			if(pSum<-10000000) pSum=-10000000;			
			for(int c=0;c<p[0].length;c++){	
				p[i][c]=pComp(points[i],pSum,centeroid[c],det[c],invCov[c],alpha[c]); 
			}
		}
		return p;
	}

	private static double[][] mStep(double[][] p, double[][] points, double[][] params) {
		
		for(int c=0;c<p[0].length;c++){
			
			int Nc=0;
			double[] centeroid=new double[3];			
			for(int i=0;i<points.length;i++){
				Nc+=p[i][c];	
				Rn.add(centeroid, centeroid, Rn.times(null, p[i][c], points[i]));
			}
			Rn.times(centeroid, 1.0/(double)Nc, centeroid);
			
			double[] cov=new double[9];
			for(int i=0;i<points.length;i++){
				double[] centeredPoint=Rn.subtract(null, points[i], centeroid);				
				for(int x=0;x<3;x++){
					for(int y=0;y<3;y++){
						cov[3*x+y]+=centeredPoint[x]*centeredPoint[y]*p[i][c];
					}	
				}
			}
			for(int x=0;x<9;x++){
				cov[x]/=(double)Nc;
			}
			
			double alpha=(double)Nc/(double)points.length;

			params[c][0]=centeroid[0]; params[c][1]=centeroid[1]; params[c][2]=centeroid[2];
			params[c][3]=cov[0]; params[c][4]=cov[1]; params[c][5]=cov[2];
			params[c][6]=cov[3]; params[c][7]=cov[4]; params[c][8]=cov[5];
			params[c][9]=cov[6]; params[c][10]=cov[7]; params[c][11]=cov[8];
			params[c][12]=alpha;		
		}
		
		return params;
	}


	private static double[][] initNextComponent(int currentComponentCount, double[][] params) {
		//get component with biggest alpha
		int splitComponentNr=0;
		double maxAlpha=0;					
		for(int c=0;c<currentComponentCount;c++){			
			if(params[c][12]>maxAlpha){
				maxAlpha=params[c][12];
				splitComponentNr=c;
			}
		}
		
		DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[splitComponentNr][3],params[splitComponentNr][4],params[splitComponentNr][5]},{params[splitComponentNr][6],params[splitComponentNr][7],params[splitComponentNr][8]},{params[splitComponentNr][9],params[splitComponentNr][10],params[splitComponentNr][11]}});
		SymmPackEVD evd=null;
		try {
			evd = SymmPackEVD.factorize(covMtx);
		} catch (NotConvergedException e) {e.printStackTrace();}
		DenseMatrix eigM=evd.getEigenvectors();
		double[] maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
		double maxEv=evd.getEigenvalues()[2];
		 
		//new component	
		params[currentComponentCount]=new double[params[0].length];
		double[] newCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
		Rn.add(newCenteroid, newCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
		params[currentComponentCount][0]=newCenteroid[0];
		params[currentComponentCount][1]=newCenteroid[1];
		params[currentComponentCount][2]=newCenteroid[2];		
		for(int i=3;i<12;i++)
			params[currentComponentCount][i]=0.25*params[splitComponentNr][i];			
		params[currentComponentCount][12]=0.5*params[splitComponentNr][12];
		
		//update splitted component
		double[] updatedCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
		Rn.add(updatedCenteroid, updatedCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
		params[splitComponentNr][0]=updatedCenteroid[0];
		params[splitComponentNr][1]=updatedCenteroid[1];
		params[splitComponentNr][2]=updatedCenteroid[2];		
		for(int i=3;i<12;i++)
			params[splitComponentNr][i]=0.25*params[splitComponentNr][i];			
		params[splitComponentNr][12]=0.5*params[splitComponentNr][12];		
		
		return params;
	}
	
//	private static double[][] initNextComponent(int currentComponentCount, double[][] params) {
//		//get component with biggest var
//		int splitComponentNr=0;
//		double maxEv=0;
//		double[] maxEig=new double[3];			
//		for(int i=0;i<currentComponentCount;i++){			
//			DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[i][3],params[i][4],params[i][5]},{params[i][6],params[i][7],params[i][8]},{params[i][9],params[i][10],params[i][11]}});
//			SymmPackEVD evd=null;
//			try {
//				evd = SymmPackEVD.factorize(covMtx);
//			} catch (NotConvergedException e) {e.printStackTrace();}
//			
//			if(evd.getEigenvalues()[2]>maxEv){
//				maxEv=evd.getEigenvalues()[2];
//				splitComponentNr=i;
//				DenseMatrix eigM=evd.getEigenvectors();
//				maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
//			}
//		}
//		
//		//new component	
//		params[currentComponentCount]=new double[params[0].length];
//		double[] newCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
//		Rn.add(newCenteroid, newCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
//		params[currentComponentCount][0]=newCenteroid[0];
//		params[currentComponentCount][1]=newCenteroid[1];
//		params[currentComponentCount][2]=newCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[currentComponentCount][i]=0.25*params[splitComponentNr][i];			
//		params[currentComponentCount][12]=0.5*params[splitComponentNr][12];
//		
//		//update splitted component
//		newCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
//		Rn.add(newCenteroid, newCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
//		params[splitComponentNr][0]=newCenteroid[0];
//		params[splitComponentNr][1]=newCenteroid[1];
//		params[splitComponentNr][2]=newCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[splitComponentNr][i]=0.25*params[splitComponentNr][i];			
//		params[splitComponentNr][12]=0.5*params[splitComponentNr][12];		
//		
//		return params;
//	}

	
	
	private static double pComp(double[] point, double pSumC, double[] centeroid, double det, DenseMatrix invCov, double alpha) {
		double pSingle=Math.log(alpha*p(point,centeroid,det,invCov));
		if(pSingle<-100000) pSingle=-100000;
		return Math.exp(pSingle-pSumC);
	}
	
	private static double p(double[][] points, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){		
		double p=1;
		for(int i=0;i<points.length;i++)
			p*=p(points[i],centeroid,det,invCov,alpha);
		return p;
	}
	
	private static double p(double[] point, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){
		double p=0;
		for(int i=0;i<centeroid.length;i++)
			p+=alpha[i]*p(point,centeroid[i],det[i],invCov[i]);
		return p;
	}
	
	private static double p(double[] point, double[] centeroid, double det, DenseMatrix invCov){
		double[] pointCentered=Rn.subtract(null, point, centeroid);
		
		double p=0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				p+=pointCentered[i]*pointCentered[j]*invCov.get(i,j);				
			}		
		}
		p*=-0.5;
		p=Math.exp(p);
		p/=Math.sqrt(det*Math.pow(2*Math.PI, 3));

		return p;
	}
	
	public static double det(double a00, double a01, double a02, double a10, double a11, double a12, double a20, double a21, double a22){
		return a00*(a11*a22-a12*a21)-a01*(a12*a20-a10*a22)+a02*(a10*a21-a11*a20);	
	}
	
	public static int[] evalPoints(double[][] points, double[][] params){
		int[] compId=new int[points.length];
		
		double[][] centeroid=new double[params.length][];
		double[] det=new double[params.length];
		DenseMatrix cov;
		DenseMatrix[] invCov=new DenseMatrix[params.length];
		double[] alpha=new double[params.length];
		double[] centerP=new double[params.length];
		for(int c=0;c<params.length;c++){
			centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			
			det[c]=det(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
			cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			invCov[c]=new DenseMatrix(3,3);
			unitMatrix.solve(cov, invCov[c]);
			alpha[c]=params[c][12];
			centerP[c]=p(centeroid[c],centeroid[c],det[c],invCov[c]);
		}
		
		for(int i=0;i<points.length;i++){
			int maxComponent=0;
			double maxP=0;		
			
			double pSum=Math.log(p(points[i],centeroid,det,invCov,alpha));
			if(pSum<-10000000) pSum=-10000000;	
			
			for(int c=0;c<params.length;c++){
//				double p=p(points[i],centeroid[c],det[c],invCov[c]);
				double p=pComp(points[i],pSum,centeroid[c],det[c],invCov[c],alpha[c]); 
				if(p>maxP){
					maxP=p;
					maxComponent=c;
				}
			}
//			if(maxP<centerP[maxComponent]/10000000000.0) compId[i]=-1; //???????????????
//			else compId[i]=maxComponent;
			
			System.out.println(maxP);
			compId[i]=maxComponent;
		}
		return compId;
	}

}
