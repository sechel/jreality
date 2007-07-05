package de.jreality.hochtief.pointclouds;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import de.jreality.math.Rn;

public class ExpectationMaximation {

	private static final double maxValue=1.79E308; 
	private static double pointWeight=1.0;

	public static double[][] calculateParameters(int componentCount, double minChange, double[][] points){
		System.out.println("EM over "+points.length+" points");

		//params: uj0, uj1, uj2, sigma00j, sigma01j, sigma02j, sigma10j, sigma11j, sigma12j, sigma20j, sigma21j, sigma22j, alphaj
		double[][] params=new double[componentCount][13];
		params[0]=new double[] {0,0,0, 1,0,0, 0,1,0, 0,0,1, 1}; 

		double thisPX,lastPX;
		long time;
		long startAllTime=System.currentTimeMillis(); 

		for(int currentComponentCount=1; currentComponentCount<=componentCount; currentComponentCount++){   //increase number of components
			System.out.print("maximizing over "+currentComponentCount+" components");
			time=System.currentTimeMillis();			

			double[][] p=new double[points.length][currentComponentCount];

			double[][] centeroid=new double[currentComponentCount][];
			double[] det=new double[currentComponentCount];
			UpperSymmPackMatrix cov;
			UpperSymmPackMatrix[] invCov=new UpperSymmPackMatrix[currentComponentCount];
			double[] alpha=new double[currentComponentCount];

			for(int c=0;c<currentComponentCount;c++){					
				centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			

				cov=new UpperSymmPackMatrix(3);
				for(int i=0;i<3;i++){
					for(int j=i;j<3;j++){				
						cov.set(i,j, params[c][3*i+j+3]);				
					}			
				}
				det[c]=det3(cov);
				invCov[c]=getInvSymm3(cov,det[c]);

				alpha[c]=params[c][12];
			}

			thisPX=logpX(points,centeroid,det,invCov,alpha);			
			lastPX=-maxValue; 

			int iterationCount=0;
			while(thisPX-lastPX > minChange){
				lastPX=thisPX;

				p=eStep(p, points,centeroid,det,invCov,alpha); // ->just use params to currentComponentCount
				params=mStep(p, points, params);				

				for(int c=0;c<currentComponentCount;c++){					
					centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			

					cov=new UpperSymmPackMatrix(3);
					for(int i=0;i<3;i++){
						for(int j=i;j<3;j++){				
							cov.set(i,j, params[c][3*i+j+3]);				
						}			
					}
					det[c]=det3(cov);
					invCov[c]=getInvSymm3(cov,det[c]);

					alpha[c]=params[c][12];
				}
				thisPX=logpX(points,centeroid,det,invCov,alpha);

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
				params=initNextComponent(currentComponentCount,params);
			}
		}

		System.out.println("maximation overall time: "+(Math.round((System.currentTimeMillis()-startAllTime)/100.0/60.0)/10.0)+" min");

		return params;
	}

	private static double[][] eStep(double[][] p, double[][] points,  double[][] centeroid, double[] det, UpperSymmPackMatrix[] invCov, double[] alpha) {	
		for(int i=0;i<p.length;i++){
			p[i]=pComp(points[i],centeroid,det,invCov,alpha); 	
		}
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
				System.err.println("Nc==0");
				for(int i=3;i<params[c].length;i++)
					params[c][i]=0;
				params[c][3]=1.0/maxValue; params[c][7]=1.0/maxValue; params[c][11]=1.0/maxValue;
			}
		}
		return params;
	}


//	private static double[][] initNextComponent(int currentComponentCount, double[][] params) {
//	//get component with biggest alpha
//	int splitComponent=0;
//	double maxAlpha=0;					
//	for(int c=0;c<currentComponentCount;c++){			
//	if(params[c][12]>maxAlpha){
//	maxAlpha=params[c][12];
//	splitComponent=c;
//	}
//	}

