package de.jreality.hochtief.pointclouds;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;

public class ExpectationMaximation {
	
	private static final double minValue=-999999999;
	private static DenseMatrix unitMatrix=new DenseMatrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});
	
	public static double[][] calculateParameters(int componentCount, double minChange, double[][] points){
		//params: uj0, uj1, uj2, sigma00j, sigma01j, sigma02j, sigma10j, sigma11j, sigma12j, sigma20j, sigma21j, sigma22j, alphaj
		double[][] params=new double[componentCount][13];
		params[0]=new double[] {0,0,0, 1,0,0, 0,1,0, 0,0,1, 1}; 
		
		
		double thisPX,lastPX;
		long time;
		long startAllTime=System.currentTimeMillis();
		for(int currentComponentCount=1; currentComponentCount<=componentCount; currentComponentCount++){   //increase number of components
			System.out.print("maximizing over "+currentComponentCount+" components");
			time=System.currentTimeMillis();
			lastPX=-499999999;
			thisPX=499999999;
			
			double[][] p=new double[points.length][currentComponentCount];
			int[] compId=new int[points.length];
			
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
				
//				thisPX=Math.log(pX(points,centeroid,det,invCov,alpha));
				thisPX=logpX(points,centeroid,det,invCov,alpha);
				//??????????????????????????????????????????????
//				compId=evalPoints(points, params);  //!is in 'while'
//				thisPX=1;
//				for(int i=0;i<compId.length;i++)
//					thisPX=thisPX*px(points[i],centeroid[compId[i]],det[compId[i]],invCov[compId[i]])/px(points[i],centeroid,det,invCov,alpha);
				
			}
			

			//timer
			double sum=0;
			for(int i=1;i<=currentComponentCount;i++) sum+=(double)i;
			double timeStep=(System.currentTimeMillis()-startAllTime)/sum;
			sum=0;
			for(int i=1;i<=componentCount;i++) sum+=(double)i;
			double estFinishedInTime=timeStep*sum-(System.currentTimeMillis()-startAllTime);
			System.out.append(" ..in "+Math.round((System.currentTimeMillis()-time)/1000.0)+" s, finished in ~ "+(Math.round(estFinishedInTime/100.0/60.0)/10.0)+" min\n");
		
		
			if(currentComponentCount<componentCount){	
				compId=evalPoints(points, params);
				params=initNextComponent(currentComponentCount,points,compId,params);
			}
		}
		
		System.out.println("maximation overall time: "+(Math.round((System.currentTimeMillis()-startAllTime)/100.0/60.0)/10.0)+" min");
		
		return params;
	}
	
	private static double[][] eStep(double[][] p, double[][] points,  double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha) {	
		for(int i=0;i<p.length;i++){
			double pSum=Math.log(px(points[i],centeroid,det,invCov,alpha));
			if(pSum<minValue) pSum=minValue;	
			for(int c=0;c<p[0].length;c++){	
				p[i][c]=pComp(points[i],pSum,centeroid[c],det[c],invCov[c],alpha[c]); 	
			}
		}
		return p;
	}

	private static double[][] mStep(double[][] p, double[][] points, double[][] params) {
		
		for(int c=0;c<p[0].length;c++){
			
			double Nc=0;
			for(int i=0;i<points.length;i++)
				Nc+=p[i][c];	
			
			if(Nc>0){			
				double[] centeroid=new double[3];			
				for(int i=0;i<points.length;i++)					
					Rn.add(centeroid, centeroid, Rn.times(null, p[i][c], points[i]));			
				Rn.times(centeroid, 1.0/Nc, centeroid);

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
					cov[x]=cov[x]/Nc;
//					if(cov[x]<minValue) cov[x]=minValue;
//					if(cov[x]>-minValue) cov[x]=-minValue;
				}

				double alpha=Nc/((double)points.length);

				params[c][0]=centeroid[0]; params[c][1]=centeroid[1]; params[c][2]=centeroid[2];
				params[c][3]=cov[0]; params[c][4]=cov[1]; params[c][5]=cov[2];
				params[c][6]=cov[3]; params[c][7]=cov[4]; params[c][8]=cov[5];
				params[c][9]=cov[6]; params[c][10]=cov[7]; params[c][11]=cov[8];
				params[c][12]=alpha;	
			}else{
				for(int i=3;i<params[c].length;i++)
					params[c][i]=0;
			}
		}
		
		return params;
	}


