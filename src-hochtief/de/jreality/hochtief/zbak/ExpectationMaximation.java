package de.jreality.hochtief.zbak;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import de.jreality.math.Rn;

public class ExpectationMaximation {
	
	private static final double maxValue=1.79E308; 
	public static DenseMatrix unitMatrix=new DenseMatrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});
	private static double pointWeight=1.0;//1.0E290;
	
	public static double[][] calculateParameters(int componentCount, double minChange, double[][] points){
		//params: uj0, uj1, uj2, sigma00j, sigma01j, sigma02j, sigma10j, sigma11j, sigma12j, sigma20j, sigma21j, sigma22j, alphaj
		double[][] params=new double[componentCount][13];
		params[0]=new double[] {0,0,0, 1,0,0, 0,1,0, 0,0,1, 1}; 
		
		double thisPX,lastPX;
		long time;
		long startAllTime=System.currentTimeMillis(); 
		
		//System.out.println("EM start");
		
		for(int currentComponentCount=1; currentComponentCount<=componentCount; currentComponentCount++){   //increase number of components
			System.out.print("maximizing over "+currentComponentCount+" components");
			time=System.currentTimeMillis();			
			
			double[][] p=new double[points.length][currentComponentCount];
	
			double[][] centeroid=new double[currentComponentCount][];
			double[] det=new double[currentComponentCount];
			DenseMatrix cov;
			DenseMatrix[] invCov=new DenseMatrix[currentComponentCount];
			double[] alpha=new double[currentComponentCount];
			
			for(int c=0;c<currentComponentCount;c++){					
				centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			
				det[c]=det3(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
				cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
				invCov[c]=new DenseMatrix(3,3);
				invCov[c]=(DenseMatrix)cov.solve(unitMatrix, invCov[c]);
				alpha[c]=params[c][12];
			}
			
			//System.out.println("\nCURRENTCOMPONENTCOUNT: "+currentComponentCount);
			//System.out.println("INIT THISPX");
			
			thisPX=logpX(points,centeroid,det,invCov,alpha);			
			lastPX=-maxValue;
			
			

			int iterationCount=0;
			while(thisPX-lastPX > minChange){
				lastPX=thisPX;
				
				//System.out.println("\niteration "+iterationCount+": ESTEP");
				
				p=eStep(p, points,centeroid,det,invCov,alpha); // ->just use params to currentComponentCount
				
				//System.out.println("\niteration "+iterationCount+": MSTEP");
				
				params=mStep(p, points, params);				
				
				for(int c=0;c<currentComponentCount;c++){					
					centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			
					det[c]=det3(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
					cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
					invCov[c]=new DenseMatrix(3,3);
					invCov[c]=(DenseMatrix)cov.solve(unitMatrix, invCov[c]);
					alpha[c]=params[c][12];
				}
				
				//System.out.println("\niteration "+iterationCount+": update thisPX");
				
				thisPX=logpX(points,centeroid,det,invCov,alpha);
			
				//System.out.println("thisPX="+thisPX+", lastPX="+lastPX+", diff="+(thisPX-lastPX));
				
				iterationCount++;
			}

			//timer
			double sum=0;
			for(int i=1;i<=currentComponentCount;i++) sum+=(double)i;
			double timeStep=(System.currentTimeMillis()-startAllTime)/sum;
			sum=0;
			for(int i=1;i<=componentCount;i++) sum+=(double)i;
			double estFinishedInTime=timeStep*sum-(System.currentTimeMillis()-startAllTime);
			System.out.append(" ..in "+Math.round((System.currentTimeMillis()-time)/1000.0)+" s, "+iterationCount+" iterations, finished in ~ "+(Math.round(estFinishedInTime/100.0/60.0)/10.0)+" min\n");
		
			if(currentComponentCount<componentCount){
				
				//System.out.println("\nINITNEXTCOMPONENT");
				
				params=initNextComponent(currentComponentCount,params);
			}
		}
		
		System.out.println("maximation overall time: "+(Math.round((System.currentTimeMillis()-startAllTime)/100.0/60.0)/10.0)+" min");
		
		return params;
	}
	
	private static double[][] eStep(double[][] p, double[][] points,  double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha) {	
		for(int i=0;i<p.length;i++){
				p[i]=pComp(points[i],centeroid,det,invCov,alpha); 		
		}
		
		//System.out.println("\npComp(X,Omega):\n"+Rn.toString(p));
		
		return p;
	}

	private static double[][] mStep(double[][] p, double[][] points, double[][] params) {
		
		for(int c=0;c<p[0].length;c++){
			
			double Nc=0;
			for(int i=0;i<points.length;i++)
				Nc+=pointWeight*p[i][c];	
			
			if(Nc!=0){			
				double[] centeroid=new double[3];			
				for(int i=0;i<points.length;i++)					
					Rn.add(centeroid, centeroid, Rn.times(null, pointWeight*p[i][c], points[i]));			
				Rn.times(centeroid, 1.0/Nc, centeroid);

				double[] cov=new double[9];
				for(int i=0;i<points.length;i++){
					double[] centeredPoint=Rn.subtract(null, points[i], centeroid);				
					for(int x=0;x<3;x++){
						for(int y=0;y<3;y++){
							cov[3*x+y]+=centeredPoint[x]*centeredPoint[y]*p[i][c]*pointWeight;
						}	
					}
				}
				for(int x=0;x<9;x++){
					cov[x]=cov[x]/Nc;
				}

				double alpha=Nc/(pointWeight*(double)points.length);

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
		
		//System.out.println("params:\n"+Rn.toString(params));
		
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
//		DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[splitComponent][3],params[splitComponent][4],params[splitComponent][5]},{params[splitComponent][6],params[splitComponent][7],params[splitComponent][8]},{params[splitComponent][9],params[splitComponent][10],params[splitComponent][11]}});
//		SymmPackEVD evd=null;
//		try {
//			evd = SymmPackEVD.factorize(covMtx);
//		} catch (NotConvergedException e) {e.printStackTrace();}
//		DenseMatrix eigM=evd.getEigenvectors();
//		double[] maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
//		double maxEv=evd.getEigenvalues()[2];
//		
//	    System.out.println("splitting comp nr "+splitComponent+" with alpha "+maxAlpha);
//	
//		//new component	
//		params[currentComponentCount]=new double[params[0].length];
//		double[] newCenteroid=new double[] {params[splitComponent][0],params[splitComponent][1],params[splitComponent][2]};
//		
////		System.out.println("old centeroid: "+Rn.toString(newCenteroid));
//		
//		Rn.add(newCenteroid, newCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
//		params[currentComponentCount][0]=newCenteroid[0];
//		params[currentComponentCount][1]=newCenteroid[1];
//		params[currentComponentCount][2]=newCenteroid[2];		
//		for(int i=3;i<12;i++)
//			params[currentComponentCount][i]=0.25*params[splitComponent][i];		
//		params[currentComponentCount][12]=0.5*params[splitComponent][12];
//		
////		System.out.println("new centeroid: "+Rn.toString(newCenteroid));
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
////		System.out.println("updated centeroid: "+Rn.toString(updatedCenteroid)+"/n");
//		
//		return params;
//	}
	
	private static double[][] initNextComponent(int currentComponentCount, double[][] params) {
		//get component with biggest var
		int splitComponentNr=0;
		double maxEv=0;
		double[] maxEig=new double[3];			
		for(int i=0;i<currentComponentCount;i++){			
			DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[i][3],params[i][4],params[i][5]},{params[i][6],params[i][7],params[i][8]},{params[i][9],params[i][10],params[i][11]}});
			SymmPackEVD evd=null;
			try {
				evd = SymmPackEVD.factorize(covMtx);
			} catch (NotConvergedException e) {e.printStackTrace();}
			
			if(evd.getEigenvalues()[2]>maxEv){
				maxEv=evd.getEigenvalues()[2];
				splitComponentNr=i;
				DenseMatrix eigM=evd.getEigenvectors();
				maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
			}
		}
	
	   // System.out.println("splitting comp nr "+splitComponentNr+" with eigenvalue "+maxEv);
		
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
		
		//System.out.println("split component nr "+splitComponentNr);
		//System.out.println("create component nr "+currentComponentCount);
		//System.out.println("new comp:\n"+Rn.toString(params[currentComponentCount]));
		//System.out.println("updated splitted comp:\n"+Rn.toString(params[splitComponentNr]));
		
		return params;
	}

	//p(y=c|x,Omega)
	private static double[] pComp(double[] point, double[][] centeroid, double det[], DenseMatrix[] invCov, double[] alpha) {
		
		//System.out.println("\ncalculating pComp");
		
		double[] pComps=new double[centeroid.length];		
		double logpxSum=logpx(point, centeroid, det, invCov, alpha);
		
		for(int c=0;c<pComps.length;c++){		
			pComps[c]=Math.exp(Math.log(alpha[c]) 
			                   + logpx(point, centeroid[c], det[c], invCov[c]) 
			                   - logpxSum);	
			if(!(pComps[c]>=1.0/maxValue) || pComps[c]>maxValue) pComps[c]=1.0/maxValue;
//			if(!(pComps[c]>=1.0/maxValue)) pComps[c]=0.0;
		}
		return pComps;
	}

	//log(p(X|Omega)
	private static double logpX(double[][] points, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){
		
		//System.out.println("calculating logpX");
		
		double p=0;
		
		for(int i=0;i<points.length;i++)	
			p+=pointWeight*logpx(points[i], centeroid, det, invCov, alpha);

		if(!(p>=-maxValue)){
			p=-maxValue;
//			System.out.println("pX=0");
		}	
		
		
		//System.out.println("logpX="+p);
		return p;
	}
	

    //log(p(x|Omega))
	private static double logpx(double[] point, double[][] centeroid, double[] det, DenseMatrix[] invCov, double[] alpha){
		
		
		//System.out.println("  calculating logpx(Omega)");
		
		double[] logpx=new double[centeroid.length];
		double maxLogAlphapx=0;
		double logAlpha=0;
		for(int c=0;c<logpx.length;c++){
			logpx[c]=logpx(point, centeroid[c], det[c], invCov[c]);
			logAlpha=Math.log(alpha[c]);
			if(logpx[c]+logAlpha>maxLogAlphapx) maxLogAlphapx=logpx[c]+logAlpha;
		}
		
		double p=0;
		for(int c=0;c<logpx.length;c++){
//			if(logpx[c]>=-maxValue)  //????????????????????????
				p+=Math.exp(Math.log(alpha[c])+logpx[c]-maxLogAlphapx);
		}
		
		p=Math.log(p);
			
		p+=maxLogAlphapx;	
		
		if(!(p>=-maxValue)){
			p=-maxValue;
//			System.out.println("px(Omega)=0");
		}
		
		//System.out.println("  logpx(Omega)="+p);
		
		return p;
	}

    //log(p(x|omega))
	private static double logpx(double[] point, double[] centeroid, double det, DenseMatrix invCov){
		
		//System.out.println("    calculating logpx(omega)");
		
		double[] pointCentered=Rn.subtract(null, point, centeroid);
		
		double p=0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				p+=pointCentered[i]*pointCentered[j]*invCov.get(i,j);				
			}		
		}
		
		p+=Math.log(det*Math.pow(2*Math.PI, 3));
		
		p=-0.5*p;
		
		if(!(p>=-maxValue)){
			p=-maxValue;
//			System.out.println("px(omega)=0");
		}
		
		//System.out.println("    logpx(omega)="+p);
		
		return p;
	}
	
	public static double det3(double a00, double a01, double a02, double a10, double a11, double a12, double a20, double a21, double a22){
		double det=a00*(a11*a22-a12*a21)-a01*(a12*a20-a10*a22)+a02*(a10*a21-a11*a20);
		if(!(det>=-maxValue))
			det=-maxValue;
		return det; 	
	}
	
	public static int[] evalPoints(double[][] points, double[][] params){
		
		//System.out.println("\nevalPoints");
		
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
			det[c]=det3(params[c][3],params[c][4],params[c][5],params[c][6],params[c][7],params[c][8],params[c][9],params[c][10],params[c][11]);
			cov=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			invCov[c]=new DenseMatrix(3,3);
			invCov[c]=(DenseMatrix)cov.solve(unitMatrix, invCov[c]);
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
		
		double[][] p=new double[points.length][params.length];
		for(int i=0;i<p.length;i++){
			p[i]=pComp(points[i],centeroid,det,invCov,alpha); 				
		}	
		
		for(int i=0;i<points.length;i++){
			int maxComponent=0;
			double maxP=0;		
			for(int c=0;c<params.length;c++){
				if(p[i][c]>maxP){
					maxP=p[i][c];
					maxComponent=c;
				}
			}
			compId[i]=maxComponent;
		}
		return compId;
	}

}