//	DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[splitComponent][3],params[splitComponent][4],params[splitComponent][5]},{params[splitComponent][6],params[splitComponent][7],params[splitComponent][8]},{params[splitComponent][9],params[splitComponent][10],params[splitComponent][11]}});
//	SymmPackEVD evd=null;
//	try {
//	evd = SymmPackEVD.factorize(covMtx);
//	} catch (NotConvergedException e) {e.printStackTrace();}
//	DenseMatrix eigM=evd.getEigenvectors();
//	double[] maxEig=new double[] {eigM.get(0, 2),eigM.get(1, 2),eigM.get(2, 2)};
//	double maxEv=evd.getEigenvalues()[2];

//	System.out.println("splitting comp nr "+splitComponent+" with alpha "+maxAlpha);

//	//new component	
//	params[currentComponentCount]=new double[params[0].length];
//	double[] newCenteroid=new double[] {params[splitComponent][0],params[splitComponent][1],params[splitComponent][2]};

////	System.out.println("old centeroid: "+Rn.toString(newCenteroid));

//	Rn.add(newCenteroid, newCenteroid, Rn.times(null,-0.5*Math.sqrt(maxEv),maxEig));
//	params[currentComponentCount][0]=newCenteroid[0];
//	params[currentComponentCount][1]=newCenteroid[1];
//	params[currentComponentCount][2]=newCenteroid[2];		
//	for(int i=3;i<12;i++)
//	params[currentComponentCount][i]=0.25*params[splitComponent][i];		
//	params[currentComponentCount][12]=0.5*params[splitComponent][12];

////	System.out.println("new centeroid: "+Rn.toString(newCenteroid));

//	//update splitted component
//	double[] updatedCenteroid=new double[] {params[splitComponent][0],params[splitComponent][1],params[splitComponent][2]};
//	Rn.add(updatedCenteroid, updatedCenteroid, Rn.times(null,0.5*Math.sqrt(maxEv),maxEig));
//	params[splitComponent][0]=updatedCenteroid[0];
//	params[splitComponent][1]=updatedCenteroid[1];
//	params[splitComponent][2]=updatedCenteroid[2];		
//	for(int i=3;i<12;i++)
//	params[splitComponent][i]=0.25*params[splitComponent][i];			
//	params[splitComponent][12]=0.5*params[splitComponent][12];		

////	System.out.println("updated centeroid: "+Rn.toString(updatedCenteroid)+"/n");

//	return params;
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

	//p(y=c|x,Omega)
	private static double[] pComp(double[] point, double[][] centeroid, double det[], UpperSymmPackMatrix[] invCov, double[] alpha) {
		double[] pComps=new double[centeroid.length];		
		double logpxSum=logpx(point, centeroid, det, invCov, alpha);

		for(int c=0;c<pComps.length;c++){		
			pComps[c]=Math.exp(Math.log(alpha[c]) 
					+ logpx(point, centeroid[c], det[c], invCov[c]) 
					- logpxSum);	
//			if((!(pComps[c]>=1.0/maxValue)) || pComps[c]>maxValue) pComps[c]=1.0/maxValue;
			if((!(pComps[c]>=1.0/maxValue)) || pComps[c]>maxValue) pComps[c]=0.0;  //???????????????????????????????????
		}
		return pComps;
	}

	//log(p(X|Omega)
	private static double logpX(double[][] points, double[][] centeroid, double[] det, UpperSymmPackMatrix[] invCov, double[] alpha){
		double p=0;
		for(int i=0;i<points.length;i++)	
			p+=pointWeight*logpx(points[i], centeroid, det, invCov, alpha);

		if(!(p>=-maxValue)){
			p=-maxValue;
		}	

		return p;
	}


	//log(p(x|Omega))
	private static double logpx(double[] point, double[][] centeroid, double[] det, UpperSymmPackMatrix[] invCov, double[] alpha){
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
			if(logpx[c]>=-maxValue)  //?????????????????????????????????????????????????????????
				p+=Math.exp(Math.log(alpha[c])+logpx[c]-maxLogAlphapx);
		}

		p=Math.log(p);

		p+=maxLogAlphapx;	

		if(!(p>=-maxValue)){
			p=-maxValue;
		}