//	private static double[][] initNextComponent(int currentComponentCount, double[][] params) {
//		//get component with biggest alpha
//		int splitComponent=0;
//		double maxAlpha=0;					
//		for(int c=0;c<currentComponentCount;c++){			
//			if(params[c][12]>maxAlpha){
//				maxAlpha=params[c][12];
//				splitComponent=c;
//			}
//		}
//		
//		System.out.println("\nsplitting component nr "+splitComponent);
//		
//		DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[splitComponent][3],params[splitComponent][4],params[splitComponent][5]},{params[splitComponent][6],params[splitComponent][7],params[splitComponent][8]},{params[splitComponent][9],params[splitComponent][10],params[splitComponent][11]}});
//		SymmPackEVD evd=null;
//		try {
//			evd = SymmPackEVD.factorize(covMtx);
//		} catch (NotConvergedException e) {e.printStackTrace();}
//		DenseMatrix eigM=evd.getEigenvectors();
//		double[] maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
//		double maxEv=evd.getEigenvalues()[2];
//		
//	    System.out.println("splitting comp nr "+splitComponentNr+" with eigenvalue "+maxEv);
//	
//		//new component	
//		params[currentComponentCount]=new double[params[0].length];
//		double[] newCenteroid=new double[] {params[splitComponent][0],params[splitComponent][1],params[splitComponent][2]};
//		
//		System.out.println("old centeroid: "+Rn.toString(newCenteroid));
//		
//		Rn.add(newCenteroid, newCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
//		params[currentComponentCount][0]=newCenteroid[0];
//		params[currentComponentCount][1]=newCenteroid[1];
//		params[currentComponentCount][2]=newCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[currentComponentCount][i]=0.25*params[splitComponent][i];		
//		params[currentComponentCount][12]=0.5*params[splitComponent][12];
//		
//		System.out.println("new centeroid: "+Rn.toString(newCenteroid));
//		
//		//update splitted component
//		double[] updatedCenteroid=new double[] {params[splitComponent][0],params[splitComponent][1],params[splitComponent][2]};
//		Rn.add(updatedCenteroid, updatedCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
//		params[splitComponent][0]=updatedCenteroid[0];
//		params[splitComponent][1]=updatedCenteroid[1];
//		params[splitComponent][2]=updatedCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[splitComponent][i]=0.25*params[splitComponent][i];			
//		params[splitComponent][12]=0.5*params[splitComponent][12];		
//		
//		System.out.println("updated centeroid: "+Rn.toString(updatedCenteroid)+"/n");
//		
//		return params;
//	}
	
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
//	    System.out.println("splitting comp nr "+splitComponentNr+" with eigenvalue "+maxEv);
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
////		    params[currentComponentCount][i]=0.125*params[splitComponentNr][i];	
//		params[currentComponentCount][12]=0.5*params[splitComponentNr][12];
//		
//		//update splitted component
//		double[] updatedCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
//		Rn.add(updatedCenteroid, updatedCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
//		params[splitComponentNr][0]=updatedCenteroid[0];
//		params[splitComponentNr][1]=updatedCenteroid[1];
//		params[splitComponentNr][2]=updatedCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[splitComponentNr][i]=0.25*params[splitComponentNr][i];			
//		params[splitComponentNr][12]=0.5*params[splitComponentNr][12];		
//		
//		return params;
//	}
	