//		if(p>1){
//		System.out.println("\n");
//		System.out.println("  logpx(Omega)="+p);
//		for(int c=0;c<logpx.length;c++){
//		System.out.println("c: "+c);
//		System.out.println("logpx[c]: "+logpx[c]);
//		System.out.println("maxLogAlphapx: "+maxLogAlphapx);
//		System.out.println("alpha[c]: "+alpha[c]);
//		System.out.println(Math.exp(Math.log(alpha[c])+logpx[c]-maxLogAlphapx));
//		}
//		}

		return p;
	}

	//log(p(x|omega))
	private static double logpx(double[] point, double[] centeroid, double det, UpperSymmPackMatrix invCov){
		double[] pointCentered=Rn.subtract(null, point, centeroid);

		double p=0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				p+=pointCentered[i]*pointCentered[j]*invCov.get(i,j);				
			}		
		}

//		if(p==0.0) return 0; //?????????????????????????????????????????????????

		p+=Math.log(det*Math.pow(2*Math.PI, 3));

		p=-0.5*p;

		if(!(p>=-maxValue)){
			p=-maxValue;
		}
		
//		if(p>0){
//			System.err.println("\nlogpx="+p);
//			System.err.println("point: "+Rn.toString(point));
//			System.err.println("centeroid: "+Rn.toString(centeroid));
//			System.err.println("det: "+det);
//			System.err.println("invCov:");
//			for(int i=0;i<3;i++){
//				for(int j=0;j<3;j++){
//					System.err.println(invCov.get(i,j));
//				}
//			}
//			UpperSymmPackMatrix cov=getInvSymm3(invCov);
//			System.err.println("cov:");
//			for(int i=0;i<3;i++){
//				for(int j=0;j<3;j++){
//					System.err.println(cov.get(i,j));
//				}
//			}
//			DenseMatrix test=new DenseMatrix(3,3);
//			test=(DenseMatrix)invCov.mult(cov, test);
//			System.err.println("invCov*cov:");
//			for(int i=0;i<3;i++){
//				for(int j=0;j<3;j++){				
//					System.err.println(test.get(i,j));				
//				}			
//			}			 
//		}
		
		if(p>0) System.err.println("logpx="+p);

		return p;
	}