//	private static double[][] initNextComponent(int currentComponentCount, double[][] points, int[] compId, double[][] params) {
//		//get component with biggest dist
//		//int[] compId=evalPoints(points, params);
//		int splitComponentNr=0;
//		double maxDist=0;
//		double maxEv=0;
//		double[] maxEig=new double[3];	
//		for(int c=0;c<currentComponentCount;c++){			
//			double[] centeroid={params[c][0],params[c][1],params[c][2]};
//			DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
//			SymmPackEVD evd=null;
//			try {
//				evd = SymmPackEVD.factorize(covMtx);
//			} catch (NotConvergedException e) {e.printStackTrace();}
//			double[] dir1={evd.getEigenvectors().get(0,2),evd.getEigenvectors().get(1,2),evd.getEigenvectors().get(2,2)};
//			double[] dir2={evd.getEigenvectors().get(0,1),evd.getEigenvectors().get(1,1),evd.getEigenvectors().get(2,1)};
//			
//			double maxX=0,minX=0,maxY=0,minY=0;
//			for(int i=0;i<compId.length;i++){
//				if(compId[i]==c){
//					double[] centeredPoint=Rn.subtract(null, points[i], centeroid);
//					double x=Rn.innerProduct(dir1, centeredPoint);
//					double y=Rn.innerProduct(dir2, centeredPoint);
//					if(x>maxX) maxX=x;
//					if(x<minX) minX=x;
//					if(y>maxY) maxY=y;
//					if(y<minY) minY=y;
//				}
//			}			
//			if(maxX-minX>maxDist || maxY-minY>maxDist){
//				maxDist=Math.max(maxX-minX, maxY-minY);
//				splitComponentNr=c;
//				maxEv=evd.getEigenvalues()[2];
//				maxEig=dir1;
////				if(maxDist==maxX-minX){
////					maxEv=evd.getEigenvalues()[2];
////					maxEig=dir1;
////				}else{
////					maxEv=evd.getEigenvalues()[1];
////					maxEig=dir2;
////				}
//			}			
//		}
//		
//		System.out.println("splitting comp nr "+splitComponentNr+" with eigenvalue "+maxEv);
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
////		    params[currentComponentCount][i]=0.125*params[splitComponentNr][i];	
//		params[currentComponentCount][12]=0.5*params[splitComponentNr][12];
//		
//		//update splitted component
//		double[] updatedCenteroid=new double[] {params[splitComponentNr][0],params[splitComponentNr][1],params[splitComponentNr][2]};
//		Rn.add(updatedCenteroid, updatedCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
//		params[splitComponentNr][0]=updatedCenteroid[0];
//		params[splitComponentNr][1]=updatedCenteroid[1];
//		params[splitComponentNr][2]=updatedCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[splitComponentNr][i]=0.25*params[splitComponentNr][i];			
//		params[splitComponentNr][12]=0.5*params[splitComponentNr][12];		
//		
//		return params;
//	}
	
	private static double[][] initNextComponent(int currentComponentCount, double[][] points, int[] compId, double[][] params) {
		//get comp with biggest var, var calculated only by points with compId==c
//		int[] compId=evalPoints(points, params);
		
		int splitComponentNr=0;		
		double maxEv=0;
		double[] maxEig=new double[3];
		double[] splitCenteroid=new double[3];
		DenseMatrix splitCov=new DenseMatrix(3,3);		
		for(int c=0;c<currentComponentCount;c++){				
			ArrayList<double[]> compPoints=new ArrayList<double[]>();
			for(int i=0;i<points.length;i++){
				if(compId[i]==c)
					compPoints.add(points[i]);
			}
			if(compPoints.size()>0){
				double[] centeroid=new double[3];
				for(int i=0;i<compPoints.size();i++)
					Rn.add(centeroid, centeroid, compPoints.get(i)); 
				Rn.times(centeroid, 1.0/((double)compPoints.size()), centeroid);
				DenseMatrix cov=new DenseMatrix(Scan3DUtility.getCovarianzMatrix(compPoints,centeroid));

				SymmPackEVD evd=null;
				try {
					evd = SymmPackEVD.factorize(cov);
				} catch (NotConvergedException e) {e.printStackTrace();}

				if(evd.getEigenvalues()[2]>maxEv){
					splitComponentNr=c;
					maxEv=evd.getEigenvalues()[2];
					maxEig=new double[] {evd.getEigenvectors().get(0,2),evd.getEigenvectors().get(1,2),evd.getEigenvectors().get(2,2)};
					splitCenteroid=centeroid;
					splitCov=cov;
				}		
			}
		}
		
		System.out.println("splitting comp nr "+splitComponentNr+" with eigenvalue "+maxEv);
		
		//new component	
		params[currentComponentCount]=new double[params[0].length];
		double[] newCenteroid=Rn.add(null, splitCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
		params[currentComponentCount][0]=newCenteroid[0];
		params[currentComponentCount][1]=newCenteroid[1];
		params[currentComponentCount][2]=newCenteroid[2];		
		for(int i=3;i<12;i++)
			params[currentComponentCount][i]=0.25*splitCov.get(i/3-1,i%3);
		
		params[currentComponentCount][12]=0.5*params[splitComponentNr][12];
		
		//update splitted component
		double[] updatedCenteroid=Rn.add(null, splitCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
		params[currentComponentCount][0]=updatedCenteroid[0];
		params[currentComponentCount][1]=updatedCenteroid[1];
		params[currentComponentCount][2]=updatedCenteroid[2];		
		for(int i=3;i<12;i++)
			params[currentComponentCount][i]=0.25*splitCov.get(i/3-1,i%3);			
		params[splitComponentNr][12]=0.5*params[splitComponentNr][12];		
		
		return params;
	}

	//p(y=c|x,omega)
	private static double pComp(double[] point, double pSumC, double[] centeroid, double det, DenseMatrix invCov, double alpha) {
		double pSingle=Math.log(alpha*px(point,centeroid,det,invCov));
		if(pSingle<minValue) pSingle=minValue;
		return Math.exp(pSingle-pSumC);
	}
	
	//log(p(X,w|Omega)
	private static double logpX(double[][] points, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){
		double p=0;
		for(int i=0;i<points.length;i++){
			double sumC=0;
			for(int c=0;c<centeroid.length;c++)
				sumC+=alpha[c]*px(points[i],centeroid[c],det[c],invCov[c]);
			p+=Math.log(sumC);
		}		
		return p;
	}
	
	//p(X|Omega)
	private static double pX(double[][] points, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){		
		double p=1;
		for(int i=0;i<points.length;i++)
			p*=px(points[i],centeroid,det,invCov,alpha);
		return p;
	}
	
    //p(x|Omega)
	private static double px(double[] point, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){
		double p=0;
		for(int i=0;i<centeroid.length;i++)
			p+=alpha[i]*px(point,centeroid[i],det[i],invCov[i]);
		return p;
	}
	
	//p(x|omega)
	private static double px(double[] point, double[] centeroid, double det, DenseMatrix invCov){
		double[] pointCentered=Rn.subtract(null, point, centeroid);
		
		double p=0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				p+=pointCentered[i]*pointCentered[j]*invCov.get(i,j);				
			}		
		}
		p*=-0.5;
		p=Math.exp(p);
		double factor=Math.sqrt(det*Math.pow(2*Math.PI, 3));
		p=p/factor;
		
//		factor=1000*Rn.euclideanDistanceSquared(point, centeroid);
//		if(factor>1)
//			p=p/factor;
		
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
		double[][][] eig=new double[params.length][3][3];
		double[][] sigma=new double[params.length][3];
		for(int c=0;c<params.length;c++){
			centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			
			det[c]=det(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
			cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			invCov[c]=new DenseMatrix(3,3);
			unitMatrix.solve(cov, invCov[c]);
			alpha[c]=params[c][12];
			
			SymmPackEVD evd=null;
			try {
				evd = SymmPackEVD.factorize(cov);
			} catch (NotConvergedException e) {e.printStackTrace();}
			DenseMatrix eigM=evd.getEigenvectors();
			eig[c][0]=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
			eig[c][1]=new double[] {eigM.get(0, 1),eigM.get(1, 1),eigM.get(2, 1)};
			eig[c][2]=new double[] {eigM.get(0, 0),eigM.get(1, 0),eigM.get(2, 0)};
			sigma[c]=new double[] {Math.sqrt(evd.getEigenvalues()[2]),Math.sqrt(evd.getEigenvalues()[1]), Math.sqrt(evd.getEigenvalues()[0])};
		}
		
		for(int i=0;i<points.length;i++){
			int maxComponent=0;
			double maxP=0;		
			
			double pSum=Math.log(px(points[i],centeroid,det,invCov,alpha));
			if(pSum<minValue) pSum=minValue;	
			
			for(int c=0;c<params.length;c++){
//				double p=p(points[i],centeroid[c],det[c],invCov[c]);
				double p=pComp(points[i],pSum,centeroid[c],det[c],invCov[c],alpha[c]); 
				if(p>maxP){
					maxP=p;
					maxComponent=c;
				}
			}
			compId[i]=maxComponent;

//			double maxFactor=1.5;
//			double[] pointCentered=Rn.subtract(null, points[i], centeroid[maxComponent]);
//			if(Rn.innerProduct(eig[maxComponent][0], pointCentered)>maxFactor*sigma[maxComponent][0]) compId[i]=-1;
//			if(Rn.innerProduct(eig[maxComponent][1], pointCentered)>maxFactor*sigma[maxComponent][1]) compId[i]=-1;
//			if(Rn.innerProduct(eig[maxComponent][2], pointCentered)>maxFactor*sigma[maxComponent][2]) compId[i]=-1;
		}
		return compId;
	}

}