//	public static double det3(double a00, double a01, double a02, double a10, double a11, double a12, double a20, double a21, double a22){
//		double det=a00*a11*a22+a01*a12*a20+a02*a10*a21-a00*a12*a21-a01*a10*a22-a02*a11*a20;
//
//		if(!(Math.abs(det)>=1.0/maxValue))
//			det=Math.signum(det)*1.0/maxValue;
//		if(Math.abs(det)>maxValue)
//			det=Math.signum(det)*maxValue;
//	
//		return det; 	
//	}

	public static double det3(UpperSymmPackMatrix mtx){
		double det=mtx.get(0,0)*mtx.get(1,1)*mtx.get(2,2)+mtx.get(0,1)*mtx.get(1,2)*mtx.get(2,0)+mtx.get(0,2)*mtx.get(1,0)*mtx.get(2,1)-mtx.get(0,0)*mtx.get(1,2)*mtx.get(2,1)-mtx.get(0,1)*mtx.get(1,0)*mtx.get(2,2)-mtx.get(0,2)*mtx.get(1,1)*mtx.get(2,0);

		if(!(Math.abs(det)>=1.0/maxValue))
			det=Math.signum(det)*1.0/maxValue;	
		if(Math.abs(det)>maxValue)
			det=Math.signum(det)*maxValue;
		
		return det; 	
	}

	public static UpperSymmPackMatrix unitMatrix=new UpperSymmPackMatrix(new DenseMatrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}}));

	public static UpperSymmPackMatrix getInvSymm3(UpperSymmPackMatrix mtx, double detMtx){
		UpperSymmPackMatrix inv=new UpperSymmPackMatrix(mtx.numRows());
		inv.set(0,0, mtx.get(1,1)*mtx.get(2,2)-mtx.get(1,2)*mtx.get(1,2));
		inv.set(0,1, mtx.get(1,2)*mtx.get(0,2)-mtx.get(0,1)*mtx.get(2,2));
		inv.set(0,2, mtx.get(0,1)*mtx.get(1,2)-mtx.get(1,1)*mtx.get(0,2));
		inv.set(1,1, mtx.get(0,0)*mtx.get(2,2)-mtx.get(0,2)*mtx.get(0,2));
		inv.set(1,2, mtx.get(0,1)*mtx.get(0,2)-mtx.get(0,0)*mtx.get(1,2));
		inv.set(2,2, mtx.get(0,0)*mtx.get(1,1)-mtx.get(0,1)*mtx.get(0,1));

//		double detMtx=det3(mtx);

		for(int i=0;i<mtx.numRows();i++){
			for(int j=i;j<mtx.numColumns();j++){				
				inv.set(i, j, inv.get(i,j)/detMtx);				
			}			
		}

		//debug:
//		DenseMatrix test=new DenseMatrix(3,3);
//		test=(DenseMatrix)inv.mult(mtx, test);
//		System.out.println("\n");
//		for(int i=0;i<mtx.numRows();i++){
//			for(int j=0;j<mtx.numColumns();j++){				
//				System.out.println(test.get(i,j));				
//			}			
//		}

		return inv;
	}

//	public static UpperSymmPackMatrix getInvSymm3(UpperSymmPackMatrix mtx){
//	DenseMatrix invDense=new DenseMatrix(mtx.numRows(),mtx.numColumns());
//	invDense=(DenseMatrix)mtx.solve(unitMatrix,invDense);
//
//	UpperSymmPackMatrix inv=new UpperSymmPackMatrix(mtx.numRows());
//	double value;
//	for(int i=0;i<mtx.numRows();i++){
//		for(int j=i;j<mtx.numColumns();j++){
//			value=0.5*(invDense.get(i,j) + invDense.get(j,i));
//			inv.set(i,j,value);				
//		}			
//	}
//
//	//debug:
////DenseMatrix test=new DenseMatrix(3,3);
////test=(DenseMatrix)inv.mult(mtx, test);
////System.out.println("\n");
////for(int i=0;i<mtx.numRows();i++){
////	for(int j=0;j<mtx.numColumns();j++){				
////		System.out.println(test.get(i,j));				
////	}			
////}
//
//	return inv;
//}
	
	private static double minAllowedP=0.0001;

	public static int[] evalPoints(double[][] points, double[][] params){
		int[] compId=new int[points.length];

		double[][] centeroid=new double[params.length][];
		double[] det=new double[params.length];		UpperSymmPackMatrix cov;		UpperSymmPackMatrix[] invCov=new UpperSymmPackMatrix[params.length];
		double[] alpha=new double[params.length];
		double[][][] eig=new double[params.length][3][3];
		double[][] sigma=new double[params.length][3];
		for(int c=0;c<params.length;c++){
			centeroid[c]=new double[] {params[c][0],params[c][1],params[c][2]};			

			cov=new UpperSymmPackMatrix(3);
			for(int i=0;i<3;i++){
				for(int j=i;j<3;j++){				
					cov.set(i,j, params[c][3*i+j+3]);				
				}			
			}
			det[c]=det3(cov);
			invCov[c]=getInvSymm3(cov,det[c]);

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
			
			compId[i]=-1;
			double thisP=Math.exp(logpx(points[i], centeroid[maxComponent], det[maxComponent], invCov[maxComponent]));
			if(thisP>minAllowedP)
				compId[i]=maxComponent;
		}
		return compId;
	}

}
